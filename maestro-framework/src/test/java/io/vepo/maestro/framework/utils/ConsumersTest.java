package io.vepo.maestro.framework.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.vepo.maestro.framework.Metadata;

class ConsumersTest {

    class SingleParameterConsumer {
        public void consume(String message) {
        }
    }

    class ConsumerWithPrivateMethod {
        public void consume(String message) {
            privateMethod(message);
        }

        private void privateMethod(String message) {
        }

    }

    class MultipleParameterConsumer {
        public void consume(String message, String other) {
        }
    }

    class ConsumerWithMetadata {
        public void consume(String message, Metadata metadata) {
        }
    }

    @Test
    void findConsumerWithPrivateMethodTest() {
        var consumers = Consumers.findConsumerMethods(ConsumerWithPrivateMethod.class);
        assertThat(consumers).hasSize(1);
    }

    @Test
    void findMultipleParameterConsumerTest() {
        var consumers = Consumers.findConsumerMethods(MultipleParameterConsumer.class);
        assertThat(consumers).isEmpty();
    }

    @Test
    void findSingleParameterConsumerTest() {
        var consumers = Consumers.findConsumerMethods(SingleParameterConsumer.class);
        assertThat(consumers).hasSize(1);
    }

    @Test
    void findConsumerWithMetadataTest() {
        var consumers = Consumers.findConsumerMethods(ConsumerWithMetadata.class);
        assertThat(consumers).hasSize(1);
    }
}
