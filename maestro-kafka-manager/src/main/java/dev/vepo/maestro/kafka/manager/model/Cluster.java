package dev.vepo.maestro.kafka.manager.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "tbl_clusters")
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(name = "bootstrap_servers", unique = true)
    private String bootstrapServers;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

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
        this.updatedAt = this.createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        Cluster other = (Cluster) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return String.format("Cluster [id=%d, name=%s, bootstrapServers=%s, createdAt=%s, updatedAt=%s]",
                             id, name, bootstrapServers, createdAt, updatedAt);
    }

}