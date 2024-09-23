package io.vepo.maestro.framework.serializers;

import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer<T> implements Serializer<T> {

    private ObjectMapper mapper;

    public void configure(Map<String, ?> configs, boolean isKey) {
        this.mapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new KafkaException("Error serializing JSON message", e);
        }
    }

}
