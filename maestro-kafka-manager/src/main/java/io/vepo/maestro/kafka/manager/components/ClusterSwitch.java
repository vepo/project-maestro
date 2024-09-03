package io.vepo.maestro.kafka.manager.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;

import io.vepo.maestro.kafka.manager.model.Cluster;

public class ClusterSwitch extends Div {

    public ClusterSwitch(Optional<Long> selectedCluster, List<Cluster> availableClusters, Consumer<Cluster> clusterConsumer) {
        if (availableClusters.size() > 0) {
            addClassName("cluster-switch");
            var clusterComboBox = new ComboBox<Cluster>("Escolher Cluster");
            clusterComboBox.setItemLabelGenerator(c -> c.name);
            clusterComboBox.setItems(availableClusters);
            clusterComboBox.setPlaceholder("Selecione um cluster");
            selectedCluster.ifPresent(clusterId -> availableClusters.stream()
                                                                    .filter(c -> c.id == clusterId)
                                                                    .findAny()
                                                                    .ifPresent(clusterComboBox::setValue));
            clusterComboBox.addValueChangeListener(event -> clusterConsumer.accept(event.getValue()));
            add(clusterComboBox);
        }
    }
}
