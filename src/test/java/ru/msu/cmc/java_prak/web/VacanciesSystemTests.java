package ru.msu.cmc.java_prak.web;

import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Vacancy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Системные Selenium-тесты сценариев вакансий.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VacanciesSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void vacancyLifecycleShouldValidateCreateCloseReopenAndDelete() {
        Company company = persistCompany("VK", "Социальная сеть");

        driver.get(baseUrl() + "/companies/" + company.getId() + "/vacancies/new");
        element("save-vacancy-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите должность.");
        assertPageContains("Укажите зарплату.");

        clearAndType("vacancy-position-input", "Java-разработчик");
        clearAndType("vacancy-salary-input", "250000");
        clearAndType("vacancy-education-input", "Высшее техническое");
        element("save-vacancy-button").click();

        assertPageContains("Вакансия успешно добавлена.");
        assertPageContains("Java-разработчик");
        assertPageContains("Открыта");

        Vacancy createdVacancy = vacancyDao.findAllOrderById().get(0);

        element("close-vacancy-button").click();
        assertPageContains("Вакансия закрыта.");
        assertPageContains("Закрыта");

        element("reopen-vacancy-button").click();
        assertPageContains("Вакансия снова открыта.");
        assertPageContains("Открыта");

        element("delete-vacancy-button").click();
        assertCurrentUrlEndsWith("/companies/" + company.getId());
        assertPageContains("Вакансия удалена.");
        assertTrue(vacancyDao.findById(createdVacancy.getId()).isEmpty());
    }

    @Test
    public void vacancyFormShouldRejectNegativeSalaryAndTooLongFields() {
        Company company = persistCompany("VK", "Социальная сеть");

        driver.get(baseUrl() + "/companies/" + company.getId() + "/vacancies/new");

        clearAndType("vacancy-position-input", "П".repeat(256));
        clearAndType("vacancy-salary-input", "-1");
        clearAndType("vacancy-education-input", "О".repeat(256));
        clearAndType("vacancy-requirements-input", "Т".repeat(4001));
        element("save-vacancy-button").click();

        assertPageContains("Должность должна быть не длиннее 255 символов.");
        assertPageContains("Зарплата не может быть отрицательной.");
        assertPageContains("Требование к образованию должно быть не длиннее 255 символов.");
        assertPageContains("Требования должны быть не длиннее 4000 символов.");
    }

    @Test
    public void vacancyCardShouldOpenAndAllowEditingData() {
        Company firstCompany = persistCompany("T-Банк", "Финтех");
        Company secondCompany = persistCompany("Яндекс", "Технологии");
        Vacancy vacancy = persistVacancy(firstCompany, "Аналитик", "210000.00", true, "Высшее");

        driver.get(baseUrl() + "/companies/" + firstCompany.getId());
        element("vacancy-link-" + vacancy.getId()).click();

        assertCurrentUrlEndsWith("/vacancies/" + vacancy.getId());
        assertPageContains("Аналитик");
        assertPageContains("T-Банк");
        assertPageContains("210000.00");

        element("edit-vacancy-link").click();

        new Select(element("vacancy-company-input")).selectByVisibleText("Яндекс");
        clearAndType("vacancy-position-input", "Ведущий аналитик");
        clearAndType("vacancy-salary-input", "230000");
        clearAndType("vacancy-education-input", "Высшее техническое");
        selectById("vacancy-status-input").selectByValue("false");
        element("save-vacancy-button").click();

        assertPageContains("Данные вакансии обновлены.");
        assertPageContains("Яндекс");
        assertPageContains("Ведущий аналитик");
        assertPageContains("230000.00");
        assertPageContains("Закрыта");
        assertEquals(vacancyDao.findById(vacancy.getId()).orElseThrow().getCompany().getId(), secondCompany.getId());
    }

    @Test
    public void vacancyCardShouldFindVacanciesBySamePositionLink() {
        Company company = persistCompany("VK", "Социальная сеть");
        Vacancy sourceVacancy = persistVacancy(company, "Java-разработчик", "240000.00", true, "Высшее техническое");
        persistVacancy(company, "Java-разработчик", "260000.00", true, "Высшее техническое");
        persistVacancy(company, "Системный аналитик", "200000.00", true, "Высшее");

        driver.get(baseUrl() + "/vacancies/" + sourceVacancy.getId());
        element("vacancy-same-position-link").click();

        assertTrue(driver.getCurrentUrl().contains("/companies/" + company.getId()));
        assertEquals(element("vacancy-position-filter").getAttribute("value"), "Java-разработчик");
        assertEquals(driver.findElements(org.openqa.selenium.By.xpath("//table[@class='data-table']/tbody/tr")).size(), 2);
        assertPageContains("Java-разработчик");
        assertPageNotContains("Системный аналитик");
    }
}
