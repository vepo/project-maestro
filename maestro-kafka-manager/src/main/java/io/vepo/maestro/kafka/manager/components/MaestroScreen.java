package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public abstract class MaestroScreen extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    protected abstract String getTitle();

    protected abstract Component buildContent();

    private Optional<String> clusterId;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        clusterId = event.getRouteParameters().get("clusterId");
    }

    protected Optional<String> getClusterId() {
        return clusterId;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        var header = new Div();
        header.add(new H1("Maestro Kafka Manager"), new H1(getTitle()));
        addToNavbar(header);
        addToDrawer(new MaestroMenu(getClusterId()));
        setContent(buildContent());
    }

}
