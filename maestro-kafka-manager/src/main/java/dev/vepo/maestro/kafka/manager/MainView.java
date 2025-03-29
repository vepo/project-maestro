package dev.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.components.TcpStatusComponent;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.model.Cluster;
import jakarta.annotation.security.RolesAllowed;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
@PreserveOnRefresh
@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
public class MainView extends MaestroScreen {

    public static PageParent page(MaestroScreen currentPage) {
        return new PageParent("Maestro", MainView.class, RouteParameters.empty());
    }

    @Override
    protected String getTitle() {
        return "Maestro";
    }

    @Override
    protected Component buildContent() {
        return new VerticalLayout(new EntityTable<>(allClusters()).addColumn("Cluster #")
                                                                  .withValue(cluster -> Long.toString(cluster.getId()))
                                                                  .build()
                                                                  .addColumn("Name")
                                                                  .withValue(Cluster::getName)
                                                                  .build()
                                                                  .addColumn("Status")
                                                                  .withComponent(c -> new TcpStatusComponent(c.getBootstrapServers()))
                                                                  .build()
                                                                  .bind());
    }

}
