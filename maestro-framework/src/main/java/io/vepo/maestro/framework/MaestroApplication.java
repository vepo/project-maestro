package io.vepo.maestro.framework;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.framework.annotations.KafkaCluster;
import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.exceptions.StartupException;
import io.vepo.maestro.framework.parallel.WorkerThreadFactory;
import io.vepo.maestro.framework.utils.Consumers;
import io.vepo.maestro.framework.utils.Topics;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Starts the Maestro Application.
 */
public class MaestroApplication implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroApplication.class.getName());

    /**
     * Runs the application with the given class.
     * 
     * @param applicationClass the class to start the application. It should contain
     *                         the main configuration for Maestro and will load all
     *                         classes annotated with @MaestroConsumer.
     */
    public static void runApplication(Class<?> applicationClass) {
        try (var app = new MaestroApplication()) {
            app.run(applicationClass);
        }
    }

    /**
     * Runs the application with self-discovery enabled. This is a CDI application,
     * which requires a META-INF/beans.xml file in your project.
     * 
     * @see <a href=
     *      "https://jakarta.ee/learn/docs/jakartaee-tutorial/current/cdi/cdi-basic/cdi-basic.html#_configuring_a_cdi_application">Configuring
     *      a CDI Application</a>
     */
    public static void runApplication() {
        try (var app = new MaestroApplication()) {
            app.run();
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
        try {
            container = initializer.initialize();
            start(null);
        } catch (IllegalStateException ise) {
            if (ise.getMessage().startsWith("WELD-ENV-000016:")) {
                logger.error("No beans.xml found. Please, create a META-INF/beans.xml file in your project.");
                throw new StartupException("""
                                           Maestro requires a CDI application. Start your application with a class parameter or create a META-INF/beans.xml file in your project.
                                           Reference: https://jakarta.ee/learn/docs/jakartaee-tutorial/current/cdi/cdi-basic/cdi-basic.html#_configuring_a_cdi_application
                                           """);
            } else {
                throw new StartupException("Unknow error starting the application", ise);
            }
        }
    }

    public void run(Class<?> applicationClass) {
        var initializer = SeContainerInitializer.newInstance();
        try {
            var builder = initializer.addPackages(true, applicationClass.getPackage());
            if (Objects.isNull(getClass().getResource("/META-INF/beans.xml"))) {
                logger.warn("No beans.xml found. Please, create a META-INF/beans.xml file in your project. Loading beans from package {} using reflection.", applicationClass.getPackage());
                var reflections = new Reflections(new ConfigurationBuilder().forPackages(applicationClass.getPackage().getName())
                                                                            .setScanners(Scanners.SubTypes,
                                                                                         Scanners.TypesAnnotated,
                                                                                         Scanners.MethodsAnnotated));

                reflections.getTypesAnnotatedWith(MaestroConsumer.class)
                           .forEach(c -> builder.addBeanClasses(c));
                reflections.getMethodsAnnotatedWith(MaestroConsumer.class)
                           .forEach(m -> builder.addBeanClasses(m.getDeclaringClass()));
                reflections.getTypesAnnotatedWith(KafkaCluster.class)
                           .forEach(c -> builder.addBeanClasses(c));
            }
            container = builder.initialize();
        } catch (IllegalStateException ise) {
            if (ise.getMessage().startsWith("WELD-ENV-000016:")) {
                container = initializer.disableDiscovery()
                                       .addPackages(true, applicationClass.getPackage())
                                       .setClassLoader(applicationClass.getClassLoader()).initialize();
            } else {
                throw new StartupException("Unknow error starting the application", ise);
            }
        }
        start(applicationClass);
    }

    @Override
    public void close() {
        this.running.set(false);
        this.loadedExecutors.forEach(ExecutorService::shutdown);
        this.container.close();
    }

    private record ConsumerDefinition(Bean<?> bean, List<Method> methods) {
    }

    private void start(Class<?> applicationClass) {
        this.running.set(true);
        logger.info("Container initialized. Starting consumers...");
        var consumers = container.getBeanManager()
                                 .getBeans(Object.class)
                                 .stream()
                                 .filter(b -> applicationClass == null || b.getBeanClass().getPackageName().contains(applicationClass.getPackageName()))
                                 .filter(b -> b.getBeanClass().isAnnotationPresent(MaestroConsumer.class))
                                 .map(b -> new ConsumerDefinition(b, Consumers.findConsumerMethods(b.getBeanClass())
                                                                              .stream()
                                                                              .toList()))
                                 .filter(c -> !c.methods.isEmpty())
                                 .toList();
        logger.debug("Found consumers: {}", consumers);
        if (!consumers.isEmpty()) {
            var threadPoll = newFixedThreadPool(consumers.size(), new WorkerThreadFactory("consumers"));
            loadedExecutors.add(threadPoll);
            consumers.stream()
                     .forEach(consumer -> threadPoll.submit(() -> consume(consumer)));
        }
    }

    private void consume(ConsumerDefinition consumerDefinition) {
        var methodLookup = MethodHandles.lookup();
        try {
            var bean = consumerDefinition.bean;
            logger.info("Starting consumer: {}", bean.getBeanClass().getName());

            System.getProperties().entrySet().forEach(entry -> logger.info("Value {}={}", entry.getKey(), entry.getValue()));
            var bootstrapServer = ConfigProvider.getConfig()
                                                .getOptionalValue(String.format("%s.kafka.%s", bean.getBeanClass().getName(), ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), String.class)
                                                .or(() -> ConfigProvider.getConfig()
                                                                        .getOptionalValue(String.format("kafka.%s", ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), String.class))
                                                .orElseThrow(() -> new IllegalArgumentException("Kafka bootstrap server not found"));

            logger.info("Kafka bootstrap server found: {}", bootstrapServer);
            var configs = new Properties();
            configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            configs.put(ConsumerConfig.GROUP_ID_CONFIG, bean.getBeanClass().getName());
            configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, bean.getBeanClass()
                                                                          .getAnnotation(MaestroConsumer.class)
                                                                          .keyDeserializer()
                                                                          .getName());
            configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, bean.getBeanClass()
                                                                            .getAnnotation(MaestroConsumer.class)
                                                                            .valueDeserializer()
                                                                            .getName());
            configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + ".type", bean.getBeanClass());
            configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            logger.info("Creating consumer: {}", configs);
            try (var consumer = new KafkaConsumer<>(configs)) {
                var instance = bean.create(container.getBeanManager()
                                                    .createCreationalContext(null));

                var topics = consumerDefinition.methods.stream()
                                                       .map(Topics::toTopicName)
                                                       .collect(Collectors.toList());
                var methods = consumerDefinition.methods.stream()
                                                        .collect(Collectors.toMap(Topics::toTopicName, m -> {
                                                            try {
                                                                return methodLookup.unreflect(m).bindTo(instance);
                                                            } catch (IllegalAccessException e) {
                                                                throw new IllegalArgumentException("Error creating method handle", e);
                                                            }
                                                        }));
                var signature = consumerDefinition.methods.stream()
                                                          .collect(Collectors.toMap(Topics::toTopicName, m -> m.getParameterTypes()));

                logger.info("Subscribing to topics: {}", topics);
                consumer.subscribe(topics);
                while (running.get()) {
                    var records = consumer.poll(Duration.ofSeconds(1));
                    records.forEach(record -> {
                        logger.info("Received record: {}", record);
                        try {
                            var m = methods.get(record.topic());
                            Class<?>[] parameterTypes = signature.get(record.topic());
                            logger.info("Invoking method: m={} signature={}", m, Arrays.toString(parameterTypes));
                            var parameters = new ArrayList<Object>(parameterTypes.length);
                            for (int i = 0; i < parameterTypes.length; ++i) {
                                logger.info("Parameter type: {}", parameterTypes[i]);
                                if (parameterTypes[i] == Metadata.class) {
                                    logger.info("Setting metadata: {}", record);
                                    parameters.add(new Metadata(record.topic(), record.partition(), record.offset(), record.timestamp()));
                                } else {
                                    parameters.add(parameterTypes[i].cast(record.value()));
                                }
                            }
                            m.invokeWithArguments(parameters);
                        } catch (Exception e) {
                            logger.error("Error invoking method", e);
                        } catch (Throwable error) {
                            System.exit(-1);
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error consuming messages", e);
        }
    }
}
