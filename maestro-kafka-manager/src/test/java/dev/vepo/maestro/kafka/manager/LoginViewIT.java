package dev.vepo.maestro.kafka.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlToBe;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.maestro.kafka.manager.utils.MaestroTest;
import dev.vepo.maestro.kafka.manager.utils.MaestroTest.Context;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@ExtendWith(MaestroTest.class)
class LoginViewIT {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewIT.class);

    @Order(1)
    @Test
    void loginFailedTest(Context context) {
        logger.info("Running loginFailedTest");
        var wait = context.wait;
        var driver = context.driver;

        logger.info("Opening browser");
        driver.get("http://localhost:8081");
        wait.until(urlToBe("http://localhost:8081/login"));
        wait.until(titleIs("Maestro"));

        logger.info("Typing username and password");
        var txtUsername = wait.until(elementToBeClickable(xpath("//input[@name='username']")));
        var txtPassword = wait.until(elementToBeClickable(xpath("//input[@name='password']")));

        txtUsername.sendKeys("admin");
        txtPassword.sendKeys("wrongpassword");
        var btnLogin = wait.until(elementToBeClickable(xpath("//vaadin-button[contains(.,'Log in')]")));
        logger.info("Clicking on login button");
        btnLogin.click();
        logger.info("Waiting for error message");
        wait.until(urlToBe("http://localhost:8081/login"));
        // find element with "Incorrect username or password"
        logger.info("Checking error message");
        var errorMessage =context. accessShadowRoot(wait.until(elementToBeClickable(xpath("//vaadin-login-form-wrapper"))),  xpath("//div[@part='error-message']"));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Incorrect username or password"));
    }

    @Order(2)
    @Test
    void loginSuccessTest(Context context) {
        logger.info("Running loginSuccessTest");
        var wait = context.wait;
        var driver = context.driver;

        driver.get("http://localhost:8081");
        wait.until(urlToBe("http://localhost:8081/login"));
        wait.until(titleIs("Maestro"));

        var txtUsername = wait.until(elementToBeClickable(xpath("//input[@name='username']")));
        var txtPassword = wait.until(elementToBeClickable(xpath("//input[@name='password']")));

        txtUsername.sendKeys("admin");
        txtPassword.sendKeys("admin");
        var btnLogin = wait.until(elementToBeClickable(xpath("//vaadin-button[contains(.,'Log in')]")));
        btnLogin.click();
        wait.until(urlToBe("http://localhost:8081/"));
        wait.until(titleIs("Maestro"));
        assertEquals("Maestro", driver.getTitle());
    }
}
