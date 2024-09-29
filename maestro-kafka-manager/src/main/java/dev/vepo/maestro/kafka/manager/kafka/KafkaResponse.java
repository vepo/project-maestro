package dev.vepo.maestro.kafka.manager.kafka;

import java.util.Objects;

public record KafkaResponse<V, E extends Exception>(V value, E exception) {
    public KafkaResponse(V value) {
        this(value, null);
    }

    public KafkaResponse(E exception) {
        this(null, exception);
    }

    public V getOrThrow() throws E {
        if (Objects.nonNull(value)) {
            return value;
        } else {
            throw exception;
        }
    }
}
