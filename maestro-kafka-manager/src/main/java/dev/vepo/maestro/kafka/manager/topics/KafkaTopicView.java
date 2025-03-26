package dev.vepo.maestro.kafka.manager.topics;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.infra.controls.components.Breadcrumb.PageParent;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.components.TopicTable;
import dev.vepo.maestro.kafka.manager.infra.controls.dialogs.CreateTopicDialog;
import dev.vepo.maestro.kafka.manager.infra.security.Roles;
import dev.vepo.maestro.kafka.manager.kafka.KafkaAdminService;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnavailableException;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnexpectedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed({
    Roles.USER,
    Roles.ADMIN })
@Route("kafka/:clusterId([1-9][0-9]*)/topics")
public class KafkaTopicView extends MaestroScreen {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicView.class);

    private KafkaAdminService adminService;

    @Inject
    public KafkaTopicView(KafkaAdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected String getTitle() {
        return "Topics";
    }

    @Override
    protected PageParent[] getParents() {
        var cluster = maybeCluster().orElseThrow(() -> new IllegalStateException("Cluster not selected!!!"));
        return new PageParent[] {
            new PageParent(String.format("Cluster \"%s\"", cluster.getName()),
                           MainView.class,
                           RouteParameters.empty()) };
    }

    @Override
    protected Component buildContent() {
        return getClusterId().map(this::build)
                             .orElseGet(() -> {
                                 getUI().ifPresent(ui -> ui.navigate(""));
                                 Notification.show("Cluster not found");
                                 return new Grid<>();
                             });
    }

    private Component build(Long clusterId) {
        try {
            var topics = adminService.listTopics();
            var topicsTable = new TopicTable(topics, (topic, table) -> {
                var btnDelete = new Button("Delete", evnt -> {
                    try {
                        adminService.deleteTopic(topic.name());
                        table.update(adminService.listTopics());
                    } catch (KafkaUnavailableException kue) {
                        logger.warn("Kafka Cluster is not available!", kue);
                    } catch (KafkaUnexpectedException kue) {
                        logger.error("Kafka Cluster is not good...", kue);
                    }
                });
                var btnListen = new Button("Listen", evnt -> {
                    getUI().ifPresent(ui -> ui.navigate("kafka/" + clusterId + "/topics/" + topic.name()));
                });
                return new HorizontalLayout(btnDelete, btnListen);
            });

            var buttons = new HorizontalLayout();
            var createTopicDialog = new CreateTopicDialog(command -> {
                try {
                    logger.info("Creating topic {}", command);
                    adminService.createTopic(command);
                    topicsTable.update(adminService.listTopics());
                } catch (KafkaUnavailableException kue) {
                    logger.warn("Kafka Cluster is not available!", kue);
                } catch (KafkaUnexpectedException kue) {
                    logger.error("Kafka Cluster is not good...", kue);
                }
            });
            buttons.add(createTopicDialog);
            buttons.add(new Button("Create", evnt -> createTopicDialog.open()));
            buttons.add(new Button("Refresh", evnt -> {
                try {
                    topicsTable.update(adminService.listTopics());
                } catch (KafkaUnavailableException kue) {
                    logger.warn("Kafka Cluster is not available!", kue);
                } catch (KafkaUnexpectedException kue) {
                    logger.error("Kafka Cluster is not good...", kue);
                }
            }));
            return new VerticalLayout(buttons, topicsTable);
        } catch (KafkaUnavailableException kue) {
            logger.warn("Kafka Cluster is not available!", kue);
        } catch (KafkaUnexpectedException kue) {
            logger.error("Kafka Cluster is not good...", kue);
        }
        return new VerticalLayout(new Text("Could not connect with Kafka Cluster!"));
    }

}
