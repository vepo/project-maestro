package io.vepo.maestro.kafka.manager.components;

import com.vaadin.quarkus.annotation.VaadinSessionScoped;

@VaadinSessionScoped
public class SessionService {

    private Long clusterId;

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getClusterId() {
        return clusterId;
    }
}
