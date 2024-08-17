package io.vepo.maestro.kafka.manager.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Platform;

public class CreateTopicDialog extends Dialog {
    private static final Logger logger = LoggerFactory.getLogger(CreateTopicDialog.class);

    public CreateTopicDialog() {
        super();

        setHeaderTitle("New Topic");

        add(createDialogLayout());

        Button saveButton = new Button("Save", e -> close());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private class Configuration {
        private String key;
        private String value;

        Configuration() {
            key = "";
            value = "";
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Configuration [key=%s, value=%s]", key, value);
        }

        public boolean isBlank() {
            return key == null || value == null || key.isBlank() || value.isBlank();
        }
    }

    private FormLayout createDialogLayout() {

        var txtName = new TextField();
        var nmbPartitions = new IntegerField();
        nmbPartitions.setStepButtonsVisible(true);
        nmbPartitions.setMin(1);

        var nmbReplicationFactor = new IntegerField();
        nmbReplicationFactor.setStepButtonsVisible(true);
        nmbReplicationFactor.setMin(1);

        var dialogLayout = new FormLayout();
        var item = dialogLayout.addFormItem(txtName, "Name");
        dialogLayout.setColspan(item, 2);
        // dialogLayout.setColspan(txtName, 2);
        dialogLayout.addFormItem(nmbPartitions, "Partitions");
        dialogLayout.addFormItem(nmbReplicationFactor, "Replication Factor");

        var grid = new Grid<Configuration>();
        var editor = grid.getEditor();
        var binder = new Binder<>(Configuration.class);
        editor.setBinder(binder);
        editor.setBuffered(true);
        var items = new ArrayList<>(List.of(new Configuration()));
        grid.setItems(items);
        Runnable checkItems = () -> {
            logger.info("Items: {}", items);
            if (items.size() == 0 || !items.get(items.size() - 1).isBlank()) {
                items.add(new Configuration());
                UI.getCurrent().access(() -> grid.setItems(items));
            }
        };
        grid.addComponentColumn(c -> {
            logger.info("Creating key component for {}", c);
            var txtKey = new TextField();
            txtKey.setValue(c.getKey());
            txtKey.setWidthFull();
            txtKey.setValueChangeMode(ValueChangeMode.ON_BLUR);
            txtKey.addValueChangeListener(e -> {
                c.setKey(e.getValue());
                checkItems.run();
            });
            binder.bind(txtKey, Configuration::getKey, Configuration::setKey);
            return txtKey;
        }).setHeader("Key");
        grid.addComponentColumn(c -> {
            logger.info("Creating value component for {}", c);
            var txtValue = new TextField();
            txtValue.setValue(c.getValue());
            txtValue.setWidthFull();
            txtValue.setValueChangeMode(ValueChangeMode.ON_BLUR);
            txtValue.addValueChangeListener(e -> {
                c.setValue(e.getValue());
                checkItems.run();
            });
            binder.bind(txtValue, Configuration::getValue, Configuration::setValue);
            return txtValue;
        }).setHeader("Value");
        grid.addComponentColumn(c -> new HorizontalLayout(new Button("Delete",
                                                                     e -> {
                                                                         items.remove(c);
                                                                         grid.setItems(items);
                                                                     })))
            .setHeader("Actions");
        dialogLayout.setColspan(dialogLayout.addFormItem(grid, "Configurations"), 2);

        return dialogLayout;
    }
}
