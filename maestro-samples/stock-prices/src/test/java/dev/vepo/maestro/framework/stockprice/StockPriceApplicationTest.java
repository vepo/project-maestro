package dev.vepo.maestro.framework.stockprice;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dev.vepo.maestro.framework.sample.StockPriceApplication;
import io.vepo.maestro.framework.MaestroApplication;
import java.util.stream.StreamSupport;

@Testcontainers
class StockPriceApplicationTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Container
    public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeEach
    void setup() {
        System.setProperty("jnosql.document.database", "stocks");
        System.setProperty("jnosql.mongodb.host", mongoDBContainer.getConnectionString());
        System.setProperty("jnosql.document.provider", "org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfiguration");
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
    void consumeMessages() {
        try (MaestroApplication maestroApplication = startApp(StockPriceApplication.class)) {
            createTopics("stock-prices");
            try (MongoClient mongoClient = MongoClients.create(mongoDBContainer.getConnectionString())) {
                await().until(() -> StreamSupport.stream(mongoClient.listDatabaseNames().spliterator(), false).anyMatch(db -> db.equals("stock-prices")));
            }
        }
        MaestroApplication.runApplication(StockPriceApplication.class);
    }

    private MaestroApplication startApp(Class<?> appClass) {
        System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
        MaestroApplication maestroApplication = new MaestroApplication();
        newSingleThreadExecutor().submit(() -> {
            maestroApplication.run(appClass);
        });
        return maestroApplication;
    }
}
