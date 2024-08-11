package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.SessionScoped;

@SessionScoped
public class ClusterSelector {

    private AtomicLong selected = new AtomicLong(-1l);

    public void select(Long clusterId) {
        this.selected.set(clusterId);
    }

    public Optional<Long> getSelected() {
        return Optional.of(selected.get()).filter(id -> id != -1);
    }

}
