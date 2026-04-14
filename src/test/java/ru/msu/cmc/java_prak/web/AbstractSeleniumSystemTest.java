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

    protected void openHomePage() {
        driver.get(baseUrl() + "/");
    }

    protected void openPeopleListPage() {
        driver.get(baseUrl() + "/people");
    }

    protected void openCompaniesListPage() {
        driver.get(baseUrl() + "/companies");
    }

    protected void openPersonCreateFormThroughList() {
        openPeopleListPage();
        element("add-person-link").click();
    }

    protected void openPersonCardThroughList(Long personId) {
        openPeopleListPage();
        element("person-link-" + personId).click();
    }

    protected void openPersonEditFormThroughCard(Long personId) {
        openPersonCardThroughList(personId);
        element("edit-person-link").click();
    }

    protected void openWorkExperienceCreateFormThroughPersonCard(Long personId) {
        openPersonCardThroughList(personId);
        element("add-work-experience-link").click();
    }

    protected void openWorkExperienceEditFormThroughPersonCard(Long personId, Long workExperienceId) {
        openPersonCardThroughList(personId);
        element("edit-work-link-" + workExperienceId).click();
    }

    protected void openPersonMatchesThroughCard(Long personId) {
        openPersonCardThroughList(personId);
        element("person-matches-link").click();
    }

    protected void openCompanyCreateFormThroughList() {
        openCompaniesListPage();
        element("add-company-link").click();
    }

    protected void openCompanyCardThroughList(Long companyId) {
        openCompaniesListPage();
        element("company-link-" + companyId).click();
    }

    protected void openCompanyEditFormThroughCard(Long companyId) {
        openCompanyCardThroughList(companyId);
        element("edit-company-link").click();
    }

    protected void openVacancyCreateFormThroughCompanyCard(Long companyId) {
        openCompanyCardThroughList(companyId);
        element("add-vacancy-link").click();
    }

    protected void openVacancyCardThroughCompanyCard(Long companyId, Long vacancyId) {
        openCompanyCardThroughList(companyId);
        element("vacancy-link-" + vacancyId).click();
    }

    protected void openVacancyEditFormThroughCard(Long companyId, Long vacancyId) {
        openVacancyCardThroughCompanyCard(companyId, vacancyId);
        element("edit-vacancy-link").click();
    }

    protected void openVacancyMatchesThroughCard(Long companyId, Long vacancyId) {
        openVacancyCardThroughCompanyCard(companyId, vacancyId);
        element("vacancy-matches-link").click();
    }
}
