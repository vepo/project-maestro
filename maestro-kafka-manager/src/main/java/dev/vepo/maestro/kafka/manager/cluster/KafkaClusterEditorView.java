package dev.vepo.maestro.kafka.manager.cluster;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.ClusterRepository;
import dev.vepo.maestro.kafka.manager.model.Protocol;
import dev.vepo.maestro.kafka.manager.model.SslCredentials;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed(Roles.ADMIN)
@Route("kafka")
public class KafkaClusterEditorView extends MaestroScreen {
    private class KafkaCluster {
        private Optional<Long> id;
        private String name;
        private String bootstrapServers;
        private Protocol protocol;

        public KafkaCluster(Cluster cluster) {
            this(cluster.getId(), cluster.getName(), cluster.getBootstrapServers(), cluster.getProtocol());
        }

        private KafkaCluster() {
            id = Optional.empty();
        }

        private KafkaCluster(Long id, String name, String bootstrapServers, Protocol protocol) {
            this.id = Optional.of(id);
            this.name = name;
            this.bootstrapServers = bootstrapServers;
            this.protocol = protocol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public Optional<Long> getId() {
            return id;
        }
    }

    private class SslAccessCredentialsForm extends Div {
        private final FormLayout form;
        private final Div naLabel;

        private SslAccessCredentialsForm() {
            form = new FormLayout();
            form.setVisible(false);
            naLabel = new Div(new Text("N/A"));
            add(form, naLabel);

            var upKeystore = new Upload();
            form.addFormItem(upKeystore, "Keystore");

            var txtKeystorePassword = new TextField();
            form.addFormItem(txtKeystorePassword, "Keystore Password");

            var upTruststore = new Upload();
            form.addFormItem(upTruststore, "Truststore");

            var txtTruststorePassword = new TextField();
            form.addFormItem(txtTruststorePassword, "Truststore Password");

            var txtKeyPassword = new TextField();
            form.addFormItem(txtKeyPassword, "Key Password");
        }

        private void showSslComponents() {
            form.setVisible(true);
            naLabel.setVisible(false);
        }

        private void hideSslComponents() {
            form.setVisible(false);
            naLabel.setVisible(true);
        }

        public SslCredentials getCredential() {
            return new SslCredentials();
        }

    }

    private class EditorForm extends VerticalLayout {
        private final Binder<KafkaCluster> binder;
        private final Command focus;
        private final Consumer<Boolean> swithMode;

        private EditorForm() {
            var form = new FormLayout();
            binder = new Binder<>(KafkaCluster.class);
            var txtId = new TextField();
            txtId.setEnabled(false);
            txtId.setReadOnly(true);
            binder.forField(txtId)
                  .bind(cluster -> cluster.getId().map(Object::toString).orElse(""),
                        (cluster, id) -> cluster.id = id != null && !id.isBlank() ? Optional.of(Long.parseLong(id)) : Optional.empty());
            form.addFormItem(txtId, "ID");

            var txtName = new TextField();
            txtName.setValueChangeMode(ValueChangeMode.EAGER);
            focus = txtName::focus;
            binder.forField(txtName)
                  .asRequired("Name cannot be empty")
                  .withValidator(name -> name != null && !name.isBlank(), "Name cannot be empty")
                  .bind(KafkaCluster::getName, KafkaCluster::setName);

            var txtBootstrapServers = new TextField();
            txtBootstrapServers.setValueChangeMode(ValueChangeMode.EAGER);
            binder.forField(txtBootstrapServers)
                  .asRequired("Bootstrap Servers cannot be empty!")
                  .withValidator(bootstrapServers -> bootstrapServers != null && !bootstrapServers.isBlank(),
                                 "Bootstrap Servers cannot be empty!")
                  .bind(KafkaCluster::getBootstrapServers, KafkaCluster::setBootstrapServers);

            var cmbProtocol = new ComboBox<Protocol>();
            cmbProtocol.setItems(Protocol.values());
            cmbProtocol.setItemLabelGenerator(Protocol::toString);

            cmbProtocol.setWidthFull();
            binder.forField(cmbProtocol)
                  .asRequired("Protocol cannot be empty!")
                  .withValidator(p -> p != null, "Protocol cannot be empty!")
                  .bind(KafkaCluster::getProtocol, KafkaCluster::setProtocol);

            var accessCredentialsForm = new SslAccessCredentialsForm();
            accessCredentialsForm.setWidthFull();

            cmbProtocol.addValueChangeListener(v -> {
                switch (v.getValue()) {
                    case PLAINTEXT:
                        accessCredentialsForm.hideSslComponents();
                        break;
                    case SSL:
                        accessCredentialsForm.showSslComponents();
                        break;
                }
            });

            var saveButton = new Button("Save", event -> {
                var entity = new Cluster();
                var bean = new KafkaCluster();
                try {
                    binder.writeBean(bean);
                } catch (ValidationException e) {
                    LOGGER.error("Error saving cluster", e);
                    return;
                }
                LOGGER.info("Saving cluster {}", bean);
                entity.setName(bean.getName());
                entity.setBootstrapServers(bean.getBootstrapServers());
                entity.setProtocol(bean.getProtocol());
                switch (bean.getProtocol()) {
                    case PLAINTEXT:
                        entity.setAccessSslCredentials(null);
                        break;
                    case SSL:
                        entity.setAccessSslCredentials(accessCredentialsForm.getCredential());
                        break;

                }
                clusterRepository.create(entity);
                showGrid();
            });

            var updateButton = new Button("Update", event -> {
                var bean = new KafkaCluster();
                try {
                    binder.writeBean(bean);
                } catch (ValidationException e) {
                    LOGGER.error("Error saving cluster", e);
                    return;
                }
                clusterRepository.update(new Cluster(bean.getId().get(), bean.getName(), bean.getBootstrapServers()));
                showGrid();
            });
            updateButton.setVisible(false);
            swithMode = (Boolean mode) -> {
                saveButton.setVisible(mode);
                updateButton.setVisible(!mode);
            };

            binder.addValueChangeListener(event -> {
                updateButton.setEnabled(binder.isValid() && binder.hasChanges());
                saveButton.setEnabled(binder.isValid() && binder.hasChanges());
            });

            var cancelButton = new Button("Cancel", event -> showGrid());
            form.addFormItem(txtName, "Name");
            form.setColspan(form.addFormItem(txtBootstrapServers, "Bootstrap Servers"), 2);
            form.addFormItem(cmbProtocol, "Protocol");
            form.addFormItem(accessCredentialsForm, "SSL Credentials");

            form.add(new HorizontalLayout(saveButton, updateButton, cancelButton));
            add(form);
            setVisible(false);
        }

        public void setItem(KafkaCluster kafkaCluster) {
            binder.readBean(kafkaCluster);
            swithMode.accept(kafkaCluster.getId().isEmpty());
        }

        public void focus() {
            focus.execute();
        }
    }

    private class GridView extends VerticalLayout {
        private final EntityTable<KafkaCluster> table;

        private GridView() {
            table = new EntityTable<>(loadClusters());
            table.addColumn("Cluster #")
                 .withValue(cluster -> Long.toString(cluster.getId().orElse(0l)))
                 .build()
                 .addColumn("Name")
                 .withValue(KafkaCluster::getName)
                 .build()
                 .addColumn("Bootstrap Servers")
                 .withValue(KafkaCluster::getBootstrapServers)
                 .build()
                 .addColumn("Protocol")
                 .withValue(c -> c.getProtocol().name())
                 .build()
                 .addColumn("Actions")
                 .withComponent(cluster -> {
                     var editButton = new Button("Edit", event -> showForm(cluster));
                     var deleteButton = new Button("Delete", event -> {
                         clusterRepository.delete(cluster.id.get());
                         table.update(loadClusters());
                     });
                     return new HorizontalLayout(editButton, deleteButton);
                 })
                 .build()
                 .bind();
            add(createActionButton(), table);
        }

        public void reloadItems() {
            table.update(loadClusters());
        }

        private Component createActionButton() {
            var layout = new HorizontalLayout();
            layout.add(new Button("New Cluster", event -> showForm(new KafkaCluster())));
            return layout;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClusterEditorView.class.getName());

    private GridView gridView;
    private EditorForm formView;
    private ClusterRepository clusterRepository;

    @Inject
    public KafkaClusterEditorView(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;

    }

    @Override
    protected String getTitle() {
        return "Kafka Cluster Editor";
    }

    @Override
    protected Component buildContent() {
        formView = new EditorForm();
        gridView = new GridView();
        return new Div(formView, gridView);
    }

    private List<KafkaCluster> loadClusters() {
        return clusterRepository.findAll()
                                .stream()
                                .map(cluster -> new KafkaCluster(cluster)).toList();
    }

    private void showGrid() {
        gridView.setVisible(true);
        gridView.reloadItems();
        formView.setVisible(false);
    }

    private void showForm(KafkaCluster cluster) {
        gridView.setVisible(false);
        formView.setVisible(true);
        formView.setItem(cluster);
        formView.focus();
    }

}