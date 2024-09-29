package dev.vepo.maestro.kafka.manager.access;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import dev.vepo.maestro.kafka.manager.access.model.User;
import dev.vepo.maestro.kafka.manager.access.model.UserRepository;
import dev.vepo.maestro.kafka.manager.infra.controls.components.MaestroScreen;
import dev.vepo.maestro.kafka.manager.infra.controls.html.EntityTable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@Route("users")
@RolesAllowed("ADMIN")
public class UsersView extends MaestroScreen {

    @Inject
    UserRepository userRepository;

    @Override
    protected String getTitle() {
        return "Users";
    }

    @Override
    protected Component buildContent() {
        var userGrid = new EntityTable<User>(userRepository.findAll()).addColumn("ID").withValue(u -> Long.toString(u.getId())).build()
                                                                      .addColumn("Username").withValue(User::getUsername).build()
                                                                      .addColumn("Email").withValue(User::getEmail).build()
                                                                      .addColumn("Role").withValue(User::getRole).build()
                                                                      .addColumn("Actions").withComponent(u -> {
                                                                          var btnEdit = new Button("Edit");
                                                                          var btnDelete = new Button("Delete");
                                                                          return new HorizontalLayout(btnEdit, btnDelete);
                                                                      }).build()
                                                                      .bind();
        return new VerticalLayout(userGrid);
    }

}
