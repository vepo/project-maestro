package io.vepo.maestro.kafka.manager;

import org.apache.kafka.clients.admin.MemberDescription;
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
            consumerGrid.addColumn(ConsumerGroup::id).setHeader("ID").setFlexGrow(0);
            consumerGrid.addColumn(ConsumerGroup::type).setHeader("Type").setFlexGrow(0);
            consumerGrid.addColumn(ConsumerGroup::state).setHeader("State").setFlexGrow(0);
            consumerGrid.addColumn(ConsumerGroup::coordinator).setHeader("Coordinator").setFlexGrow(0);
            consumerGrid.addComponentColumn(c -> {
                var memberGrid = new Grid<MemberDescription>();
                memberGrid.addColumn(MemberDescription::consumerId).setHeader("Consumer ID");
                memberGrid.addColumn(MemberDescription::clientId).setHeader("Client ID");
                memberGrid.addColumn(MemberDescription::host).setHeader("Host");
                memberGrid.addColumn(MemberDescription::assignment).setHeader("Assignment");
                memberGrid.setItems(c.members());
                return memberGrid;
            }).setHeader("Members");
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
