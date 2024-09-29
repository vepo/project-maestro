package dev.vepo.maestro.kafka.manager.access.model;

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
        return em.createQuery("SELECT u FROM tbl_users u WHERE u.username = :username AND u.hashedPassword = :hashedPassword", User.class)
                 .setParameter("username", username)
                 .setParameter("hashedPassword", hashedPassword)
                 .getResultStream()
                 .findFirst();
    }

}
