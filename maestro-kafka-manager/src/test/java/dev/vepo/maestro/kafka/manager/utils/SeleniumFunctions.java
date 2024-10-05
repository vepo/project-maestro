package dev.vepo.maestro.kafka.manager.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumFunctions {
    private SeleniumFunctions() {
        throw new IllegalStateException("Utility class");
    }

    public static WebElement accessShadowRoot(WebElement root, RemoteWebDriver driver, By... selectors) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        var shadowRoot = (SearchContext) js.executeScript("return arguments[0].shadowRoot.firstElementChild", root);
        assertNotNull(shadowRoot, "Shadow root not found");
        if (selectors.length == 1) {
            return shadowRoot.findElement(selectors[0]);
        } else {
            return accessShadowRoot((WebElement) shadowRoot, driver, Arrays.copyOfRange(selectors, 1, selectors.length));
        }
    }
}
