package io.vepo.maestro.kafka.manager.components;

import java.util.List;

import io.vepo.maestro.kafka.manager.components.html.Table;
import io.vepo.maestro.kafka.manager.kafka.KafkaAdminService.ConsumerGroup;

public class ConsumerGroupTable extends Table {
    public ConsumerGroupTable(List<ConsumerGroup> list) {
        super();
        addHeader("ID", rowSpan(2));
        addHeader("Type", rowSpan(2));
        addHeader("State", rowSpan(2));
        addHeader("Coordinator", rowSpan(2));
        addHeader("Members", rowSpan(1), colspan(4), true);
        addHeader("Consumer ID");
        addHeader("Client ID");
        addHeader("Host");
        addHeader("Assignment");

        list.forEach(c -> {
            addCell(c.id(), rowSpan(c.members().size()));
            addCell(c.type(), rowSpan(c.members().size()));
            addCell(c.state(), rowSpan(c.members().size()));
            addCell(c.coordinator(), rowSpan(c.members().size()));
            c.members().forEach(m -> {
                addCell(m.consumerId());
                addCell(m.clientId());
                addCell(m.host());
                addCell(m.assignment().toString(), true);
            });
        });
    }

}
