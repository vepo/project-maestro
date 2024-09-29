package dev.vepo.maestro.kafka.manager.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.maestro.kafka.manager.model.Cluster;

public class TopicConsumer implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicConsumer.class);
    private final Cluster cluster;
    private ExecutorService currenExecutor;
    private AtomicBoolean running;

    public TopicConsumer(Cluster cluster) {
        this.cluster = cluster;
        this.running = new AtomicBoolean(false);
    }

    @Override
    public void close() {
        if (Objects.nonNull(currenExecutor)) {
            running.set(false);
            try {
                if (!currenExecutor.awaitTermination(1500, TimeUnit.MILLISECONDS)) {
                    currenExecutor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                LOGGER.error("Error closing consumer", ex);
                currenExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

        }
    }

    public <K, V> void start(String topicName, String keySerializer, String valueSerializer, Consumer<ConsumerRecords<K, V>> messageConsumer) {
        if (Objects.isNull(currenExecutor)) {
            currenExecutor = Executors.newSingleThreadExecutor();
        }
        currenExecutor.submit(() -> {
            try {
                LOGGER.info("Starting to listen topic: {} - {}", topicName, cluster);
            } catch (Exception ex) {
                LOGGER.error("Error listening topic: {}", topicName, ex);
            }

            this.running.set(true);

            try {
                LOGGER.info("Starting to listen topic: {} on cluster: {}", topicName, cluster);
                var configs = new Properties();
                configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBootstrapServers());
                configs.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-" + topicName + "-" + System.currentTimeMillis());
                configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keySerializer);
                configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueSerializer);
                configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                LOGGER.info("Starting consumer with configs: {}", configs);
                try (var consumer = new KafkaConsumer<K, V>(configs)) {
                    consumer.subscribe(List.of(topicName));
                    while (running.get()) {
                        var records = consumer.poll(Duration.ofSeconds(1));
                        messageConsumer.accept(records);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error listening topic: {}", topicName, ex);
                }

                LOGGER.info("Stopped listening topic: {}", topicName);
            } catch (Exception ex) {
                LOGGER.error("Error listening topic: {}", topicName, ex);
            }
            this.running.set(false);
        });
    }
}
