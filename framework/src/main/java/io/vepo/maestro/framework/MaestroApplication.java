package io.vepo.maestro.framework;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.function.Function.identity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.ConfigProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import io.vepo.maestro.framework.parallel.WorkerThreadFactory;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;

public class MaestroApplication implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroApplication.class.getName());

    private static String toTopicName(Method method) {
        if (method.isAnnotationPresent(Topic.class)) {
            return method.getAnnotation(Topic.class).value();
        } else {
            return method.getName();
        }
    }

    private SeContainer container;
    private final List<ExecutorService> loadedExecutors;

    private final AtomicBoolean running;

    public MaestroApplication() {
        loadedExecutors = new ArrayList<>();
        running = new AtomicBoolean(false);
    }

    public void run() {
        var initializer = SeContainerInitializer.newInstance();
        container = initializer.initialize();
        start(null);
    }

    public void run(Class<?> applicationClass) {
        var initializer = SeContainerInitializer.newInstance();
        container = initializer.initialize();
        start(applicationClass);
    }

    @Override
    public void close() {
        this.running.set(false);
        this.loadedExecutors.forEach(ExecutorService::shutdown);
        this.container.close();
    }

    private void start(Class<?> applicationClass) {
        this.running.set(true);
        logger.info("Container initialized. Starting consumers...");
        var consumers = container.getBeanManager()
                                 .getBeans(Object.class)
                                 .stream()
                                 .filter(b -> applicationClass == null || b.getBeanClass().getPackageName().contains(applicationClass.getPackageName()))
                                 .filter(b -> b.getBeanClass().isAnnotationPresent(MaestroConsumer.class))
                                 .toList();

        var threadPoll = newFixedThreadPool(consumers.size(), new WorkerThreadFactory("consumers"));
        loadedExecutors.add(threadPoll);
        consumers.stream()
                 .forEach(consumer -> threadPoll.submit(() -> consume(consumer)));
    }

    private void consume(Bean<?> consumerBean) {
        try {
            logger.info("Starting consumer: {}", consumerBean);

            System.getProperties().entrySet().forEach(entry -> logger.info("Value {}={}", entry.getKey(), entry.getValue()));
            var bootstrapServers = ConfigProvider.getConfig()
                                                 .getOptionalValue(String.format("%s.kafka.bootstrap.servers", consumerBean.getBeanClass().getName()), String.class)
                                                 .or(() -> ConfigProvider.getConfig()
                                                                         .getOptionalValue("kafka.bootstrap.servers", String.class))
                                                 .orElseThrow(() -> new IllegalArgumentException("Kafka bootstrap servers not found"));

            logger.info("Kafka bootstrap servers found: {}", bootstrapServers);
            var configs = new Properties();
            configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configs.put(ConsumerConfig.GROUP_ID_CONFIG, consumerBean.getBeanClass().getName());
            configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerBean.getBeanClass()
                                                                                  .getAnnotation(MaestroConsumer.class)
                                                                                  .keyDeserializer()
                                                                                  .getName());
            configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerBean.getBeanClass()
                                                                                    .getAnnotation(MaestroConsumer.class)
                                                                                    .valueDeserializer()
                                                                                    .getName());
            configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", consumerBean.getBeanClass());
            configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            logger.info("Creating consumer: {}", configs);
            var bean = consumerBean.create(container.getBeanManager()
                                                    .createCreationalContext(null));
            try (var consumer = new KafkaConsumer<>(configs)) {
                var topics = Stream.of(consumerBean.getBeanClass()
                                                   .getDeclaredMethods())
                                   .filter(method -> method.getParameterTypes().length == 1)
                                   .map(method -> toTopicName(method))
                                   .toList();
                var methods = Stream.of(consumerBean.getBeanClass().getDeclaredMethods())
                                    .filter(method -> method.getParameterTypes().length == 1)
                                    .collect(Collectors.toMap(MaestroApplication::toTopicName, identity()));

                logger.info("Subscribing to topics: {}", topics);
                consumer.subscribe(topics);
                while (running.get()) {
                    var records = consumer.poll(Duration.ofSeconds(1));
                    records.forEach(record -> {
                        logger.info("Received record: {}", record);
                        try {
                            var m = methods.get(record.topic());
                            m.invoke(bean, m.getParameterTypes()[0].cast(record.value()));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            logger.error("Error invoking method", e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error consuming messages", e);
        }
    }
}
