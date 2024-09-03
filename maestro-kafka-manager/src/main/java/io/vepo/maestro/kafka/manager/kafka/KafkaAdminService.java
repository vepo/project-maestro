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
import org.apache.kafka.clients.admin.TopicListing;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);

    @Inject
    ClusterSelector clusterSelector;

    private Optional<AdminClient> client;

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

    public List<String> listTopics() throws KafkaUnexpectedException {
        return client.map(KafkaAdminService::listTopicsInternal)
                     .get()
                     .getOrThrow()
                     .stream()
                     .map(t -> t.name())
                     .toList();
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
}
