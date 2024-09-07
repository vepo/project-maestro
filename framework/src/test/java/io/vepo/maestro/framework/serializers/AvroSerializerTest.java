package io.vepo.maestro.framework.serializers;

import java.io.IOException;

import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class AvroSerializerTest {
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

    @SuppressWarnings("unchecked")
    private static final <T> T bytes2Pojo(byte[] bytes, Class<T> clazz) {
        var schema = ReflectData.get().getSchema(clazz);
        var reader = new GenericDatumReader<GenericRecord>(schema);
        var decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        try {
            var avroData = reader.read(null, decoder);
            if (clazz == Pojo.class) {
                var pojo = (GenericRecord) avroData;
                return (T) new Pojo(pojo.get("stringValue").toString(), (int) pojo.get("intValue"),
                                    (float) pojo.get("floatValue"), (double) pojo.get("doubleValue"), (long) pojo.get("longValue"),
                                    (boolean) pojo.get("booleanValue"));
            } else {
                var pojo = (GenericRecord) avroData;
                var pojoValue = (GenericRecord) pojo.get("pojoValue");
                return (T) new ComposedPojo(pojo.get("stringValue").toString(),
                                            new Pojo(pojoValue.get("stringValue").toString(), (int) pojoValue.get("intValue"),
                                                     (float) pojoValue.get("floatValue"), (double) pojoValue.get("doubleValue"),
                                                     (long) pojoValue.get("longValue"), (boolean) pojoValue.get("booleanValue")));
            }
        } catch (IOException e) {
            Assertions.fail("Error deserializing bytes", e);
            return null;
        }
    }

    @Test
    void testSerialize() {
        try (var serializer = new AvroSerializer()) {
            var expectedValue = new Pojo("ABCD", 7, 8.2f, 2.1, 10L, true);
            var actualValue = bytes2Pojo(serializer.serialize("", expectedValue), Pojo.class);
            Assertions.assertThat(actualValue).isEqualTo(expectedValue);
        }
    }
}
