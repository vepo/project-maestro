package dev.vepo.maestro.framework.sample.temperature;

import io.vepo.maestro.framework.MaestroApplication;
import io.vepo.maestro.framework.annotations.KafkaCluster;

@KafkaCluster(bootstrapServers = "${kafka.bootstrap.servers}")
public class TemperatureSensorApplication {
    public static void main(String[] args) {
        try (var app = new MaestroApplication()) {
            app.run();
        }
    }
}