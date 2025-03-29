package dev.vepo.maestro.kafka.manager.cluster;

import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@PreserveOnRefresh
@Route("kafka/:clusterId([1-9][0-9]*)")
public class KafkaClusterStatus extends MaestroScreen {

    public static PageParent page(MaestroScreen page) {
        return page.maybeCluster().map(cluster -> new PageParent(String.format("Cluster \"%s\"", cluster.getName()),
                                                                 MainView.class,
                                                                 new RouteParameters(Map.of("clusterId", cluster.getId().toString()))))
                   .orElseThrow(() -> new IllegalStateException("Cluster not selected!!!"));

    }

    @Override
    protected String getTitle() {
        return maybeCluster().map(c -> String.format("Cluster \"%s\"", c.getName())).orElseThrow(() -> new IllegalStateException("Cluster does not exists!"));
    }

    @Override
    protected Component buildContent() {
        return new HorizontalLayout();
    }

    @Override
    protected PageParent[] getParents() {
        return new PageParent[] {
            MainView.page(this) };
    }

}
