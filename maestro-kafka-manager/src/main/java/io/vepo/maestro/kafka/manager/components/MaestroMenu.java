package io.vepo.maestro.kafka.manager.components;

import java.util.Optional;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MaestroMenu extends VerticalLayout {

    public MaestroMenu(Optional<String> maybeClusterId) {
        var sideBar = new SideNav();
        sideBar.addItem(new SideNavItem("Clusters", "/kafka"));
        maybeClusterId.ifPresent(clusterId -> {
            sideBar.addItem(new SideNavItem("Cluster", "/kafka/" + clusterId));
            sideBar.addItem(new SideNavItem("Topics", "/kafka/" + clusterId + "/topics"));
            sideBar.addItem(new SideNavItem("Consumers", "/kafka/" + clusterId + "/consumers"));
        });
        add(new Scroller(sideBar));
    }
}
