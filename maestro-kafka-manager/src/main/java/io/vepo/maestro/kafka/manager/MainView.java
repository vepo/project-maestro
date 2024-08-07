package io.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        // Create a ComboBox for selecting a Kafka cluster
        ComboBox<String> kafkaClusterComboBox = new ComboBox<>("Select Kafka Cluster");
        kafkaClusterComboBox.setItems("Cluster 1", "Cluster 2", "Cluster 3"); // Example items

        // Create a Button to navigate to the Kafka cluster editor view
        Button editClusterButton = new Button("Edit Kafka Cluster");

        // Add event listener to the ComboBox
        kafkaClusterComboBox.addValueChangeListener(event -> {
            String selectedCluster = event.getValue();
            if (selectedCluster != null) {
                // Handle the selection of a Kafka cluster
                // For example, you can display the selected cluster or perform other actions
                System.out.println("Selected Kafka Cluster: " + selectedCluster);
                getUI().ifPresent(ui -> ui.navigate("/kafka/" + selectedCluster));
            }
        });

        // Add event listener to the Button
        editClusterButton.addClickListener(event -> {
            // Navigate to the Kafka cluster editor view
            getUI().ifPresent(ui -> ui.navigate("/kafka"));
        });

        // Add components to the layout
        add(kafkaClusterComboBox, editClusterButton);

    }
}
