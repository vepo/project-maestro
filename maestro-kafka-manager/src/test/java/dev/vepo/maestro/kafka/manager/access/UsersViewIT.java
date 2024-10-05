package dev.vepo.maestro.kafka.manager.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import dev.vepo.maestro.kafka.manager.utils.MaestroTest;
import dev.vepo.maestro.kafka.manager.utils.MaestroTest.Context;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@ExtendWith(MaestroTest.class)
class UsersViewIT {
    @Test
    void accessTest(Context context) {
        context.givenLoggedIn().accessMenu("Users");
        var wait = context.wait;

        wait.until(titleIs("Users"));

        var table = context.hasTable();
        assertThat(table.getRows()).isEqualTo(2);

    }
}
