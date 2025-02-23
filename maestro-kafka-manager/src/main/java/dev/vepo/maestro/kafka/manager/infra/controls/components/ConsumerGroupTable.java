package dev.vepo.maestro.kafka.manager.infra.controls.components;

import java.util.List;

import dev.vepo.maestro.kafka.manager.infra.controls.html.Table;
import dev.vepo.maestro.kafka.manager.kafka.KafkaAdminService.ConsumerGroup;

public class ConsumerGroupTable extends Table {
    public ConsumerGroupTable(List<ConsumerGroup> consumers) {
        super();
        addHeader("ID", rowSpan(2));
        addHeader("Type", rowSpan(2));
        addHeader("State", rowSpan(2));
        addHeader("Coordinator", rowSpan(2));
        addHeader("Members", rowSpan(1), colspan(6), true);
        addHeader("Consumer ID");
        addHeader("Client ID");
        addHeader("Host");
        addHeader("Assignment");
        addHeader("Offset");
        addHeader("LAG");

        consumers.forEach(c -> {
            var rowSpan = rowSpan(Math.max(1, c.members()
                                               .stream()
                                               .mapToInt(m -> m.assignment()
                                                               .size())
                                               .sum()));
            addCell(c.id(), rowSpan);
            addCell(c.type(), rowSpan);
            addCell(c.state(), rowSpan);
            addCell(c.coordinator(), rowSpan);
            if (c.members().isEmpty()) {
                addCell("N/A");
                addCell("N/A");
                addCell("N/A");
                addCell("N/A");
                addCell("N/A");
                addCell("N/A", true);
            } else {
                c.members().forEach(m -> {
                    var memberRowSpan = rowSpan(Math.max(1, m.assignment()
                                                             .size()));
                    addCell(m.consumerId(), memberRowSpan);
                    addCell(m.clientId(), memberRowSpan);
                    addCell(m.host(), memberRowSpan);
                    m.assignment()
                     .stream()
                     .forEach(t -> {
                         addCell(String.format("%s:%d", t.topic(), t.partition()));
                         addCell(Long.toString(t.offset()));
                         addCell(Long.toString(t.lag()), true);
                     });
                });
            }
        });

    }

}
