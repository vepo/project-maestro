package dev.vepo.maestro.kafka.manager.infra.controls.components;

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class SuffixSideNavItem extends SideNavItem {

    private Span labelElement;
    private HorizontalLayout container;

    public SuffixSideNavItem(String label, Component suffixComponent) {
        super(label);
        if (Objects.isNull(container)) {
            container = new HorizontalLayout();
        }
        suffixComponent.addClassName("side-nav-item-suffix");
        this.container.add(suffixComponent);
        getElement().appendChild(container.getElement());
    }

    public SuffixSideNavItem(String label, String path, Component suffixComponent) {
        super(label, path);
        if (Objects.isNull(container)) {
            container = new HorizontalLayout();
        }
        suffixComponent.addClassName("side-nav-item-suffix");
        this.container.add(suffixComponent);
        getElement().appendChild(container.getElement());
    }

    public SuffixSideNavItem(String label, String path, Component prefixComponent, Component suffixComponent) {
        super(label, path, prefixComponent);
        if (Objects.isNull(container)) {
            container = new HorizontalLayout();
        }
        suffixComponent.addClassName("side-nav-item-suffix");
        this.container.add(suffixComponent);
        getElement().appendChild(container.getElement());
    }

    @Override
    public void setLabel(String label) {
        if (label == null) {
            removeLabelElement();
        } else {
            if (labelElement == null) {
                labelElement = createAndAppendLabelElement(label);
            } else {
                labelElement.setText(label);
            }
        }
    }

    @Override
    public String getLabel() {
        return labelElement == null ? null : labelElement.getText();
    }

    private void removeLabelElement() {
        if (labelElement != null) {
            container.remove(labelElement);
            labelElement = null;
        }
    }

    private Span createAndAppendLabelElement(String label) {
        var element = new Span(label);
        if (Objects.isNull(container)) {
            container = new HorizontalLayout();
        }
        container.addComponentAsFirst(element);
        return element;
    }
}
