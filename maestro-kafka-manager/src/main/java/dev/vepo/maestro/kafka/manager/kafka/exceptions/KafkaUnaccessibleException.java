package dev.vepo.maestro.kafka.manager.kafka.exceptions;

import org.apache.kafka.common.KafkaException;

public class KafkaUnaccessibleException extends RuntimeException {

    public KafkaUnaccessibleException(String message, KafkaException cause) {
        super(message, cause);
    }
    
}
