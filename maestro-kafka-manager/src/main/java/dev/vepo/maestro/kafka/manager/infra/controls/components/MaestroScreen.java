package dev.vepo.maestro.kafka.manager.infra.controls.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

import io.quarkus.security.identity.SecurityIdentity;
import dev.vepo.maestro.kafka.manager.infra.security.Authenticator;
import dev.vepo.maestro.kafka.manager.model.Cluster;
import dev.vepo.maestro.kafka.manager.model.ClusterRepository;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class MaestroScreen extends AppLayout implements AfterNavigationObserver, HasDynamicTitle, BeforeEnterObserver {

    @Inject
    ClusterSelector clusterSelector;

    @Inject
    ClusterRepository clusterRepository;

    private Map<String, String> routeParameters = Collections.synchronizedMap(new HashMap<>());

    private MaestroMenu menu;

    @Inject
    SecurityIdentity identity;

    @Inject
    Authenticator authenticator;

    private H2 viewTitle;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var params = event.getRouteParameters();
        params.getParameterNames()
              .forEach(name -> params.get(name)
                                     .ifPresent(value -> routeParameters.put(name, value)));
        params.get("clusterId")
              .map(Long::valueOf)
              .ifPresent(clusterSelector::select);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        viewTitle.setText(getTitle());
        menu.updateSelectedCluster(clusterSelector.getSelected());
        setContent(buildContent());
    }

    @PostConstruct
    protected void setup() {
        setPrimarySection(Section.DRAWER);
        buildNavbar();
        menu = new MaestroMenu(identity.getRoles());
        buildDrawer();
    }

    protected void buildDrawer() {
        var appName = new Span("Maestro");

        appName.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                              LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD,
                              LumoUtility.Height.XLARGE, LumoUtility.Padding.Horizontal.MEDIUM);
        var clusterSwitch = new ClusterSwitch(clusterSelector.getSelected(),
                                              clusterRepository.findAll(),
                                              cluster -> {
                                                  clusterSelector.select(cluster.getId());
                                                  menu.updateSelectedCluster(Optional.of(cluster.getId()));
                                              });
        addToDrawer(appName,
                    new VerticalLayout(clusterSwitch),
                    menu,
                    new VerticalLayout(new Button("Logout", e -> authenticator.logout())));

    }

    protected Optional<String> getRouteParameter(String name) {
        return Optional.ofNullable(routeParameters.get(name));
    }

    protected Optional<Cluster> maybeCluster() {
        return clusterSelector.getSelectedCluster();
    }

    protected void buildNavbar() {
        var toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.setTooltipText("Menu toggle");
        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE,
                                LumoUtility.Flex.GROW);
        var header = new Header(toggle, viewTitle);
        header.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                             LumoUtility.Padding.End.MEDIUM, LumoUtility.Width.FULL);

        addToNavbar(false, header);

    }

    protected abstract String getTitle();

    @Override
    public String getPageTitle() {
        return getTitle();
    }

    protected abstract Component buildContent();

    protected Optional<Long> getClusterId() {
        return clusterSelector.getSelected();
    }

}
