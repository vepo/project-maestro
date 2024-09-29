package dev.vepo.maestro.kafka.manager.infra.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

import dev.vepo.maestro.kafka.manager.LoginView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletException;

@ApplicationScoped
public class Authenticator {

    // @Inject
    // PasswordHash passwordHash;

    // @Inject
    // UserRepository userRepository;

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
            // change session ID to protect against session fixation
            request.getHttpServletRequest().changeSessionId();
            return true;
        } catch (ServletException e) {
            // login exception handle code omitted
            return false;
        }
    }

}
