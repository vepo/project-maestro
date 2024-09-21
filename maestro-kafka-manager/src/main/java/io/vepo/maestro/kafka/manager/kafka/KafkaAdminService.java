package io.vepo.maestro.kafka.manager.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.kafka.manager.components.ClusterSelector;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class KafkaAdminService {

    public record ConsumerGroup(String id, String type, String state, String coordinator, List<MemberDescription> members) {

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

    public record KafkaTopic(String id, String name, int partitions, int replicas, boolean internal) {
        public KafkaTopic(TopicListing topic, TopicDescription description) {
            this(topic.topicId().toString(), topic.name(), description.partitions().size(), description.partitions().get(0).replicas().size(), topic.isInternal());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);

    private static KafkaResponse<Map<String, TopicDescription>, KafkaUnexpectedException> describeTopics(AdminClient client, List<String> topicNames) {
        try {
            return new KafkaResponse<>(client.describeTopics(topicNames).allTopicNames().get(500, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null; // this line is never executed!
        } catch (ExecutionException e) {
            return new KafkaResponse<>(new KafkaUnexpectedException(e));
        } catch (TimeoutException e) {
            return new KafkaResponse<>(new KafkaUnavailableException("Could not connecto with Kafka Brokers!", e));
        }
    }

    private static KafkaResponse<Map<String, ConsumerGroupDescription>, KafkaUnexpectedException> describeConsumerGroups(AdminClient client, List<String> groupIds) {
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

    @Inject
    ClusterSelector clusterSelector;

    private Optional<AdminClient> client;

    public List<KafkaTopic> listTopics() throws KafkaUnexpectedException {
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
    }

    public List<ConsumerGroup> listConsumers() throws KafkaUnexpectedException {
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
    }

    public List<KafkaNode> describeBroker() throws KafkaUnexpectedException {
        return client.map(KafkaAdminService::describeClusterInternal)
                     .get()
                     .getOrThrow()
                     .stream()
                     .map(KafkaNode::new)
                     .toList();
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

    @PostConstruct
    void setup() {
        client = clusterSelector.getSelectedCluster()
                                .map(cluster -> {
                                    var adminProperties = new Properties();
                                    adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers);
                                    // adminProperties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1500);
                                    // adminProperties.put(AdminClientConfig.RETRIES_CONFIG, 0);
                                    return AdminClient.create(adminProperties);
                                });
    }

    @PreDestroy
    void cleanup() {
        client.ifPresent(AdminClient::close);
    }
}
