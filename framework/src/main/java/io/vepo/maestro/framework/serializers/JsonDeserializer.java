package io.vepo.maestro.framework.serializers;

import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer<T> implements Deserializer<T> {
    private ObjectMapper mapper;
    private Class<T> type;

    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.mapper = new ObjectMapper();
        if (configs.get("type") instanceof Class typeClass) {
            this.type = typeClass;
        } else {
            throw new IllegalArgumentException("Type parameter is required for JsonDeserializer!");
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, type);
        } catch (Exception e) {
            throw new KafkaException("Error deserializing JSON message", e);
        }
    }

}
