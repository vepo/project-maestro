package dev.vepo.maestro.framework.sample.message;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import io.vepo.maestro.framework.MaestroApplication;

@Testcontainers
class MessageConsumerApplicationTest {
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerApplicationTest.class.getName());

    @Container
    KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Test
    void noBeansXmlTest() {
        assertThatThrownBy(() -> MaestroApplication.runApplication()).isInstanceOf(IllegalStateException.class)
                                                                     .hasMessageContaining("Maestro requires a CDI application.")
                                                                     .hasMessageContaining("https://jakarta.ee/learn/docs/jakartaee-tutorial/current/cdi/cdi-basic/cdi-basic.html#_configuring_a_cdi_application");
    }

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
        try (MaestroApplication maestroApplication = startApp(MessageConsumerApplication.class)) {
            createTopics("messages");
            var configs = new Properties();
            configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            try (var producer = new KafkaProducer<String, String>(configs)) {

                var message1 = UUID.randomUUID().toString();
                var message2 = UUID.randomUUID().toString();
                var maybeMetadata = producer.send(new ProducerRecord<String, String>("messages", "key", message1));

                logger.info("Metadata: {}", maybeMetadata.get());

                maybeMetadata = producer.send(new ProducerRecord<String, String>("messages", "key", message2));

                logger.info("Metadata: {}", maybeMetadata.get());

                await().until(() -> Files.exists(Paths.get(folder.toAbsolutePath().toString(), "messages.txt")));
                var lines = Files.readAllLines(Paths.get(folder.toAbsolutePath().toString(), "messages.txt"));
                assertThat(lines).hasSize(2)
                                 .containsExactly(message1, message2);
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
