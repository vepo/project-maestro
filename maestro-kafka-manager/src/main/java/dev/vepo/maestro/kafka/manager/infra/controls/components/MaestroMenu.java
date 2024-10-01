package dev.vepo.maestro.kafka.manager.infra.controls.components;

import static com.vaadin.flow.component.icon.VaadinIcon.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MaestroMenu extends VerticalLayout {

    private final transient Consumer<Optional<Long>> bindMenu;

    public MaestroMenu(Set<String> roles) {
        var sideBar = new SideNav();
        bindMenu = bindMenu(sideBar, roles);
        bindMenu.accept(Optional.empty());
        add(new Scroller(sideBar));
    }

    public void updateSelectedCluster(Optional<Long> id) {
        bindMenu.accept(id);
    }

    private static Consumer<Optional<Long>> bindMenu(SideNav sideBar, Set<String> roles) {
        return id -> {
            sideBar.removeAll();
            sideBar.addItem(new SideNavItem("Clusters", "/kafka", CLUSTER.create()));
            sideBar.addItem(new SideNavItem("Users", "/users", USERS.create()));
            if (id.isPresent()) {
                sideBar.addItem(new SideNavItem("Cluster", String.format("/kafka/%d", id.get()), COG.create()));
                sideBar.addItem(new SideNavItem("Topics", String.format("/kafka/%d/topics", id.get()), MEGAPHONE.create()));
                sideBar.addItem(new SideNavItem("Consumers", String.format("/kafka/%d/consumers", id.get()), PLAY_CIRCLE.create()));
            }
        };
    }
}
