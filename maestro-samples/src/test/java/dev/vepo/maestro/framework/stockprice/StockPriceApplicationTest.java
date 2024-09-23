package dev.vepo.maestro.framework.stockprice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import dev.vepo.maestro.framework.sample.StockPriceApplication;
import io.vepo.maestro.framework.MaestroApplication;

@Testcontainers
class StockPriceApplicationTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Test
    void noBeansXmlTest() {
        assertThatThrownBy(() -> MaestroApplication.runApplication()).isInstanceOf(IllegalStateException.class)
                                                                     .hasMessageContaining("Maestro requires a CDI application.")
                                                                     .hasMessageContaining("https://jakarta.ee/learn/docs/jakartaee-tutorial/current/cdi/cdi-basic/cdi-basic.html#_configuring_a_cdi_application");
    }

    @Test
    void beansXmlTest() {
        MaestroApplication.runApplication(StockPriceApplication.class);
    }
}
