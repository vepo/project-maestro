package io.vepo.maestro.kafka.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.dialogs.CreateTopicDialog;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService.KafkaTopic;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import io.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.inject.Inject;

@Route("kafka/:clusterId([1-9][0-9]*)/topics")
public class KafkaTopicView extends MaestroScreen {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicView.class);

    @Inject
    KafkaAdminService adminService;

    @Override
    protected String getTitle() {
        return maybeCluster().map(c -> String.format("Topics %s", c.name))
                             .orElse("Topics");
    }

    @Override
    protected Component buildContent() {
        return getClusterId().map(this::build)
                             .orElseGet(() -> {
                                 getUI().ifPresent(ui -> ui.navigate(""));
                                 Notification.show("Cluster not found");
                                 return new Grid<>();
                             });
    }

    private Component build(Long clusterId) {
        try {
            var topicGrid = new Grid<>(KafkaTopic.class, false);

            var buttons = new HorizontalLayout();
            var createTopicDialog = new CreateTopicDialog(command -> {
                try {
                    logger.info("Creating topic {}", command);
                    adminService.createTopic(command);
                    topicGrid.setItems(adminService.listTopics());
                } catch (KafkaUnavailableException kue) {
                    logger.warn("Kafka Cluster is not available!", kue);
                } catch (KafkaUnexpectedException kue) {
                    logger.error("Kafka Cluster is not good...", kue);
                }
            });
            buttons.add(createTopicDialog);
            buttons.add(new Button("Create", evnt -> createTopicDialog.open()));
            topicGrid.addColumn(KafkaTopic::id).setHeader("ID");
            topicGrid.addColumn(KafkaTopic::name).setHeader("Topic Name");
            topicGrid.addColumn(KafkaTopic::internal).setHeader("Internal");
            topicGrid.addComponentColumn(topic -> {
                var btnDelete = new Button("Delete", evnt -> {
                    try {
                        adminService.deleteTopic(topic.name());
                        topicGrid.setItems(adminService.listTopics());
                    } catch (KafkaUnavailableException kue) {
                        logger.warn("Kafka Cluster is not available!", kue);
                    } catch (KafkaUnexpectedException kue) {
                        logger.error("Kafka Cluster is not good...", kue);
                    }
                });
                var btnListen = new Button("Listen", evnt -> {
                    getUI().ifPresent(ui -> ui.navigate("kafka/" + clusterId + "/topics/" + topic.name()));
                });
                return new HorizontalLayout(btnDelete, btnListen);
            });
            var topics = adminService.listTopics();
            topicGrid.setItems(topics);
            return new VerticalLayout(buttons, topicGrid);
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }

}
