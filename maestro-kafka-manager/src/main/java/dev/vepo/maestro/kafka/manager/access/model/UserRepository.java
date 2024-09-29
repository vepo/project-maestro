package dev.vepo.maestro.kafka.manager.access.model;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class UserRepository {

    @PersistenceContext
    EntityManager em;

    public Optional<User> authenticate(String username, String hashedPassword) {
        return em.createQuery("""
                              SELECT u FROM User u

                              WHERE u.username = :username AND
                                    u.hashedPassword = :hashedPassword AND
                                    u.active = true
                              """, User.class)
                 .setParameter("username", username)
                 .setParameter("hashedPassword", hashedPassword)
                 .getResultStream()
                 .findFirst();
    }

    public List<User> findAll() {
        return em.createQuery("""
                              SELECT u FROM User u
                              WHERE u.active = true
                              """, User.class)
                 .getResultList();
    }

    public Optional<User> findByName(String username) {
        return em.createQuery("""
                              SELECT u FROM User u

                              WHERE u.username = :username AND
                                    u.active = true
                              """, User.class)
                 .setParameter("username", username)
                 .getResultStream()
                 .findFirst();
    }

}
