package io.vepo.maestro.framework.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.vepo.maestro.framework.serializers.JsonDeserializer;

@Target({
    METHOD,
    TYPE })
@Retention(RUNTIME)
public @interface MaestroConsumer {
    public static Class<? extends Deserializer> DEFAULT_DESERIALIZER = JsonDeserializer.class;

    public Class<? extends Deserializer> keyDeserializer() default StringDeserializer.class;

    public Class<? extends Deserializer> valueDeserializer() default JsonDeserializer.class;
}
