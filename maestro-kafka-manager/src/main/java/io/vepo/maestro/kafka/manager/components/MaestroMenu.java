package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MaestroMenu extends VerticalLayout {

    private Consumer<Long> updateClusterAction;

    public MaestroMenu(Optional<Long> maybeClusterId) {
        var sideBar = new SideNav();
        sideBar.addItem(new SideNavItem("Clusters", "/kafka"));
        maybeClusterId.ifPresent(clusterId -> {
            sideBar.addItem(new SideNavItem("Cluster", "/kafka/" + clusterId));
            sideBar.addItem(new SideNavItem("Topics", "/kafka/" + clusterId + "/topics"));
            sideBar.addItem(new SideNavItem("Consumers", "/kafka/" + clusterId + "/consumers"));
        });
        updateClusterAction = id -> {
            sideBar.removeAll();
            sideBar.addItem(new SideNavItem("Clusters", "/kafka"));
            sideBar.addItem(new SideNavItem("Cluster", "/kafka/" + id));
            sideBar.addItem(new SideNavItem("Topics", "/kafka/" + id + "/topics"));
            sideBar.addItem(new SideNavItem("Consumers", "/kafka/" + id + "/consumers"));
        };
        add(new Scroller(sideBar));
    }

    public void updateSelectedCluster(Long id) {
        updateClusterAction.accept(id);
    }
}
