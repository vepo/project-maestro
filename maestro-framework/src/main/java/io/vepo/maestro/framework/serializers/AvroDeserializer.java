package io.vepo.maestro.framework.serializers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Deserializer;

import io.vepo.maestro.framework.utils.Topics;

/**
 * The Avro Deserializer allows receiving AVRO messages from Kafka.
 */
public class AvroDeserializer implements Deserializer<Object> {

    private static Object instantiate(Constructor<?> constructor, GenericRecord data) {
        try {
            return constructor.newInstance(Stream.of(constructor.getParameters())
                                                 .map(p -> avro2Record(data.get(p.getName()), p.getType()))
                                                 .toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new KafkaException("Error deserializing Avro message", e);
        }
    }

    private static Object avro2Record(Object data, Class<?> dataType) {
        if (data instanceof GenericRecord avroData) {
            return Stream.of(dataType.getConstructors())
                         .sorted(Comparator.comparingInt(c -> ((Constructor<?>) c).getParameterCount()).reversed())
                         .findFirst()
                         .map(c -> instantiate(c, avroData))
                         .orElseThrow(() -> new KafkaException("Constructor not found"));
        } else if (data instanceof Utf8 utf8Data) {
            return utf8Data.toString();
        } else {
            return data;
        }
    }

    private Class<?> type;

    private Map<String, Schema> schemaCache = new HashMap<>();

    private Map<String, GenericDatumReader<GenericRecord>> readerCache = new HashMap<>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type") instanceof Class typeClass) {
            this.type = typeClass;
        } else {
            throw new IllegalArgumentException("Type parameter is required for JsonDeserializer!");
        }

    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        var dataType = Stream.of(type.getDeclaredMethods())
                             .filter(m -> Topics.match(topic, m))
                             .findFirst()
                             .map(m -> m.getParameterTypes()[0])
                             .orElseThrow(() -> new KafkaException("Method not found for topic " + topic));

        var schema = schemaCache.computeIfAbsent(topic, t -> ReflectData.get().getSchema(dataType));
        var reader = readerCache.computeIfAbsent(topic, t -> new GenericDatumReader<>(schema));
        if (Objects.nonNull(data) && data.length > 0) {
            try {
                Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
                var avroData = reader.read(null, decoder);
                return avro2Record(avroData, dataType);
            } catch (IOException e) {
                throw new KafkaException("Error deserializing Avro message", e);
            }
        } else {
            return null;
        }
    }
}
