package dev.vepo.maestro.kafka.manager;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlToBe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@SuppressWarnings("squid:S2699")
@QuarkusIntegrationTest
class LoginViewIT {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewIT.class);

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.firefoxdriver().setup();
    }

    private RemoteWebDriver driver;

    @Order(1)
    @Test
    void loginFailedTest() {
        logger.info("Running loginFailedTest");
        var wait = new WebDriverWait(driver, ofSeconds(60), ofSeconds(1));

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
        JavascriptExecutor js = (JavascriptExecutor) driver;
        var loginForm = wait.until(elementToBeClickable(xpath("//vaadin-login-form-wrapper")));
        var shadowRoot = (SearchContext) js.executeScript("return arguments[0].shadowRoot.firstElementChild", loginForm);
        var errorMessage = shadowRoot.findElement(cssSelector("div[part='error-message'"));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Incorrect username or password"));
    }

    @Order(2)
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
