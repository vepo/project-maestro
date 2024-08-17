package io.vepo.maestro.kafka.manager.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Key;
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

    private static final List<String> TOPIC_PROPERTIES = Stream.of(TopicConfig.CLEANUP_POLICY_CONFIG,
                                                                   TopicConfig.COMPRESSION_TYPE_CONFIG,
                                                                   TopicConfig.DELETE_RETENTION_MS_CONFIG,
                                                                   TopicConfig.FILE_DELETE_DELAY_MS_CONFIG,
                                                                   TopicConfig.FLUSH_MESSAGES_INTERVAL_CONFIG,
                                                                   TopicConfig.FLUSH_MS_CONFIG,
                                                                   TopicConfig.INDEX_INTERVAL_BYTES_CONFIG,
                                                                   TopicConfig.MAX_COMPACTION_LAG_MS_CONFIG,
                                                                   TopicConfig.MAX_MESSAGE_BYTES_CONFIG,
                                                                   TopicConfig.MESSAGE_DOWNCONVERSION_ENABLE_CONFIG,
                                                                   TopicConfig.MESSAGE_FORMAT_VERSION_CONFIG,
                                                                   TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG,
                                                                   TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG,
                                                                   TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG,
                                                                   TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG,
                                                                   TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                                                                   TopicConfig.PREALLOCATE_CONFIG,
                                                                   TopicConfig.RETENTION_BYTES_CONFIG,
                                                                   TopicConfig.RETENTION_MS_CONFIG,
                                                                   TopicConfig.SEGMENT_BYTES_CONFIG,
                                                                   TopicConfig.SEGMENT_INDEX_BYTES_CONFIG,
                                                                   TopicConfig.SEGMENT_JITTER_MS_CONFIG,
                                                                   TopicConfig.SEGMENT_MS_CONFIG,
                                                                   TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG)
                                                               .sorted().toList();

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
        dialogLayout.addFormItem(nmbPartitions, "Partitions");
        dialogLayout.addFormItem(nmbReplicationFactor, "Replication Factor");

        var grid = new Grid<Configuration>();
        var editor = grid.getEditor();
        var binder = new Binder<>(Configuration.class);
        editor.setBinder(binder);
        editor.setBuffered(true);
        var items = new ArrayList<>(List.of(new Configuration()));
        var view = grid.setItems(items);
        Runnable checkItems = () -> {
            if (view.getItemCount() == 0 || !view.getItem(view.getItemCount() - 1).isBlank()) {
                view.addItem(new Configuration());
            }
        };
        grid.addComponentColumn(c -> {
            AtomicInteger lastIndex = new AtomicInteger(-1);
            var txtKey = new TextField();
            txtKey.setValue(c.getKey());
            txtKey.setWidthFull();
            txtKey.setValueChangeMode(ValueChangeMode.ON_BLUR);
            txtKey.addValueChangeListener(e -> {
                c.setKey(e.getValue());
                checkItems.run();
            });
            txtKey.addKeyDownListener(Key.ARROW_UP, e -> {
                var currValue = lastIndex.updateAndGet(v -> {
                    if (v == 0) {
                        return TOPIC_PROPERTIES.size() - 1;
                    } else {
                        return v - 1;
                    }
                });
                txtKey.setValue(TOPIC_PROPERTIES.get(currValue));
            });
            txtKey.addKeyDownListener(Key.ARROW_DOWN, e -> {
                var currValue = lastIndex.updateAndGet(v -> {
                    if (v == TOPIC_PROPERTIES.size() - 1) {
                        return 0;
                    } else {
                        return v + 1;
                    }

                });
                txtKey.setValue(TOPIC_PROPERTIES.get(currValue));
            });
            binder.bind(txtKey, Configuration::getKey, Configuration::setKey);
            return txtKey;
        }).setHeader("Key")
            .setFlexGrow(1);
        grid.addComponentColumn(c -> {
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
        }).setHeader("Value").setFlexGrow(1);
        grid.addComponentColumn(c -> new HorizontalLayout(new Button("Delete",
                                                                     e -> {
                                                                         view.removeItem(c);
                                                                         if (view.getItemCount() == 0) {
                                                                             view.addItem(new Configuration());
                                                                         }
                                                                     })))
            .setFlexGrow(0)
            .setHeader("Actions");
        dialogLayout.setColspan(dialogLayout.addFormItem(grid, "Configurations"), 2);

        return dialogLayout;
    }
}
