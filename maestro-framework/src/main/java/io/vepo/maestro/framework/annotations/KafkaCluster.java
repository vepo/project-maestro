package io.vepo.maestro.framework.annotations;

public @interface KafkaCluster {
    /**
     * The Kafka cluster bootstrap servers.
     */
    public String bootstrapServers() default "";
}
