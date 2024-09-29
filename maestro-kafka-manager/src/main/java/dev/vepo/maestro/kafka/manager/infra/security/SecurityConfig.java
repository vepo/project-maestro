package dev.vepo.maestro.kafka.manager.infra.security;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.NavigationAccessControl;

import dev.vepo.maestro.kafka.manager.LoginView;

public class SecurityConfig implements VaadinServiceInitListener {

    private NavigationAccessControl accessControl;

    public SecurityConfig() {
        accessControl = new NavigationAccessControl();
        accessControl.setLoginView(LoginView.class);
    }

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource()
                        .addUIInitListener(uiInitEvent -> uiInitEvent.getUI()
                                                                     .addBeforeEnterListener(accessControl));
    }
}
