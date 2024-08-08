package io.vepo.maestro.kafka.manager;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.components.SessionService;
import io.vepo.maestro.kafka.manager.model.Cluster;
import jakarta.inject.Inject;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends MaestroScreen {

    @Inject
    SessionService sessionService;

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    protected Component buildContent() {
        var content = new VerticalLayout();
        // Escolher Cluster
        List<Cluster> allClusters = Cluster.findAll().list();
        if (!allClusters.isEmpty()) {
            var clusterComboBox = new ComboBox<Cluster>("Escolher Cluster");
            clusterComboBox.setItemLabelGenerator(c -> c.name);
            clusterComboBox.setItems(allClusters);
            clusterComboBox.setPlaceholder("Selecione um cluster");
            Optional.ofNullable(sessionService.getClusterId())
                    .ifPresent(clusterId -> clusterComboBox.setValue(allClusters.stream()
                                                                                .filter(cluster -> cluster.id.equals(clusterId))
                                                                                .findFirst()
                                                                                .orElse(null)));
            content.add(clusterComboBox);

            // Lógica para acessar o monitoramento do cluster selecionado
            var btnSelect = new Button("Selecionar", event -> clusterComboBox.getOptionalValue()
                                                                             .ifPresentOrElse(cluster -> {
                                                                                 getUI().ifPresent(ui -> ui.navigate("kafka/" + cluster));
                                                                             }, () -> {
                                                                                 Notification.show("Por favor, selecione um cluster");
                                                                             }));
            clusterComboBox.addValueChangeListener(event -> btnSelect.setEnabled(event.getValue() != null));
            btnSelect.setEnabled(false);
            content.add(btnSelect);

        }
        // Cadastrar Cluster
        content.add(new Button("Cadastrar Novo Cluster", event -> {
            // Navegar para a tela de cadastro de um novo cluster
            getUI().ifPresent(ui -> ui.navigate("kafka"));
        }));
        return content;
    }

}
