package dev.vepo.maestro.kafka.manager.cluster.management;

import java.util.Optional;

import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.Protocol;

public class KafkaCluster {
    private Optional<Long> id;
    private String name;
    private String bootstrapServers;
    private Protocol protocol;

    public KafkaCluster(Cluster cluster) {
        this(cluster.getId(), cluster.getName(), cluster.getBootstrapServers(), cluster.getProtocol());
    }

    public KafkaCluster() {
        id = Optional.empty();
    }

    private KafkaCluster(Long id, String name, String bootstrapServers, Protocol protocol) {
        this.id = Optional.of(id);
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.protocol = protocol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Optional<Long> getId() {
        return id;
    }

    public void setId(Optional<Long> id) {
        this.id = id;
    }
}
