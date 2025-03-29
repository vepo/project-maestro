package dev.vepo.maestro.kafka.manager.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.cluster.KafkaClusterStatus;
import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.ConsumerGroupTable;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@Route("kafka/:clusterId([1-9][0-9]*)/consumers")
public class KafkaConsumerView extends MaestroScreen {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerView.class);
    private KafkaAdminService adminService;

    @Inject
    public KafkaConsumerView(KafkaAdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected String getTitle() {
        return "Consumers";
    }

    @Override
    protected PageParent[] getParents() {
        return new PageParent[] {
            MainView.page(this),
            KafkaClusterStatus.page(this) };
    }

    @Override
    protected Component buildContent() {
        try {
            return new VerticalLayout(new ConsumerGroupTable(adminService.listConsumers()));
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }
}
