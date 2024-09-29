package io.vepo.maestro.framework.serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Serializer;

/**
 * The Avro Serializer allows sending AVRO messages to Kafka.
 */
public class AvroSerializer implements Serializer<Object> {
    private Map<String, GenericDatumWriter<Object>> writerCache = new HashMap<>();
    private Map<String, Schema> schemaCache = new HashMap<>();

    @Override
    public byte[] serialize(String topic, Object value) {
        var schema = schemaCache.computeIfAbsent(topic, t -> ReflectData.get().getSchema(value.getClass()));
        var writer = writerCache.computeIfAbsent(topic, t -> new ReflectDatumWriter<>(schema));
        if (Objects.nonNull(value)) {

            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                Encoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
                writer.write(value, encoder);
                encoder.flush();
                return stream.toByteArray();
            } catch (IOException e) {
                throw new KafkaException("Error serializing Avro message", e);
            }
        } else {
            return null;
        }
    }
}
