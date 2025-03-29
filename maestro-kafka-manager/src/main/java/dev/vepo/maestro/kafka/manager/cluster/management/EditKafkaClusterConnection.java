package dev.vepo.maestro.kafka.manager.cluster.management;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@PreserveOnRefresh
@Route("kafka/edit/:clusterId([1-9][0-9]*)")
public class EditKafkaClusterConnection extends MaestroScreen {

    @Override
    protected String getTitle() {
        return "Edit";
    }

    @Override
    protected PageParent[] getParents() {
        return new PageParent[] {
            MainView.page(this),
            KafkaClusterConnection.page(this)
        };
    }

    @Override
    protected Component buildContent() {
        return new VerticalLayout();
    }

}
