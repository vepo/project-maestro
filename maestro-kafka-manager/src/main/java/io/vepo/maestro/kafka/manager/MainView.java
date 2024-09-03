package io.vepo.maestro.kafka.manager;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.components.SessionService;
import io.vepo.maestro.kafka.manager.model.Cluster;
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
