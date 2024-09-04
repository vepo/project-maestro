package io.vepo.maestro.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Bean;

public class MaestroApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaestroApplication.class.getName());

    public void run() {
        var initializer = SeContainerInitializer.newInstance();
        try (var container = initializer.initialize()) {
            container.select(MaestroConsumer.class)
                     .forEach(consumer -> {
                         System.out.println("Consumer: " + consumer);
                     });
        }
    }

    public void run(Class<?> applicationClass) {
        var initializer = SeContainerInitializer.newInstance();
        try (var container = initializer.initialize()) {
            LOGGER.info("Container initialized. Starting consumers...");
            var consumers = container.getBeanManager()
                                     .getBeans(Object.class)
                                     .stream()
                                     .filter(b -> b.getBeanClass().isAnnotationPresent(MaestroConsumer.class))
                                     .toList();

            var threadPoll = Executors.newFixedThreadPool(consumers.size());
            consumers.stream()
                     .map(consumer -> threadPoll.submit(() -> consume(consumer, container)))
                     .forEach(f -> {
                         try {
                             f.get();
                         } catch (InterruptedException | ExecutionException e) {
                             LOGGER.error("Error consuming messages", e);
                         }
                     });
            ;

        } catch (Exception e) {
            LOGGER.error("Error starting consumers", e);
        }
    }

    private void consume(Bean<?> consumerBean, SeContainer container) {
        try {
            LOGGER.info("Starting consumer: {}", consumerBean);

            var bootstrapServers = ConfigProvider.getConfig()
                                                 .getOptionalValue(String.format("%s.kafka.bootstrap.servers", consumerBean.getBeanClass().getName()), String.class)
                                                 .or(() -> ConfigProvider.getConfig()
                                                                         .getOptionalValue("kafka.bootstrap.servers", String.class))
                                                 .orElseThrow(() -> new IllegalArgumentException("Kafka bootstrap servers not found"));

            LOGGER.info("Kafka bootstrap servers found: {}", bootstrapServers);
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
            LOGGER.info("Creating consumer: {}", configs);
            var bean = consumerBean.create(container.getBeanManager()
                                                    .createCreationalContext(null));
            try (var consumer = new KafkaConsumer<>(configs)) {
                var topics = Stream.of(consumerBean.getBeanClass()
                                                   .getDeclaredMethods())
                                   .filter(method -> method.getParameterTypes().length == 1)
                                   .map(method -> method.getName())
                                   .toList();
                var methods = Stream.of(consumerBean.getBeanClass().getDeclaredMethods())
                                    .filter(method -> method.getParameterTypes().length == 1)
                                    .collect(Collectors.toMap(Method::getName, Function.identity()));

                LOGGER.info("Subscribing to topics: {}", topics);
                consumer.subscribe(topics);
                while (true) {
                    var records = consumer.poll(Duration.ofSeconds(1));
                    records.forEach(record -> {
                        LOGGER.info("Received record: {}", record);
                        try {
                            var m = methods.get(record.topic());
                            m.invoke(bean, m.getParameterTypes()[0].cast(record.value()));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error consuming messages", e);
        }
    }
}
