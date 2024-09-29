package dev.vepo.maestro.kafka.manager.model;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
@RequestScoped
public class ClusterRepository {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void create(Cluster entity) {
        em.persist(entity);
    }

    @Transactional
    public void update(Cluster cluster) {
        em.createQuery("UPDATE tbl_clusters c SET c.name = :name, c.bootstrapServers = :bootstrapServers WHERE c.id = :id")
          .setParameter("name", cluster.getName())
          .setParameter("bootstrapServers", cluster.getBootstrapServers())
          .setParameter("id", cluster.getId())
          .executeUpdate();
    }

    @Transactional
    public void delete(Long clusterId) {
        em.createQuery("DELETE FROM tbl_clusters c WHERE c.id = :id").setParameter("id", clusterId)
          .executeUpdate();
    }

    public List<Cluster> findAll() {
        return em.createQuery("SELECT c FROM tbl_clusters c", Cluster.class)
                 .getResultList();
    }

    public Optional<Cluster> findById(Long id) {
        return Optional.ofNullable(em.find(Cluster.class, id));
    }

}
