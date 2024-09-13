package io.vepo.maestro.kafka.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.ConsumerGroupTable;
import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
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
            return new VerticalLayout(new ConsumerGroupTable(adminService.listConsumers()));
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }
}
