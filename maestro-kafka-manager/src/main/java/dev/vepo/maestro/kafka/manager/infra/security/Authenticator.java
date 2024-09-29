package dev.vepo.maestro.kafka.manager.infra.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

import dev.vepo.maestro.kafka.manager.LoginView;
import io.agroal.api.security.NamePrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;

@ApplicationScoped
public class Authenticator {

    @Inject
    Instance<SecurityIdentitySupplier> identitySupplierInstance;

    public void logout() {
        UI.getCurrent().navigate(LoginView.class);
        VaadinSession.getCurrent().getSession().invalidate();
    }

    public boolean authenticate(String username, String password) {

        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        if (request == null) {
            // This is in a background thread and we can't access the request to
            // log in the user
            return false;
        }
        try {
            request.login(username, password);

            SecurityIdentitySupplier identitySupplier = identitySupplierInstance.get();
            identitySupplier.setIdentity(QuarkusSecurityIdentity.builder().setPrincipal(new NamePrincipal(username)).build());
            return true;
        } catch (ServletException e) {
            // login exception handle code omitted
            return false;
        }
    }

}
