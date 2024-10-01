package io.vepo.maestro.framework;

public record Metadata(String topic, int partition, long offset, long timestamp) {

}
