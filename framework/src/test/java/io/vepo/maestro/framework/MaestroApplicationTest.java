package io.vepo.maestro.framework;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import io.vepo.maestro.framework.apps.Data;
import io.vepo.maestro.framework.apps.app1.ClassConsumerWithoutTopicApp;
import io.vepo.maestro.framework.apps.app2.ClassConsumerWithTopicApp;
import io.vepo.maestro.framework.serializers.JsonSerializer;

@Testcontainers
class MaestroApplicationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaestroApplicationTest.class.getName());
    @Container
    public KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    private void createTopics(String... topics) {
        var newTopics = Arrays.stream(topics)
                              .map(topic -> new NewTopic(topic, 1, (short) 1))
                              .collect(Collectors.toList());
        try (var admin = AdminClient.create(Map.of(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            admin.createTopics(newTopics);
        }
    }

    private MaestroApplication startApp(Class<?> appClass) {
        System.setProperty(appClass.getName() + ".kafka.bootstrap.servers", kafka.getBootstrapServers());
        MaestroApplication maestroApplication = new MaestroApplication();
        newSingleThreadExecutor().submit(() -> {
            maestroApplication.run(appClass);
        });
        return maestroApplication;
    }

    @Test
    @DisplayName("It should start the application and consume messages with MaestroConsumer")
    void classConsumerWithoutTopicTest() throws InterruptedException, ExecutionException {
        createTopics("consume");
        try (var app = startApp(ClassConsumerWithoutTopicApp.class)) {
            var buffer = ClassConsumerWithoutTopicApp.buffer;
            var configs = new Properties();
            configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            try (var producer = new KafkaProducer<String, Data>(configs)) {
                var maybeMetadata = producer.send(new ProducerRecord<String, Data>("consume", "key", new Data("Hello, World!", 42, 666L)));
                LOGGER.info("Metadata: {}", maybeMetadata.get());
            }
            await().until(() -> buffer.receivedDataSize() == 1);
            assertThat(buffer.pollReceivedData()).hasSize(1)
                                                 .containsExactly(new Data("Hello, World!", 42, 666L));
        }
    }

    @Test
    @DisplayName("It should start the application and consume messages with MaestroConsumer defining the topic name")
    void classConsumerWithTopicTest() throws InterruptedException, ExecutionException {
        createTopics("topic-consume");
        try (var app = startApp(ClassConsumerWithTopicApp.class)) {
            var buffer = ClassConsumerWithTopicApp.buffer;
            var configs = new Properties();
            configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            try (var producer = new KafkaProducer<String, Data>(configs)) {
                var maybeMetadata = producer.send(new ProducerRecord<String, Data>("topic-consume", "key", new Data("Hello, World!", 42, 666L)));
                LOGGER.info("Metadata: {}", maybeMetadata.get());
            }
            await().until(() -> buffer.receivedDataSize() == 1);
            assertThat(buffer.pollReceivedData()).hasSize(1)
                                                 .containsExactly(new Data("Hello, World!", 42, 666L));
        }
    }
}
