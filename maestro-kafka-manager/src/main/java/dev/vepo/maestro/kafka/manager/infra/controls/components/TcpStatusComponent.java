package dev.vepo.maestro.kafka.manager.infra.controls.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import dev.vepo.maestro.kafka.manager.tcp.TcpCheck;

public class TcpStatusComponent extends Span {

    public TcpStatusComponent(String bootstrapServers) {
        if (TcpCheck.fromKafkaBootstrapServers(bootstrapServers)
                    .anyMatch(TcpCheck::isListening)) {
            var okIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
            okIcon.setClassName("icon ok");
            add(okIcon);
        } else {
            var nokIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
            nokIcon.setClassName("icon nok");
            add(nokIcon);
        }
    }

}
