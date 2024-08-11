package io.vepo.maestro.kafka.manager;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;

@Route("kafka/:clusterId([1-9][0-9]*)")
public class KafkaClusterView extends MaestroScreen {

    private List<String> fetchKafkaTopics(Long clusterId) {
        // Implement the logic to fetch Kafka topics based on the clusterId
        // This is a placeholder implementation
        return List.of("Topic1", "Topic2", "Topic3");
    }

    @Override
    protected String getTitle() {
        return "Kafka Cluster View";
    }

    @Override
    protected Component buildContent() {
        return getClusterId().map(clusterId -> {
            var topicGrid = new Grid<>(String.class);
            topicGrid.addColumn(topic -> topic).setHeader("Topic Name");
            List<String> topics = fetchKafkaTopics(clusterId);
            topicGrid.setItems(topics);
            return topicGrid;
        }).orElseGet(() -> {
            getUI().ifPresent(ui -> ui.navigate(""));
            Notification.show("Cluster not found");
            return new Grid<>();
        });
    }
}
