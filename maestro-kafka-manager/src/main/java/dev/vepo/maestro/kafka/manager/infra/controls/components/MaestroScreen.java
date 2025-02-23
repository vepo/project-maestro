package dev.vepo.maestro.kafka.manager.infra.controls.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

import io.quarkus.security.identity.SecurityIdentity;
import dev.vepo.maestro.kafka.manager.MainView;
import dev.vepo.maestro.kafka.manager.infra.security.Authenticator;
import dev.vepo.maestro.kafka.manager.kafka.exceptions.KafkaUnaccessibleException;
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
        try {
            setContent(buildContent());
        } catch (KafkaUnaccessibleException ke) {
            getUI().ifPresent(ui -> ui.navigate(""));
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            Div text = new Div(new Text("Não foi possível conectar ao Cluster Kafka!"));

            Button closeButton = new Button(new Icon("lumo", "cross"));
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeButton.setAriaLabel("Close");
            closeButton.addClickListener(e -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(text, closeButton);
            layout.setAlignItems(Alignment.CENTER);

            notification.add(layout);
            notification.open();
        }
    }

    @PostConstruct
    protected void setup() {
        setPrimarySection(Section.DRAWER);
        buildNavbar();
        menu = new MaestroMenu(identity.getRoles());
        buildDrawer();
    }

    protected void buildDrawer() {
        var appName = new RouterLink("Maestro", MainView.class);

        appName.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                              LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD,
                              LumoUtility.Height.XLARGE, LumoUtility.Padding.Horizontal.MEDIUM);
        var clusterSwitch = new ClusterSwitch(clusterSelector.getSelected(),
                                              clusterRepository.findAll(),
                                              cluster -> {
                                                  clusterSelector.select(cluster.getId());
                                                  menu.updateSelectedCluster(Optional.of(cluster.getId()));
                                                  setContent(buildContent());
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
