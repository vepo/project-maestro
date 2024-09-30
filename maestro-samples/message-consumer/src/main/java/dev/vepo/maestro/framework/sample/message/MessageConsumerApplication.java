package dev.vepo.maestro.framework.sample.message;

import io.vepo.maestro.framework.annotations.KafkaCluster;

@KafkaCluster(bootstrapServers = "${kafka.bootstrap.servers}")
public class MessageConsumerApplication {

}
