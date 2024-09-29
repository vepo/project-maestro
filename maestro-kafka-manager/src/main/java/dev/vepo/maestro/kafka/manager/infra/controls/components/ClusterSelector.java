package dev.vepo.maestro.kafka.manager.infra.controls.components;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.ClusterRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

@SessionScoped
public class ClusterSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterSelector.class);
    private AtomicLong selected = new AtomicLong(-1l);

    @Inject
    ClusterRepository clusterRepository;

    public void select(Long clusterId) {
        LOGGER.info("Selecting cluster {}", clusterId);
        this.selected.set(clusterId);
    }

    public Optional<Long> getSelected() {
        return Optional.of(selected.get()).filter(id -> id != -1);
    }

    public Optional<Cluster> getSelectedCluster() {
        LOGGER.info("Getting selected cluster: {}", selected);
        return getSelected().flatMap(id -> clusterRepository.findById(id));
    }

}
