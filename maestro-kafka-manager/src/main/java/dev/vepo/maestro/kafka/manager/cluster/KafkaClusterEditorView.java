package dev.vepo.maestro.kafka.manager.cluster;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
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
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;

import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.cluster.management.KafkaCluster;
import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.Protocol;
import dev.vepo.maestro.kafka.manager.model.SslCredentials;


public class KafkaClusterEditorView extends MaestroScreen {
   

    private class SslAccessCredentialsForm extends Div {
        private final FormLayout form;
        private final Div naLabel;
        private final MemoryBuffer keystoreBuffer;
        private final MemoryBuffer truststoreBuffer;
        private final Supplier<SslCredentials> loadCredentials;

        private SslAccessCredentialsForm() {
            form = new FormLayout();
            form.setVisible(false);
            naLabel = new Div(new Text("N/A"));
            add(form, naLabel);

            var upKeystore = new Upload();
            keystoreBuffer = new MemoryBuffer();
            upKeystore.setReceiver(keystoreBuffer);
            form.addFormItem(upKeystore, "Keystore");

            var txtKeystorePassword = new TextField();
            form.addFormItem(txtKeystorePassword, "Keystore Password");

            var upTruststore = new Upload();
            truststoreBuffer = new MemoryBuffer();
            upTruststore.setReceiver(truststoreBuffer);
            form.addFormItem(upTruststore, "Truststore");

            var txtTruststorePassword = new TextField();
            form.addFormItem(txtTruststorePassword, "Truststore Password");

            var txtKeyPassword = new TextField();
            form.addFormItem(txtKeyPassword, "Key Password");
            loadCredentials = () -> {
                try {
                    return new SslCredentials(IOUtils.toByteArray(keystoreBuffer.getInputStream()),
                                              keystoreBuffer.getFileName(),
                                              txtKeyPassword.getValue(),
                                              IOUtils.toByteArray(truststoreBuffer.getInputStream()),
                                              truststoreBuffer.getFileName(),
                                              txtTruststorePassword.getValue(),
                                              txtKeyPassword.getValue());
                } catch (IOException ioe) {
                    return null;
                }
            };
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
            return loadCredentials.get();
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
            // binder.forField(txtId)
            //       .bind(cluster -> cluster.getId().map(Object::toString).orElse(""),
            //             (cluster, id) -> cluster.id = id != null && !id.isBlank() ? Optional.of(Long.parseLong(id))
            //                                                                       : Optional.empty());
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
                // clusterRepository.create(entity);
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
                // clusterRepository.update(new Cluster(bean.getId().get(), bean.getName(), bean.getBootstrapServers()));
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

    // private class GridView extends VerticalLayout {
    //     private final EntityTable<KafkaCluster> table;

    //     private GridView() {
         
    //     }

    //     public void reloadItems() {
    //         table.update(loadClusters());
    //     }
    // }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClusterEditorView.class.getName());

    // private GridView gridView;
    private EditorForm formView;

    @Override
    protected String getTitle() {
        return "Clusters";
    }

    @Override
    protected PageParent[] getParents() {
        return new PageParent[] {
            MainView.page(this) };
    }

    @Override
    protected Component buildContent() {
        return new EditorForm();
        // gridView = new GridView();
        // return new Div(formView, gridView);
    }

    private void showGrid() {
        // gridView.setVisible(true);
        // gridView.reloadItems();
        formView.setVisible(false);
    }

    private void showForm(KafkaCluster cluster) {
        // gridView.setVisible(false);
        formView.setVisible(true);
        formView.setItem(cluster);
        formView.focus();
    }

}