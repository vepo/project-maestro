package io.vepo.maestro.kafka.manager.kafka;

import java.util.List;

public record CreateTopicCommand(String name, int partitions, int replicationFactor, List<Config> configs) {

}
