package io.vepo.maestro.framework.serializers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.maestro.framework.annotations.Topic;

public class JsonDeserializer<T> implements Deserializer<T> {
    private ObjectMapper mapper;
    private Class<T> type;

    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.mapper = new ObjectMapper();
        if (configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type") instanceof Class typeClass) {
            this.type = typeClass;
        } else {
            throw new IllegalArgumentException("Type parameter is required for JsonDeserializer!");
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            var dataType = Stream.of(type.getDeclaredMethods())
                                 .filter(m -> match(topic, m))
                                 .findFirst()
                                 .map(m -> m.getParameterTypes()[0])
                                 .orElseThrow(() -> new KafkaException("Method not found"));
            return (T) mapper.readValue(data, dataType);
        } catch (Exception e) {
            throw new KafkaException("Error deserializing JSON message", e);
        }
    }

    private boolean match(String topic, Method m) {
        if (m.isAnnotationPresent(Topic.class)) {
            return m.getAnnotation(Topic.class).value().equals(topic);
        } else {
            return m.getName().equals(topic);
        }
    }

}
