package ru.msu.cmc.java_prak.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;

/**
 * Системные тесты главной страницы и базовой навигации.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HomeAndNavigationSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void homePageShouldNavigateToPeopleAndCompaniesSections() {
        persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "220000.00"
        );
        Company company = persistCompany("ВКонтакте", "IT-компания");
        persistVacancy(company, "Java-разработчик", "240000.00", true, "Высшее техническое");

        driver.get(baseUrl() + "/");

        assertPageContains("Веб-интерфейс кадрового агентства");

        element("home-people-link").click();
        assertCurrentUrlEndsWith("/people");
        assertPageContains("Список людей");
        assertPageContains("Иванов Алексей Дмитриевич");

        element("nav-home-link").click();
        assertCurrentUrlEndsWith("/");

        element("home-companies-link").click();
        assertCurrentUrlEndsWith("/companies");
        assertPageContains("Список компаний");
        assertPageContains("ВКонтакте");
    }

    @Test
    public void homePageShouldOpenAddFormsAndHeaderNavigation() {
        driver.get(baseUrl() + "/");

        element("home-add-person-link").click();
        assertCurrentUrlEndsWith("/people/new");
        assertPageContains("Добавление человека");

        element("nav-home-link").click();
        assertCurrentUrlEndsWith("/");

        element("home-add-company-link").click();
        assertCurrentUrlEndsWith("/companies/new");
        assertPageContains("Добавление компании");

        element("nav-companies-link").click();
        assertCurrentUrlEndsWith("/companies");
        assertPageContains("Список компаний");
    }
}
