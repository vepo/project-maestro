package dev.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.components.SessionService;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
public class MainView extends MaestroScreen {

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    protected Component buildContent() {
        return new VerticalLayout();
    }

}
