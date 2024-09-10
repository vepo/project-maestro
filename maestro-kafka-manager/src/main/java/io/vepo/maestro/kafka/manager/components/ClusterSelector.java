package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.kafka.manager.model.Cluster;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
public class ClusterSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterSelector.class);
    private AtomicLong selected = new AtomicLong(-1l);

    public void select(Long clusterId) {
        LOGGER.info("Selecting cluster {}", clusterId);
        this.selected.set(clusterId);
    }

    public Optional<Long> getSelected() {
        return Optional.of(selected.get()).filter(id -> id != -1);
    }

    public Optional<Cluster> getSelectedCluster() {
        LOGGER.info("Getting selected cluster: {}", selected);
        return getSelected().map(id -> Cluster.findById(id));
    }

}
