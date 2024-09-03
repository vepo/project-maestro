package io.vepo.maestro.framework;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
class MaestroApplicationTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    
    @Test
    void testStart() {
        MaestroApplication maestroApplication = new MaestroApplication();
        maestroApplication.start();
        System.out.println(kafka.getBootstrapServers());
    }
}
