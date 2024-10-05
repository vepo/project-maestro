package dev.vepo.maestro.kafka.manager.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@PreserveOnRefresh
@Route("kafka/:clusterId([1-9][0-9]*)")
public class KafkaClusterView extends MaestroScreen {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClusterView.class);

    private KafkaAdminService adminService;

    @Inject
    public KafkaClusterView(KafkaAdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected String getTitle() {
        return maybeCluster().map(c -> String.format("Cluster %s", c.getName()))
                             .orElse("Cluster");
    }

    @Override
    protected Component buildContent() {
        try {
            var nodes = adminService.describeBroker();
            var clusterTable = new EntityTable<>(nodes);
            clusterTable.addColumn("ID").withValue(n -> Integer.toString(n.id())).build()
                        .addColumn("Host").withValue(n -> n.host()).build()
                        .addColumn("Port").withValue(n -> Integer.toString(n.port())).build()
                        .addColumn("Rack").withValue(n -> n.rack()).build()
                        .bind();

            return new VerticalLayout(clusterTable);
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }
}
