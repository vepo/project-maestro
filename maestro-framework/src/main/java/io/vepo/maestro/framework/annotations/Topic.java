package io.vepo.maestro.framework.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({
    METHOD,
    TYPE })
@Retention(RUNTIME)
public @interface Topic {
    String value();

    String name() default "";
}
