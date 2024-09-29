package dev.vepo.maestro.kafka.manager.infra.controls.html;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;

@Tag("td")
public class Td extends HtmlContainer {
    public Td() {
        this(false);
    }

    public Td(boolean grow) {
        super();
        if (grow) {
            getElement().setAttribute("width", "99%");
        } else {
            getElement().setAttribute("width", "0");
        }
    }
}
