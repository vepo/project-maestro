package io.vepo.maestro.kafka.manager.components;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;

import io.vepo.maestro.kafka.manager.components.html.Table;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService.KafkaTopic;

public class TopicTable extends Table {

    private final BiFunction<KafkaTopic, TopicTable, Component> actionsGenerator;

    public TopicTable(List<KafkaTopic> topics, BiFunction<KafkaTopic, TopicTable, Component> actionsGenerator) {
        super();
        this.actionsGenerator = actionsGenerator;
        addHeader("ID", rowSpan(2));
        addHeader("Name", rowSpan(2));
        addHeader("#Partitions", rowSpan(2));
        addHeader("Replicas", rowSpan(2));
        addHeader("Partitions", rowSpan(1), colspan(4));
        addHeader("Actions", rowSpan(2), true);
        addHeader("ID");
        addHeader("Leader");
        addHeader("Replicas");
        addHeader("ISR");
        populateBody(topics);
    }

    public void update(List<KafkaTopic> topics) {
        clearBody();
        populateBody(topics);
    }

    private void populateBody(List<KafkaTopic> topics) {
        topics.forEach(t -> {
            addCell(t.id(), rowSpan(t.partitions().size()));
            addCell(t.name(), true, rowSpan(t.partitions().size()));
            addCell(Integer.toString(t.partitions().size()), rowSpan(t.partitions().size()), Table.CellClass.NUMERIC);
            addCell(String.valueOf(t.replicas()), rowSpan(t.partitions().size()), Table.CellClass.NUMERIC);
            var firstPartition = t.partitions().get(0);
            t.partitions().forEach(p -> {
                addCell(Integer.toString(p.id()), Table.CellClass.NUMERIC);
                addCell(Integer.toString(p.leader()), Table.CellClass.NUMERIC);
                addCell(p.replicas().stream().map(i -> i.toString()).collect(Collectors.joining(", ")), Table.CellClass.NUMERIC);
                addCell(p.isr().stream().map(i -> i.toString()).collect(Collectors.joining(", ")), Table.CellClass.NUMERIC, !p.equals(firstPartition));
                if (p.equals(firstPartition)) {
                    addCell(actionsGenerator.apply(t, this), rowSpan(t.partitions().size()), true);
                }
            });
        });
    }

}
