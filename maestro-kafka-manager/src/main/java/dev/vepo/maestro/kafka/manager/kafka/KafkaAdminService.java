package dev.vepo.maestro.kafka.manager.kafka;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.GroupType;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.maestro.kafka.manager.infra.controls.components.ClusterSelector;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnaccessibleException;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import dev.vepo.maestro.kafka.manager.model.SslCredentials;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class KafkaAdminService {

    public record ConsumerGroup(String id, String type, String state, String coordinator,
                                List<MemberDescription> members) {

        public ConsumerGroup(ConsumerGroupListing group, ConsumerGroupDescription description) {
            this(group.groupId(),
                 group.type().map(GroupType::name).orElse(description.type().name()),
                 group.state().map(ConsumerGroupState::name).orElse(description.state().name()),
                 description.coordinator().host(),
                 description.members().stream().toList());
        }
    }

    public record KafkaNode(int id, String host, int port, String rack) {
        public KafkaNode(Node node) {
            this(node.id(), node.host(), node.port(), node.rack());
        }
    }

    public record KafkaPartition(int id, Integer leader, List<Integer> replicas, List<Integer> isr) {

        public KafkaPartition(TopicPartitionInfo partition) {
            this(partition.partition(), partition.leader().id(), partition.replicas().stream().map(Node::id).toList(),
                 partition.isr().stream().map(Node::id).toList());
        }
    }

    public record KafkaTopic(String id, String name, int replicas, List<KafkaPartition> partitions, boolean internal) {
        public KafkaTopic(TopicListing topic, TopicDescription description) {
            this(topic.topicId().toString(), topic.name(), description.partitions().get(0).replicas().size(),
                 description.partitions().stream().map(KafkaPartition::new).toList(), topic.isInternal());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);

    private static KafkaResponse<Map<String, TopicDescription>, KafkaUnexpectedException> describeTopics(AdminClient client,
                                                                                                         List<String> topicNames) {
        try {
            return new KafkaResponse<>(client.describeTopics(topicNames).allTopicNames().get(500,
                                                                                             TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private static KafkaResponse<Map<String, ConsumerGroupDescription>, KafkaUnexpectedException> describeConsumerGroups(AdminClient client,
                                                                                                                         List<String> groupIds) {
        try {
            return new KafkaResponse<>(client.describeConsumerGroups(groupIds).all().get(500, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private static KafkaResponse<Collection<ConsumerGroupListing>, KafkaUnexpectedException> listConsumersInternal(AdminClient client) {
        try {
            // client.describeConsumerGroups()
            return new KafkaResponse<>(client.listConsumerGroups()
                                             .all()
                                             .get(500, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private static KafkaResponse<Collection<TopicListing>, KafkaUnexpectedException> listTopicsInternal(AdminClient client) {
        try {
            return new KafkaResponse<>(client.listTopics().listings().get(500, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private static KafkaResponse<Collection<Node>, KafkaUnexpectedException> describeClusterInternal(AdminClient client) {
        try {
            return new KafkaResponse<>(client.describeCluster().nodes().get(500, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private final Optional<AdminClient> client;

    @Inject
    public KafkaAdminService(ClusterSelector clusterSelector) {
        client = clusterSelector.getSelectedCluster()
                                .map(cluster -> {
                                    var adminProperties = new Properties();
                                    adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                                                        cluster.getBootstrapServers());
                                    switch (cluster.getProtocol()) {
                                        case PLAINTEXT:
                                            adminProperties.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG,
                                                                "PLAINTEXT");
                                            break;
                                        case SSL:
                                            adminProperties.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG,
                                                                "SSL");
                                            SslCredentials accessSslCredentials = cluster.getAccessSslCredentials();
                                            if (Objects.nonNull(accessSslCredentials)) {
                                                accessSslCredentials.getTemporaryTruststore()
                                                                    .ifPresent(file -> {
                                                                        adminProperties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                                                                                            file.getAbsolutePath());
                                                                    });
                                                if (Objects.nonNull(accessSslCredentials.getTruststorePassword())) {
                                                    adminProperties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                                                                        accessSslCredentials.getKeystorePassword());
                                                }
                                                accessSslCredentials.getTemporaryKeystore()
                                                                    .ifPresent(file -> {
                                                                        adminProperties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                                                                                            file.getAbsolutePath());
                                                                    });
                                                if (Objects.nonNull(accessSslCredentials.getKeystorePassword())) {
                                                    adminProperties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                                                                        accessSslCredentials.getKeystorePassword());
                                                }

                                                if (Objects.nonNull(accessSslCredentials.getKeyPassword())) {
                                                    adminProperties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,
                                                                        accessSslCredentials.getKeyPassword());
                                                }
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    adminProperties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1500);
                                    adminProperties.put(AdminClientConfig.RETRIES_CONFIG, 0);
                                    try {
                                        return AdminClient.create(adminProperties);
                                    } catch (KafkaException ke) {
                                        throw new KafkaUnaccessibleException("Could not access Kafka Brokers!", ke);
                                    }
                                });
    }

    public List<KafkaTopic> listTopics() throws KafkaUnexpectedException {
        if (client.isPresent()) {
            var topics = client.map(KafkaAdminService::listTopicsInternal)
                               .get()
                               .getOrThrow()
                               .stream()
                               .toList();
            var descriptions = client.map(c -> describeTopics(c, topics.stream()
                                                                       .map(TopicListing::name)
                                                                       .toList()))
                                     .get()
                                     .getOrThrow();
            return topics.stream()
                         .map(topic -> new KafkaTopic(topic, descriptions.get(topic.name())))
                         .toList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<ConsumerGroup> listConsumers() throws KafkaUnexpectedException {
        if (client.isPresent()) {
            var groups = client.map(KafkaAdminService::listConsumersInternal)
                               .get()
                               .getOrThrow()
                               .stream()
                               .toList();
            var descriptions = client.map(c -> describeConsumerGroups(c, groups.stream()
                                                                               .map(ConsumerGroupListing::groupId)
                                                                               .toList()))
                                     .get()
                                     .getOrThrow();
            return groups.stream()
                         .map(group -> new ConsumerGroup(group, descriptions.get(group.groupId())))
                         .toList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<KafkaNode> describeBroker() throws KafkaUnexpectedException {
        if (client.isPresent()) {
            return client.map(KafkaAdminService::describeClusterInternal)
                         .get()
                         .getOrThrow()
                         .stream()
                         .map(KafkaNode::new)
                         .toList();
        } else {
            return Collections.emptyList();
        }
    }

    public void deleteTopic(String name) {
        client.ifPresent(client -> {
            try {
                client.deleteTopics(List.of(name)).all().get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.error("Could not delete topic {}", name, e);
            } catch (TimeoutException e) {
                LOGGER.error("Could not delete topic {}", name, e);
            }
        });
    }

    public void createTopic(CreateTopicCommand command) {
        client.ifPresent(client -> {
            try {
                client.createTopics(List.of(command.toNewTopic())).all().get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.error("Could not create topic {}", command, e);
            } catch (TimeoutException e) {
                LOGGER.error("Could not create topic {}", command, e);
            }
        });
    }

    @PreDestroy
    void cleanup() {
        client.ifPresent(AdminClient::close);
    }
}
