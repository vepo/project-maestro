package io.vepo.maestro.kafka.manager;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("kafka/:clusterId")
public class KafkaClusterView extends VerticalLayout implements BeforeEnterObserver {
    private Grid<String> topicGrid;

    public KafkaClusterView() {
        // Initialize the Grid component
        topicGrid = new Grid<>(String.class);
        topicGrid.addColumn(topic -> topic).setHeader("Topic Name");

        // Add the Grid to the layout
        add(topicGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get the clusterId parameter from the route
        String clusterId = event.getRouteParameters().get("clusterId").orElse(null);

        if (clusterId != null) {
            // Fetch the Kafka topics based on the clusterId
            List<String> topics = fetchKafkaTopics(clusterId);

            // Populate the Grid with the fetched topics
            topicGrid.setItems(topics);
        }
    }

    private List<String> fetchKafkaTopics(String clusterId) {
        // Implement the logic to fetch Kafka topics based on the clusterId
        // This is a placeholder implementation
        return List.of("Topic1", "Topic2", "Topic3");
    }

}
