package dev.vepo.maestro.kafka.manager.cluster.management;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.model.ClusterRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed(Roles.ADMIN)
@Route("kafka")
public class KafkaClusterConnection extends MaestroScreen {

    public static PageParent page(MaestroScreen currentPage) {
        return new PageParent("Connections", KafkaClusterConnection.class, RouteParameters.empty());
    }

    private EntityTable<KafkaCluster> table;
    private final ClusterRepository clusterRepository;

    @Inject
    public KafkaClusterConnection(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;

    }

    @Override
    protected String getTitle() {
        return "Connections";
    }

    @Override
    protected Component buildContent() {
        var layout = new VerticalLayout();
        table = new EntityTable<>(loadClusters());
        table.addColumn("Cluster #")
             .withValue(cluster -> Long.toString(cluster.getId().orElse(0l)))
             .build()
             .addColumn("Name")
             .withValue(KafkaCluster::getName)
             .build()
             .addColumn("Bootstrap Servers")
             .withValue(KafkaCluster::getBootstrapServers)
             .build()
             .addColumn("Protocol")
             .withValue(c -> c.getProtocol().name())
             .build()
             .addColumn("Actions")
             .withComponent(cluster -> new HorizontalLayout(new Button("Edit", event -> edit(cluster)),
                                                            new Button("Delete", event -> delete(cluster))))
             .build()
             .bind();
        layout.add(createActionButton(), table);
        return layout;
    }

    private void delete(KafkaCluster cluster) {
        clusterRepository.delete(cluster.getId().get());
        table.update(loadClusters());
    }

    private void edit(KafkaCluster cluster) {
        getUI().ifPresent(ui -> ui.navigate(EditKafkaClusterConnection.class,
                                            new RouteParameters(Map.of("clusterId", cluster.getId().get().toString()))));
    }

    private Component createActionButton() {
        var layout = new HorizontalLayout();
        // layout.add(new Button("New Cluster", event -> showForm(new KafkaCluster())));
        return layout;
    }

    private List<KafkaCluster> loadClusters() {
        return clusterRepository.findAll()
                                .stream()
                                .map(cluster -> new KafkaCluster(cluster)).toList();
    }

}
