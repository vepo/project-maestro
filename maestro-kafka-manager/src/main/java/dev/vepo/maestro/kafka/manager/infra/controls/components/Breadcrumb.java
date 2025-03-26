package dev.vepo.maestro.kafka.manager.infra.controls.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;

public class Breadcrumb extends Div {

    public Breadcrumb() {
        addClassName("breadcrumb");
    }

    public record PageParent(String title,
                             Class<? extends Component> implementation,
                             RouteParameters params) {
    }

    public void setup(String title, PageParent... parents) {
        this.removeAll();
        for (PageParent parent : parents) {
            this.add(new RouterLink(parent.title, parent.implementation, parent.params));
        }
        this.add(new H2(title));
    }

}
