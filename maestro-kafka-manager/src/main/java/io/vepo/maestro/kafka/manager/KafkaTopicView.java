package io.vepo.maestro.kafka.manager;

import java.util.List;

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
        return "Kafka Cluster View";
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
            var buttons = new HorizontalLayout();
            var createTopicDialog = new CreateTopicDialog();
            buttons.add(createTopicDialog);
            buttons.add(new Button("Create", evnt -> createTopicDialog.open()));
            var topicGrid = new Grid<>(String.class);
            topicGrid.addColumn(topic -> topic).setHeader("Topic Name");
            List<String> topics = adminService.listTopics();
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
