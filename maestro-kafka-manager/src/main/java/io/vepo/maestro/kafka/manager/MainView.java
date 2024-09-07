package io.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.components.SessionService;
import jakarta.inject.Inject;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends MaestroScreen {

    @Inject
    SessionService sessionService;

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    protected Component buildContent() {
        return new VerticalLayout();
    }

}
