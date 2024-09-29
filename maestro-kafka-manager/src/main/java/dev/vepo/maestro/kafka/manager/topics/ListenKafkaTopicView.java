package dev.vepo.maestro.kafka.manager.topics;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import dev.vepo.maestro.kafka.manager.kafka.TopicConsumer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@Route("kafka/:clusterId([1-9][0-9]*)/topics/:topicName([a-zA-Z0-9\\-\\_\\.]+)")
public class ListenKafkaTopicView extends MaestroScreen implements BeforeLeaveObserver {

    public record Message(String key, String value, long offset, int partition, long timestamp) {
    }

    @Inject
    KafkaAdminService adminService;

    private AtomicReference<TopicConsumer> consumer = new AtomicReference<>();

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        closeConsumer();
    }

    @Override
    protected String getTitle() {
        var topicName = getRouteParameter("topicName").orElseThrow(() -> new IllegalArgumentException("Topic Name is required"));
        return maybeCluster().map(c -> String.format("Listening Topic %s on Cluster %s", topicName, c.getName()))
                             .orElse("Topics");
    }

    @Override
    protected Component buildContent() {
        closeConsumer();

        var consumerParametersForm = new FormLayout();
        consumerParametersForm.setWidthFull();
        consumerParametersForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        var cmbKeySeralizer = new ComboBox<String>();
        cmbKeySeralizer.setWidthFull();
        cmbKeySeralizer.setItems("StringSerializer",
                                 "LongSerializer",
                                 "IntegerSerializer",
                                 "DoubleSerializer",
                                 "FloatSerializer",
                                 "ByteArraySerializer",
                                 "AvroSerializer",
                                 "ProtobufSerializer");
        cmbKeySeralizer.setValue("StringSerializer");
        consumerParametersForm.addFormItem(cmbKeySeralizer, "Key Serializer");

        var cmbValueSeralizer = new ComboBox<String>();
        cmbValueSeralizer.setWidthFull();
        cmbValueSeralizer.setItems("StringSerializer",
                                   "LongSerializer",
                                   "IntegerSerializer",
                                   "DoubleSerializer",
                                   "FloatSerializer",
                                   "ByteArraySerializer",
                                   "AvroSerializer",
                                   "ProtobufSerializer");
        cmbValueSeralizer.setValue("StringSerializer");
        consumerParametersForm.addFormItem(cmbValueSeralizer, "Value Serializer");
        var btnStart = new Button("Start");
        consumerParametersForm.add(btnStart);
        var btnStop = new Button("Stop");
        btnStop.setEnabled(false);
        consumerParametersForm.add(btnStop);

        var gridMessages = new Grid<Message>();
        var dataProvider = new ListDataProvider<Message>(new ArrayList<>());
        gridMessages.setDataProvider(dataProvider);
        gridMessages.addColumn(Message::key).setHeader("Key");
        gridMessages.addColumn(Message::value).setHeader("Value");
        gridMessages.addColumn(Message::offset).setHeader("Offset").setSortable(true);
        gridMessages.addColumn(Message::partition).setHeader("Partition").setSortable(true);
        gridMessages.addColumn(Message::timestamp).setHeader("Timestamp").setSortable(true);
        gridMessages.setSizeFull();
        var cluster = maybeCluster().orElseThrow(() -> new IllegalArgumentException("Cluster not found"));
        consumer.set(new TopicConsumer(cluster));

        btnStart.addClickListener(e -> {
            var keySerializer = cmbKeySeralizer.getValue();
            var valueSerializer = cmbValueSeralizer.getValue();
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            // Start listening
            var topicName = getRouteParameter("topicName").orElseThrow(() -> new IllegalArgumentException("Topic Name is required"));
            consumer.get().start(topicName,
                                 serializer(keySerializer),
                                 serializer(valueSerializer),
                                 records -> {
                                     var messages = StreamSupport.stream(records.spliterator(), false)
                                                                 .map(record -> new Message(record.key().toString(),
                                                                                            record.value().toString(),
                                                                                            record.offset(),
                                                                                            record.partition(),
                                                                                            record.timestamp()))
                                                                 .collect(Collectors.toList());
                                     getUI().ifPresent(ui -> {
                                         if (ui.isAttached()) {
                                             ui.access(() -> {
                                                 dataProvider.getItems().addAll(messages);
                                                 dataProvider.refreshAll();
                                                 ui.push();
                                             });
                                         } else {
                                             consumer.get().close();
                                         }
                                     });
                                 });

        });

        btnStop.addClickListener(e -> {
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            consumer.get().close();
        });
        var contents = new VerticalLayout(consumerParametersForm, gridMessages);
        contents.setSizeFull();
        return contents;
    }

    private void closeConsumer() {
        consumer.updateAndGet(c -> {
            if (Objects.nonNull(c)) {
                c.close();
            }
            return null;
        });
    }

    private String serializer(String serializer) {
        return switch (serializer) {
            case "StringSerializer" -> "org.apache.kafka.common.serialization.StringDeserializer";
            case "LongSerializer" -> "org.apache.kafka.common.serialization.LongDeserializer";
            case "IntegerSerializer" -> "org.apache.kafka.common.serialization.IntegerDeserializer";
            case "DoubleSerializer" -> "org.apache.kafka.common.serialization.DoubleDeserializer";
            case "FloatSerializer" -> "org.apache.kafka.common.serialization.FloatDeserializer";
            case "ByteArraySerializer" -> "org.apache.kafka.common.serialization.ByteArrayDeserializer";
            case "AvroSerializer" -> "io.confluent.kafka.serializers.KafkaAvroDeserializer";
            case "ProtobufSerializer" -> "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer";
            default -> throw new IllegalArgumentException("Serializer not supported: " + serializer);
        };
    }

}
