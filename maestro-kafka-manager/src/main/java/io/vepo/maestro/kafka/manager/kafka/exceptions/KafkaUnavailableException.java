package io.vepo.maestro.kafka.manager.kafka.exceptions;

public class KafkaUnavailableException extends KafkaUnexpectedException {

    public KafkaUnavailableException(String message, Exception cause) {
        super(message, cause);
    }

}
