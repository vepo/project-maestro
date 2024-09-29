package dev.vepo.maestro.kafka.manager.infra.security;

import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.evidence.PasswordGuessEvidence;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.util.ModularCrypt;

import dev.vepo.maestro.kafka.manager.access.model.UserRepository;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KafkaManagerIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Inject
    UserRepository userRepository;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext authenticationRequestContext) {

        return authenticationRequestContext.runBlocking(() -> {
            try {
                String username = request.getUsername();
                var user = userRepository.findByName(username)
                                         .orElseThrow(AuthenticationFailedException::new);
                var builder = checkPassword(ModularCrypt.decode(user.getPassword()), request);
                builder.addRole(user.getRole());
                return builder.build();
            } catch (Exception e) {
                throw new AuthenticationFailedException();
            }
        });
    }

    protected QuarkusSecurityIdentity.Builder checkPassword(Password storedPassword, UsernamePasswordAuthenticationRequest request) {
        String username = request.getUsername();
        PasswordGuessEvidence sentPasswordEvidence = new PasswordGuessEvidence(request.getPassword().getPassword());
        PasswordCredential storedPasswordCredential = new PasswordCredential(storedPassword);
        if (!storedPasswordCredential.verify(sentPasswordEvidence)) {
            throw new AuthenticationFailedException();
        }
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
        builder.setPrincipal(new QuarkusPrincipal(username));
        builder.addCredential(request.getPassword());
        return builder;
    }
}