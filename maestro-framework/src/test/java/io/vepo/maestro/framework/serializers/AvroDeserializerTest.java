package io.vepo.maestro.framework.serializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vepo.maestro.framework.annotations.Topic;

public class AvroDeserializerTest {
    public static record Pojo(String stringValue, int intValue, float floatValue, double doubleValue, long longValue, boolean booleanValue) {
        public Pojo() {
            this("", 0, 0.0f, 0.0, 0L, false);
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    public static record ComposedPojo(String stringValue, Pojo pojoValue) {
        public ComposedPojo() {
            this("", new Pojo());
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    public void consumeSimple(Pojo pojo) {
        System.out.println(pojo);
    }

    public void consumeComposed(ComposedPojo pojo) {
        System.out.println(pojo);
    }

    @Topic("another-consume-simple")
    public void consumeSimpleWithTopic(Pojo pojo) {
        System.out.println(pojo);
    }

    @Nested
    class NameResolution {
        @Test
        @DisplayName("Should resolve the topic name from the method name")
        void resolveTopicNameFromMethodName() {
            try (var avroDeserializer = new AvroDeserializer()) {
                avroDeserializer.configure(Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", AvroDeserializerTest.class), false);
                var pojo = new Pojo("ABCD", 7, 8.2f, 2.1, 10L, true);
                var deserializedPojo = avroDeserializer.deserialize("another-consume-simple", record2Bytes(pojo));
                assertThat(deserializedPojo).as("Deserialized Pojo")
                                            .isNotNull()
                                            .hasFieldOrPropertyWithValue("stringValue", "ABCD")
                                            .hasFieldOrPropertyWithValue("intValue", 7)
                                            .hasFieldOrPropertyWithValue("floatValue", 8.2f)
                                            .hasFieldOrPropertyWithValue("doubleValue", 2.1)
                                            .hasFieldOrPropertyWithValue("longValue", 10L)
                                            .hasFieldOrPropertyWithValue("booleanValue", true);
            }
        }

        @Test
        @DisplayName("If cannot resolve topic name, should throw an exception")
        void resolveTopicNameFromMethodNameFail() {
            try (var avroDeserializer = new AvroDeserializer()) {
                avroDeserializer.configure(Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", AvroDeserializerTest.class), false);
                var pojo = new Pojo("ABCD", 7, 8.2f, 2.1, 10L, true);
                Assertions.assertThatThrownBy(() -> avroDeserializer.deserialize("not-found-consume-simple", record2Bytes(pojo)))
                          .isInstanceOf(KafkaException.class)
                          .hasMessage("Method not found for topic not-found-consume-simple");
            }
        }
    }

    private static <T> byte[] record2Bytes(T data) {
        try (var baos = new ByteArrayOutputStream()) {
            var writer = new ReflectDatumWriter<T>(ReflectData.get().getSchema(data.getClass()));
            var encoder = EncoderFactory.get().binaryEncoder(baos, null);
            writer.write(data, encoder);
            encoder.flush();
            var binaryPojo = baos.toByteArray();
            assertThat(binaryPojo).isNotEmpty();
            return binaryPojo;
        } catch (IOException e) {
            fail("Error serializing data", e);
            return null;
        }
    }

    @Nested
    class Serialize {
        @Test
        @DisplayName("Should deserialize a simple pojo")
        void simplePojoDeserializer() throws IOException {
            try (var avroDeserializer = new AvroDeserializer()) {
                avroDeserializer.configure(Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", AvroDeserializerTest.class), false);
                var pojo = new Pojo("ABCD", 7, 8.2f, 2.1, 10L, true);
                var deserializedPojo = avroDeserializer.deserialize("consume-simple", record2Bytes(pojo));
                assertThat(deserializedPojo).as("Deserialized Pojo")
                                            .isNotNull()
                                            .hasFieldOrPropertyWithValue("stringValue", "ABCD")
                                            .hasFieldOrPropertyWithValue("intValue", 7)
                                            .hasFieldOrPropertyWithValue("floatValue", 8.2f)
                                            .hasFieldOrPropertyWithValue("doubleValue", 2.1)
                                            .hasFieldOrPropertyWithValue("longValue", 10L)
                                            .hasFieldOrPropertyWithValue("booleanValue", true);
            }
        }

        @Test
        @DisplayName("Should deserialize a composed pojo")
        void composedPojoTest() throws IOException {
            try (var avroDeserializer = new AvroDeserializer()) {
                avroDeserializer.configure(Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", AvroDeserializerTest.class), false);
                var pojo = new ComposedPojo("ABCD", new Pojo("EFH", 7, 8.2f, 2.1, 10L, true));
                var deserializedPojo = avroDeserializer.deserialize("consume-composed", record2Bytes(pojo));
                assertThat(deserializedPojo).as("Deserialized Pojo")
                                            .isNotNull()
                                            .hasFieldOrPropertyWithValue("stringValue", "ABCD")
                                            .extracting("pojoValue")
                                            .hasFieldOrPropertyWithValue("intValue", 7)
                                            .hasFieldOrPropertyWithValue("floatValue", 8.2f)
                                            .hasFieldOrPropertyWithValue("doubleValue", 2.1)
                                            .hasFieldOrPropertyWithValue("longValue", 10L)
                                            .hasFieldOrPropertyWithValue("booleanValue", true);
            }
        }
    }
}