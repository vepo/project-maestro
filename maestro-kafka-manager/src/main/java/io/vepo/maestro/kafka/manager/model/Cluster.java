package io.vepo.maestro.kafka.manager.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Cluster extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "cluster_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "cluster_id_seq", sequenceName = "cluster_id_seq", allocationSize = 1)
    public Long id;
    public String name;
    public String bootstrapServers;

    public Cluster() {
        this(null, null, null);
    }

    public Cluster(String name, String bootstrapServers) {
        this(null, name, bootstrapServers);
    }

    public Cluster(Long id, String name, String bootstrapServers) {
        this.id = id;
        this.name = name;
        this.bootstrapServers = bootstrapServers;
    }

    @Override
    public String toString() {
        return String.format("Cluster [id=%d, name=%s, bootstrapServers=%s]", id, name, bootstrapServers);
    }

}
