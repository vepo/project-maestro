package dev.vepo.maestro.kafka.manager;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;

@Theme("starter-theme")
@Push(PushMode.MANUAL)
public class AppConfig implements AppShellConfigurator {
}
