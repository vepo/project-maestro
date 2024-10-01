package dev.vepo.maestro.framework.sample.temperature;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils;

import io.vepo.maestro.framework.MaestroApplication;

@Testcontainers
class TemperatureSensorApplicationTest {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureSensorApplicationTest.class.getName());

    @Container
    KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    private void createTopics(String... topics) {
        var newTopics = Arrays.stream(topics)
                              .map(topic -> new NewTopic(topic, 1, (short) 1))
                              .collect(Collectors.toList());
        try (var admin = AdminClient.create(Map.of(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            admin.createTopics(newTopics);
        }
    }

    @Test
    void consumeMessages() throws IOException, InterruptedException, ExecutionException {
        var folder = Files.createTempDirectory("maestro");

        System.setProperty("message.handle.file.path", Paths.get(folder.toAbsolutePath().toString(), "messages.txt").toString());
        try (MaestroApplication maestroApplication = startApp(TemperatureSensorApplication.class)) {
            createTopics("messages");
            var configs = new Properties();
            configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
            try (var producer = new KafkaProducer<String, byte[]>(configs)) {
                // Use alphabetic order
                // https://issues.apache.org/jira/browse/AVRO-2579
                Schema schema = SchemaBuilder.record("SensorData")
                                             .namespace("dev.vepo.maestro.framework.sample.temperature")
                                             .fields()
                                             .name("sensorId").type().stringType().noDefault()
                                             .name("timestamp").type().longType().noDefault()
                                             .name("value").type().doubleType().noDefault()
                                             .endRecord();
                var message1 = new GenericData.Record(schema);
                var sensorId = UUID.randomUUID().toString();
                var value = RandomUtils.nextDouble();
                var timestamp = Instant.now();
                message1.put("sensorId", sensorId);
                message1.put("value", value);
                message1.put("timestamp", timestamp.toEpochMilli());
                logger.info("Message: {}", message1);
                var writer = new GenericDatumWriter<GenericRecord>(schema);
                try (var stream = new ByteArrayOutputStream()) {
                    var encoder = EncoderFactory.get().binaryEncoder(stream, null);
                    writer.write(message1, encoder);
                    encoder.flush();
                    var maybeMetadata = producer.send(new ProducerRecord<String, byte[]>("temperature", message1.get("sensorId").toString(), stream.toByteArray()));
                    logger.info("Metadata: {}", maybeMetadata.get());
                }

                await().until(() -> Files.exists(Paths.get(folder.toAbsolutePath().toString(), "messages.txt")));
                var lines = Files.readAllLines(Paths.get(folder.toAbsolutePath().toString(),
                                                         "messages.txt"));
                assertThat(lines).hasSize(1).contains(new SensorData(sensorId, value, timestamp).toString());
            }
        }
    }

    private MaestroApplication startApp(Class<?> appClass) {
        System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
        MaestroApplication maestroApplication = new MaestroApplication();
        Executors.newSingleThreadExecutor()
                 .submit(() -> {
                     maestroApplication.run(appClass);
                 });
        return maestroApplication;
    }
}
