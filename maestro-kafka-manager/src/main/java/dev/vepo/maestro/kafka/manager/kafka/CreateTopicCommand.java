package dev.vepo.maestro.kafka.manager.kafka;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.NewTopic;

public record CreateTopicCommand(String name, int partitions, int replicationFactor, List<Config> configs) {

    public NewTopic toNewTopic() {
        return new NewTopic(name, partitions, (short) replicationFactor).configs(configs.stream()
                                                                                        .collect(Collectors.toMap(Config::key,
                                                                                                                  Config::value)));
    }

}
