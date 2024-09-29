package dev.vepo.maestro.kafka.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.vepo.maestro.kafka.manager.cluster.KafkaClusterView;
import dev.vepo.maestro.kafka.manager.infra.security.Authenticator;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver, ComponentEventListener<AbstractLogin.LoginEvent> {
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    final LoginForm loginForm;

    @Inject
    Authenticator authenticator;

    @Inject
    SecurityIdentity identity;

    public LoginView() {
        loginForm = new LoginForm();

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.addLoginListener(this);

        add(new H1("Maestro"), new Div("Control you Kafka Clusters."), loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!identity.isAnonymous()) {
            UI.getCurrent().navigate("/");
        }
        logger.info("Before enter: {}", identity.getRoles());
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }

    @Override
    public void onComponentEvent(AbstractLogin.LoginEvent loginEvent) {
        if (authenticator.authenticate(loginEvent.getUsername(), loginEvent.getPassword())) {
            logger.info("User {} logged in", loginEvent.getUsername());
            UI.getCurrent().navigate("/");
        } else {
            logger.info("User {} failed to login", loginEvent.getUsername());
            loginForm.setError(true);
        }
    }
}
