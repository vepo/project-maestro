package dev.vepo.maestro.framework.stockprice;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.m;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dev.vepo.maestro.framework.sample.StockPrice;
import dev.vepo.maestro.framework.sample.StockPriceApplication;
import io.vepo.maestro.framework.MaestroApplication;
import io.vepo.maestro.framework.serializers.JsonSerializer;

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
                var configs = new Properties();
                configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
                configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
                try (var producer = new KafkaProducer<String, StockPrice>(configs)) {
                    producer.send(new ProducerRecord<>("stock-prices",
                                                       "ETH-USD",
                                                       new StockPrice("ETH-USD",
                                                                      2613.6016,
                                                                      1727728500000L,
                                                                      "USD",
                                                                      "CCC",
                                                                      "CRYPTOCURRENCY",
                                                                      "REGULAR_MARKET",
                                                                      -1.9350458,
                                                                      "17193248768",
                                                                      2659.2073,
                                                                      2581.9211,
                                                                      -51.572266,
                                                                      2658.1855,
                                                                      "17193248768",
                                                                      "2",
                                                                      "17193248768",
                                                                      "17193248768",
                                                                      "ETH",
                                                                      1.20367968E8,
                                                                      3.14593903E11)));
                }
                // await().until(() -> StreamSupport.stream(mongoClient.listDatabaseNames()
                //                                                     .spliterator(),
                //                                          false)
                //                                 //  .peek(db -> System.out.println(db))
                //                                  .anyMatch(db -> db.equals("stocks")));
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
