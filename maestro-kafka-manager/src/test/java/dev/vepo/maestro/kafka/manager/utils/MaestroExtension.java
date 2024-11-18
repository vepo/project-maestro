package dev.vepo.maestro.kafka.manager.utils;

import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlToBe;

import java.util.Objects;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class MaestroExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public class Context {
        private Context(FirefoxDriver driver, WebDriverWait wait) {
            this.driver = driver;
            this.wait = wait;
        }

        public FirefoxDriver driver;
        public WebDriverWait wait;

        public WebElement accessShadowRoot(WebElement element, By selectors) {
            return SeleniumFunctions.accessShadowRoot(element, driver, selectors);
        }

        public Context givenLoggedIn() {
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
            return this;
        }

        public Context accessMenu(String menuItem) {
            var menu = wait.until(elementToBeClickable(xpath(String.format("//vaadin-side-nav-item[contains(.,'%s')]", menuItem))));
            menu.click();
            return this;
        }

        public Table hasTable() {
            return new Table(wait.until(elementToBeClickable(xpath("//table[@class='custom-table']"))));
        }
    }

    public class Table {

        private WebElement element;

        public Table(WebElement element) {
            this.element = element;
        }

        public int getRows() {
            return element.findElements(By.xpath(".//tr")).size();
        }
    }

    private FirefoxDriver driver;
    private WebDriverWait wait;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        WebDriverManager.firefoxdriver().setup();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, ofSeconds(60), ofSeconds(1));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        wait = null;
        if (Objects.nonNull(driver)) {
            driver.quit();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(Context.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(Context.class)) {
            return new Context(driver, wait);
        } else {
            throw new ParameterResolutionException("Unsupported parameter type");
        }
    }

}
