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
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService.ConsumerGroup;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.inject.Inject;

@Route("kafka/:clusterId([1-9][0-9]*)/consumers")
public class KafkaConsumerView extends MaestroScreen {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerView.class);
    @Inject
    KafkaAdminService adminService;

    @Override
    protected String getTitle() {
        return maybeCluster().map(c -> String.format("Consumers %s", c.name))
                             .orElse("Consumers");
    }

    @Override
    protected Component buildContent() {
        try {
            var consumerGrid = new Grid<>(ConsumerGroup.class, false);
            consumerGrid.addColumn(ConsumerGroup::id).setHeader("ID");
            consumerGrid.addColumn(ConsumerGroup::type).setHeader("Type");
            consumerGrid.addColumn(ConsumerGroup::state).setHeader("State");
            var consumers = adminService.listConsumers();
            consumerGrid.setItems(consumers);
            return new VerticalLayout(consumerGrid);
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }
}
