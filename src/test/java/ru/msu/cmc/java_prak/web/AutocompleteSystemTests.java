package ru.msu.cmc.java_prak.web;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Системные Selenium-тесты JS-автодополнения.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AutocompleteSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void companyNameFilterShouldFillValueFromSuggestion() {
        persistCompany("Alpha Tech", "IT");
        persistCompany("Beta Labs", "IT");

        openCompaniesListPage();
        clearAndType("company-name-filter", "alp");

        WebElement option = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#company-name-filter-suggestions .autocomplete-option")
                ));

        assertEquals(option.getText(), "Alpha Tech");
        option.click();

        assertEquals(element("company-name-filter").getAttribute("value"), "Alpha Tech");
    }

    @Test
    public void peopleWorkedPositionFilterShouldUseCandidatePositionSuggestions() {
        persistPerson(
                "Pavel Product",
                "Высшее",
                true,
                "Product Owner",
                "190000.00"
        );

        openPeopleListPage();
        clearAndType("filter-position", "prod");

        WebElement option = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#filter-position-suggestions .autocomplete-option")
                ));

        assertEquals(option.getText(), "Product Owner");
    }
}
