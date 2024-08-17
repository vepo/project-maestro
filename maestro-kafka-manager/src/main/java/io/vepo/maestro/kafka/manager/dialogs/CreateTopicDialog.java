package io.vepo.maestro.kafka.manager.dialogs;

import java.util.ArrayList;
import java.util.List;

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

public class CreateTopicDialog extends Dialog {

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
        grid.addComponentColumn(c -> {
            var txtKey = new TextField();
            txtKey.setValueChangeMode(ValueChangeMode.EAGER);
            txtKey.addValueChangeListener(e -> {
                System.out.println("Value changed: " + e.getValue());
                System.out.println("Items        : " + items);
                System.out.println("Item         : " + c);
                System.out.println("Items BINDER : " + binder.getBean());
//                System.out.println("Items GRID   : " + grid.getDataProvider().fetch(new ArrayList<>()));
            });
            binder.bind(txtKey, Configuration::getKey, Configuration::setKey);
            return txtKey;
        }).setHeader("Key");
        grid.addComponentColumn(c -> {
            var txtValue = new TextField();
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
