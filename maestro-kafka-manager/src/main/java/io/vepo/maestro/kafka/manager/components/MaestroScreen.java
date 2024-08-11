package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import io.vepo.maestro.kafka.manager.model.ClusterRepository;
import jakarta.inject.Inject;

public abstract class MaestroScreen extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    @Inject
    ClusterSelector clusterSelector;

    @Inject
    ClusterRepository clusterRepository;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters()
             .get("clusterId")
             .map(Long::valueOf)
             .ifPresent(clusterSelector::select);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        var header = new Div();
        header.addClassName("app-header");
        var maestroMenu = new MaestroMenu(clusterSelector.getSelected());
        header.add(new Div(new Text("Maestro Kafka Manager")),
                   new Div(new Text(getTitle())),
                   new ClusterSwitch(clusterSelector.getSelected(),
                                     clusterRepository.findAll(),
                                     cluster -> {
                                         clusterSelector.select(cluster.id);
                                         maestroMenu.updateSelectedCluster(cluster.id);
                                     }));
        addToNavbar(header);
        addToDrawer(maestroMenu);
        setContent(buildContent());
    }

    protected abstract String getTitle();

    protected abstract Component buildContent();

    protected Optional<Long> getClusterId() {
        return clusterSelector.getSelected();
    }

}
