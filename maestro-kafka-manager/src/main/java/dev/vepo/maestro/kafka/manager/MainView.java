package dev.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.ClusterRepository;
import dev.vepo.maestro.kafka.manager.tcp.TcpCheck;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
@PreserveOnRefresh
@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
public class MainView extends MaestroScreen {

    private ClusterRepository clusterRepository;

    @Inject
    public MainView(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    @Override
    protected String getTitle() {
        return "Maestro";
    }

    @Override
    protected Component buildContent() {
        var clusters = clusterRepository.findAll();
        var table = new EntityTable<>(clusters).addColumn("Cluster #")
                                               .withValue(cluster -> Long.toString(cluster.getId()))
                                               .build()
                                               .addColumn("Name")
                                               .withValue(Cluster::getName)
                                               .build()
                                               .addColumn("Status")
                                               .withComponent(c -> {
                                                   if (TcpCheck.fromKafkaBootstrapServers(c.getBootstrapServers()).anyMatch(TcpCheck::isListening)) {
                                                       var okIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
                                                       okIcon.setClassName("icon ok");
                                                       return okIcon;
                                                   } else {
                                                       var nokIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
                                                       nokIcon.setClassName("icon nok");
                                                       return nokIcon;
                                                   }
                                               })
                                               .build()
                                               .bind();
        return new VerticalLayout(table);
    }

}
