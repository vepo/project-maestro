package dev.vepo.maestro.kafka.manager;

import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlToBe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class LoginViewIT {
    @BeforeAll
    static void setupDriver() {
        WebDriverManager.firefoxdriver().setup();
    }

    private RemoteWebDriver driver;

    @Test
    void loginSuccessTest() {
        var wait = new WebDriverWait(driver, ofSeconds(60), ofSeconds(1));

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
    }

    @BeforeEach
    void createDriver() {
        var options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
    }

    @AfterEach
    void exitDriver() {
        driver.quit();
    }
}
