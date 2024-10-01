package io.vepo.maestro.framework.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vepo.maestro.framework.Metadata;

public class Consumers {
    public static List<Method> findConsumerMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                     .filter(method -> Modifier.isPublic(method.getModifiers()) && Stream.of(method.getParameterTypes())
                                                                                         .filter(parameter -> parameter != Metadata.class)
                                                                                         .count() == 1)
                     .collect(Collectors.toList());
    }
}
