package io.vepo.maestro.framework.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Documented
@Retention(RUNTIME)
public @interface KafkaCluster {
    /**
     * The Kafka cluster bootstrap servers.
     */
    public String bootstrapServers() default "";
}
