package io.vepo.maestro.kafka.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService.KafkaNode;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.inject.Inject;

@Route("kafka/:clusterId([1-9][0-9]*)")
public class KafkaClusterView extends MaestroScreen {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClusterView.class);

    @Inject
    KafkaAdminService adminService;

    @Override
    protected String getTitle() {
        return maybeCluster().map(c -> String.format("Cluster %s", c.name))
                             .orElse("Cluster");
    }

    @Override
    protected Component buildContent() {
        try {
            var kafkaNodeGrid = new Grid<>(KafkaNode.class, false);
            kafkaNodeGrid.addColumn(KafkaNode::id).setHeader("ID");
            kafkaNodeGrid.addColumn(KafkaNode::host).setHeader("Host");
            kafkaNodeGrid.addColumn(KafkaNode::port).setHeader("Port");
            kafkaNodeGrid.addColumn(KafkaNode::rack).setHeader("Rack");
            var nodes = adminService.describeBroker();
            kafkaNodeGrid.setItems(nodes);
            return new VerticalLayout(kafkaNodeGrid);
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }
}
