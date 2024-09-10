package io.vepo.maestro.kafka.manager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import io.vepo.maestro.kafka.manager.components.MaestroScreen;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import jakarta.inject.Inject;

@Route("kafka/:clusterId([1-9][0-9]*)/topics/:topicName([a-zA-Z0-9\\-\\_]+)")
public class ListenKafkaTopicView extends MaestroScreen {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenKafkaTopicView.class);

    @Inject
    KafkaAdminService adminService;

    @Override
    protected String getTitle() {
        var topicName = getRouteParameter("topicName").orElseThrow(() -> new IllegalArgumentException("Topic Name is required"));
        return maybeCluster().map(c -> String.format("Listening Topic %s on Cluster %s", topicName, c.name))
                             .orElse("Topics");
    }

    public record Message(String key, String value, long offset, int partition, long timestamp) {
    }

    @Override
    protected Component buildContent() {
        var layout = new FormLayout();
        layout.setSizeFull();
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
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
        layout.addFormItem(cmbKeySeralizer, "Key Serializer");

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
        layout.addFormItem(cmbValueSeralizer, "Value Serializer");
        var btnStart = new Button("Start");
        btnStart.setMaxWidth("250px");
        layout.add(btnStart);

        var gridMessages = new Grid<Message>();
        var dataProvider = new ListDataProvider<Message>(new ArrayList<>());
        gridMessages.setDataProvider(dataProvider);
        gridMessages.addColumn(Message::key).setHeader("Key");
        gridMessages.addColumn(Message::value).setHeader("Value");
        gridMessages.addColumn(Message::offset).setHeader("Offset");
        gridMessages.addColumn(Message::partition).setHeader("Partition");
        gridMessages.addColumn(Message::timestamp).setHeader("Timestamp");
        gridMessages.setSizeFull();

        btnStart.addClickListener(e -> {
            var keySerializer = cmbKeySeralizer.getValue();
            var valueSerializer = cmbValueSeralizer.getValue();
            // Start listening
            Executors.newSingleThreadExecutor()
                     .submit(() -> {
                         var topicName = getRouteParameter("topicName").orElseThrow(() -> new IllegalArgumentException("Topic Name is required"));
                         try {
                            LOGGER.info("Starting to listen topic: {} - {}", topicName, maybeCluster());
                         } catch (Exception ex) {
                             LOGGER.error("Error listening topic: {}", topicName, ex);
                         }
                         maybeCluster().ifPresent(c -> {
                             try {
                                LOGGER.info("Starting to listen topic: {} on cluster: {}", topicName, c);
                                 var configs = new Properties();
                                 configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, c.bootstrapServers);
                                 configs.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-" + topicName + "-" + System.currentTimeMillis());
                                 configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer(keySerializer));
                                 configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, serializer(valueSerializer));
                                 configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                                 LOGGER.info("Starting consumer with configs: {}", configs);
                                 try (var consumer = new KafkaConsumer<String, String>(configs)) {
                                     consumer.subscribe(List.of(topicName));
                                     while (true) {
                                         var messages = StreamSupport.stream(consumer.poll(Duration.ofSeconds(1)).spliterator(), false)
                                                                     .map(record -> new Message(record.key(), record.value(), record.offset(), record.partition(), record.timestamp()))
                                                                     .collect(Collectors.toList());
                                         if (!messages.isEmpty()) {
                                             LOGGER.info("Received {} messages", messages.size());
                                             dataProvider.getItems().addAll(messages);
                                         }
                                     }
                                 } catch (Exception ex) {
                                     LOGGER.error("Error listening topic: {}", topicName, ex);
                                 }

                                 LOGGER.info("Stopped listening topic: {}", topicName);
                             } catch (Exception ex) {
                                 LOGGER.error("Error listening topic: {}", topicName, ex);
                             }
                         });
                     });
        });
        return new VerticalLayout(layout, gridMessages);
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
