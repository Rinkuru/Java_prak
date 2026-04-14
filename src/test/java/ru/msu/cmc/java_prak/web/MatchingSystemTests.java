package ru.msu.cmc.java_prak.web;

import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

import static org.testng.Assert.assertEquals;

/**
 * Системные Selenium-тесты страницы результатов подбора.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatchingSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void personCardShouldOpenMatchesPage() {
        Company company = persistCompany("VK", "Технологическая компания");
        Person person = persistPerson(
                "Смирнова Анна Ильинична",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "230000.00"
        );
        persistVacancy(company, "Java-разработчик", "260000.00", true, "Высшее техническое");

        driver.get(baseUrl() + "/people/" + person.getId());
        element("person-matches-link").click();

        assertCurrentUrlEndsWith("/people/" + person.getId() + "/matches");
        assertPageContains("Подходящие вакансии");
        assertPageContains("Java-разработчик");
    }

    @Test
    public void personMatchesShouldValidateQueryParamsAndExplainIncompleteCriteria() {
        Person person = persistPerson(
                "Крылов Артем Сергеевич",
                "Высшее техническое",
                true,
                null,
                null
        );

        driver.get(baseUrl() + "/people/" + person.getId() + "/matches?onlyActive=oops&sort=sideways");

        assertPageContains("Передано некорректное значение фильтра показа только открытых вакансий.");
        assertPageContains("Передан некорректный параметр сортировки.");
        assertPageContains("Для содержательного подбора у человека должны быть заполнены желаемая должность и желаемая зарплата.");
        assertPageNotContains("Подходящих вакансий по текущим условиям не найдено.");
    }

    @Test
    public void personMatchesShouldRespectOnlyActiveSortAndBlankEducationState() {
        Person person = persistPerson(
                "Смирнов Андрей Викторович",
                "",
                true,
                "Java-разработчик",
                "200000.00"
        );
        Company openCompany = persistCompany("Открытая компания", "Открытая вакансия");
        Company closedCompany = persistCompany("Закрытая компания", "Закрытая вакансия");

        persistVacancy(openCompany, "Java-разработчик", "300000.00", true, "Высшее техническое");
        persistVacancy(closedCompany, "Java-разработчик", "240000.00", false, "Высшее медицинское");

        driver.get(baseUrl() + "/people/" + person.getId() + "/matches");

        assertPageContains("У человека не заполнено образование, поэтому фильтр по образованию не применялся.");
        assertPageContains("Открытая компания");
        assertPageNotContains("Закрытая компания");

        driver.get(baseUrl() + "/people/" + person.getId() + "/matches?onlyActive=false&sort=asc");

        assertPageContains("Открытая компания");
        assertPageContains("Закрытая компания");
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[3]")).getText(),
                "240000.00"
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[2]/td[3]")).getText(),
                "300000.00"
        );
    }

    @Test
    public void personMatchesShouldShowEmptyStateWhenNoVacanciesMatch() {
        Person person = persistPerson(
                "Соколов Павел Андреевич",
                "Высшее техническое",
                true,
                "DevOps-инженер",
                "240000.00"
        );

        driver.get(baseUrl() + "/people/" + person.getId() + "/matches");

        assertPageContains("Подходящих вакансий по текущим условиям не найдено.");
    }

    @Test
    public void vacancyCardShouldOpenMatchesPage() {
        Company company = persistCompany("T-Банк", "Финтех");
        Vacancy vacancy = persistVacancy(company, "Аналитик", "210000.00", true, "Высшее экономическое");
        persistPerson(
                "Соколова Мария Андреевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "180000.00"
        );

        driver.get(baseUrl() + "/vacancies/" + vacancy.getId());
        element("vacancy-matches-link").click();

        assertCurrentUrlEndsWith("/vacancies/" + vacancy.getId() + "/matches");
        assertPageContains("Подходящие резюме");
        assertPageContains("Соколова Мария Андреевна");
    }

    @Test
    public void vacancyMatchesShouldHandleClosedVacancyBlankEducationFiltersAndSorting() {
        Company company = persistCompany("T-Банк", "Финтех");
        Vacancy vacancy = persistVacancy(company, "Аналитик", "210000.00", false, "");
        persistPerson(
                "Соколова Мария Андреевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "180000.00"
        );
        persistPerson(
                "Петров Кирилл Олегович",
                "Высшее техническое",
                false,
                "Аналитик",
                "170000.00"
        );
        persistPerson(
                "Иванова Елена Сергеевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "230000.00"
        );

        driver.get(baseUrl() + "/vacancies/" + vacancy.getId() + "/matches?onlyLookingForJob=wrong&sort=bad");

        assertPageContains("Передано некорректное значение фильтра показа только людей, которые ищут работу.");
        assertPageContains("Передан некорректный параметр сортировки.");
        assertPageContains("Подбор выполняется для закрытой вакансии.");
        assertPageContains("У вакансии не заполнено требование к образованию, поэтому фильтр по образованию не применялся.");
        assertPageContains("Соколова Мария Андреевна");
        assertPageNotContains("Петров Кирилл Олегович");

        driver.get(baseUrl() + "/vacancies/" + vacancy.getId() + "/matches?onlyLookingForJob=false&sort=desc");

        assertPageContains("Соколова Мария Андреевна");
        assertPageContains("Петров Кирилл Олегович");
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[5]")).getText(),
                "180000.00"
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[2]/td[5]")).getText(),
                "170000.00"
        );
    }

    @Test
    public void vacancyMatchesShouldShowEmptyStateWhenNoPeopleMatch() {
        Company company = persistCompany("VK", "Социальная сеть");
        Vacancy vacancy = persistVacancy(company, "Data Scientist", "300000.00", true, "Высшее техническое");

        driver.get(baseUrl() + "/vacancies/" + vacancy.getId() + "/matches");

        assertPageContains("Подходящих резюме по текущим условиям не найдено.");
    }
}
