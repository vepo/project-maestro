package dev.vepo.maestro.kafka.manager.infra.controls.components;

import static com.vaadin.flow.component.icon.VaadinIcon.CLUSTER;
import static com.vaadin.flow.component.icon.VaadinIcon.COG;
import static com.vaadin.flow.component.icon.VaadinIcon.MEGAPHONE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.tcp.TcpCheck;

public class MaestroMenu extends VerticalLayout {

    private final transient Consumer<List<Cluster>> bindMenu;
    private final VerticalLayout content;

    public MaestroMenu(Set<String> roles, List<Cluster> clusters) {
        bindMenu = bindMenu(roles);
        content = new VerticalLayout();
        add(new Scroller(content));
        bindMenu.accept(clusters);
    }

    private Consumer<List<Cluster>> bindMenu(Set<String> roles) {
        return clusters -> {
            content.removeAll();
            var sideBar = new SideNav();
            sideBar.addItem(new SideNavItem("Clusters", "/kafka", CLUSTER.create()));
            sideBar.addItem(new SideNavItem("Users", "/users", USERS.create()));
            content.add(sideBar);
            clusters.forEach(c -> {
                var clusterNav = new SuffixSideNavItem(c.getName(),
                                                       String.format("/kafka/%d", c.getId()),
                                                       CLUSTER.create(),
                                                       new TcpStatusComponent(c.getBootstrapServers()));
                if (TcpCheck.fromKafkaBootstrapServers(c.getBootstrapServers()).anyMatch(TcpCheck::isListening)) {
                    clusterNav.addClassName("active");
                } else {
                    clusterNav.addClassName("inactive");
                }
                clusterNav.addItem(new SideNavItem("Cluster", String.format("/kafka/%d/info", c.getId()), COG.create()),
                                   new SideNavItem("Topics", String.format("/kafka/%d/topics", c.getId()), MEGAPHONE.create()),
                                   new SideNavItem("Consumers", String.format("/kafka/%d/consumers", c.getId()), PLAY_CIRCLE.create()));
                sideBar.addItem(clusterNav);
            });
        };
    }
}
