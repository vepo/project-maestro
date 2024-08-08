package io.vepo.maestro.kafka.manager.model;

import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

@RequestScoped
public class ClusterRepository {

    @Transactional
    public void create(Cluster entity) {
        entity.persist();
    }

    @Transactional
    public void update(Cluster cluster) {
        var dbCluster = Cluster.<Cluster>findById(cluster.id);
        dbCluster.name = cluster.name;
        dbCluster.bootstrapServers = cluster.bootstrapServers;
        dbCluster.persist();
    }

    @Transactional
    public void delete(Long clusterId) {
        Cluster.deleteById(clusterId);
    }

}
