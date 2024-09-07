package io.vepo.maestro.framework.utils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import io.vepo.maestro.framework.annotations.Topic;

public class Topics {
    private static final String CAMEL_CASE_NAME = "([a-z])([A-Z]+)";
    private static final String CAMEL_CASE_REPLACEMENT = "$1-$2";
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(CAMEL_CASE_NAME);

    public static String toTopicName(Method method) {
        if (method.isAnnotationPresent(Topic.class)) {
            return method.getAnnotation(Topic.class).value();
        } else {
            return camelCaseToTopic(method.getName());
        }
    }

    public static boolean match(String topic, Method m) {
        if (m.isAnnotationPresent(Topic.class)) {
            return m.getAnnotation(Topic.class).value().equals(topic);
        } else {
            return camelCaseToTopic(m.getName()).equals(topic);
        }
    }

    public static String camelCaseToTopic(String name) {
        var matcher = CAMEL_CASE_PATTERN.matcher(name);
        return matcher.replaceAll(CAMEL_CASE_REPLACEMENT)
                      .toLowerCase();
    }
}
