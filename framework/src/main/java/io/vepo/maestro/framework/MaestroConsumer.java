package io.vepo.maestro.framework;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.kafka.common.serialization.Deserializer;

import io.vepo.maestro.framework.serializers.JsonDeserializer;

@Target({
    METHOD,
    TYPE })
@Retention(RUNTIME)
public @interface MaestroConsumer {
    public static Class<? extends Deserializer> DEFAULT_DESERIALIZER = JsonDeserializer.class;

    public Class<? extends Deserializer> keyDeserializer() default JsonDeserializer.class;

    public Class<? extends Deserializer> valueDeserializer() default JsonDeserializer.class;
}
