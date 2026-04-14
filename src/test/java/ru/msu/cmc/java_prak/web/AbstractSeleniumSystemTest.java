package ru.msu.cmc.java_prak.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * База для Selenium/HtmlUnit системных тестов.
 */
public abstract class AbstractSeleniumSystemTest extends AbstractWebTestSupport {

    protected HtmlUnitDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUpDriver() {
        driver = new HtmlUnitDriver(true);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected WebElement element(String id) {
        return driver.findElement(By.id(id));
    }

    protected Select selectById(String id) {
        return new Select(element(id));
    }

    protected void clearAndType(String id, String value) {
        WebElement field = element(id);
        field.clear();
        field.sendKeys(value);
    }

    protected void assertCurrentUrlEndsWith(String suffix) {
        assertTrue(driver.getCurrentUrl().endsWith(suffix));
    }

    protected void assertPageContains(String text) {
        assertTrue(driver.getPageSource().contains(text));
    }

    protected void assertPageNotContains(String text) {
        assertFalse(driver.getPageSource().contains(text));
    }
}
