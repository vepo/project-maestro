package dev.vepo.maestro.kafka.manager.infra.security;

import java.util.function.Supplier;

import dev.vepo.maestro.kafka.manager.access.model.User;
import dev.vepo.maestro.kafka.manager.access.model.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

@Dependent
class SecurityIdentitySupplier implements Supplier<SecurityIdentity> {

    private SecurityIdentity identity;

    @Inject
    UserRepository userRepository;

    @Override
    @ActivateRequestContext
    public SecurityIdentity get() {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        String user = identity.getPrincipal().getName();

        userRepository.findByName(user)
                      .filter(User::isActive)
                      .ifPresentOrElse(userEntity -> {
                          builder.addRole(userEntity.getRole());
                      }, () -> {
                          builder.setAnonymous(true)
                                 .setPrincipal(null);
                      });

        return builder.build();
    }

    public void setIdentity(SecurityIdentity identity) {
        this.identity = identity;
    }
}
