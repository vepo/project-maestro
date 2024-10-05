package dev.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.vepo.maestro.kafka.manager.infra.security.Authenticator;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;

@Route("login")
@PreserveOnRefresh
@AnonymousAllowed
@PageTitle("Maestro")
public class LoginView extends VerticalLayout implements BeforeEnterObserver, ComponentEventListener<AbstractLogin.LoginEvent> {
    final LoginForm loginForm;

    private Authenticator authenticator;

    private SecurityIdentity identity;

    @Inject
    public LoginView(Authenticator authenticator, SecurityIdentity identity) {
        this.authenticator = authenticator;
        this.identity = identity;
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
            event.forwardTo("/");
        }
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }

    @Override
    public void onComponentEvent(AbstractLogin.LoginEvent loginEvent) {
        if (authenticator.authenticate(loginEvent.getUsername(), loginEvent.getPassword())) {
            UI.getCurrent().getPage().setLocation("/");
        } else {
            loginForm.setError(true);

        }
    }
}
