package dev.vepo.maestro.kafka.manager.kafka.exceptions;

public class KafkaUnexpectedException extends Exception {

    public KafkaUnexpectedException(Exception cause) {
        super(cause);
    }

    public KafkaUnexpectedException(String message, Exception cause) {
        super(message, cause);
    }

}
