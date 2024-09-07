package io.vepo.maestro.kafka.manager.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
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

    public record ConsumerGroup(String id, String type, String state) {

        public ConsumerGroup(ConsumerGroupListing group) {
            this(group.groupId(),
                 group.type().map(GroupType::name).orElse("UNKNOWN"),
                 group.state().map(ConsumerGroupState::name).orElse("UNKNOWN"));
        }
    }

    public record KafkaNode(int id, String host, int port, String rack) {
        public KafkaNode(Node node) {
            this(node.id(), node.host(), node.port(), node.rack());
        }
    }

    public record KafkaTopic(String id, String name, boolean internal) {
        public KafkaTopic(TopicListing topic) {
            this(topic.topicId().toString(), topic.name(), topic.isInternal());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);

    private static KafkaResponse<Collection<ConsumerGroupListing>, KafkaUnexpectedException> listConsumersInternal(AdminClient client) {
        try {
            return new KafkaResponse<>(client.listConsumerGroups().all().get(500, TimeUnit.MILLISECONDS));
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
        return client.map(KafkaAdminService::listTopicsInternal)
                     .get()
                     .getOrThrow()
                     .stream()
                     .map(KafkaTopic::new)
                     .toList();
    }

    public List<ConsumerGroup> listConsumers() throws KafkaUnexpectedException {
        return client.map(KafkaAdminService::listConsumersInternal)
                     .get()
                     .getOrThrow()
                     .stream()
                     .map(t -> new ConsumerGroup(t))
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

    @PostConstruct
    void setup() {
        client = clusterSelector.getSelectedCluster()
                                .map(cluster -> {
                                    var adminProperties = new Properties();
                                    adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers);
                                    adminProperties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
                                    adminProperties.put(AdminClientConfig.RETRIES_CONFIG, 0);
                                    return AdminClient.create(adminProperties);
                                });
    }

    @PreDestroy
    void cleanup() {
        client.ifPresent(AdminClient::close);
    }
}
