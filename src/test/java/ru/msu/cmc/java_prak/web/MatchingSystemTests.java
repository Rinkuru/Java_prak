package ru.msu.cmc.java_prak.web;

import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

        openPersonMatchesThroughCard(person.getId());

        assertCurrentUrlEndsWith("/people/" + person.getId() + "/matches");
        assertPageContains("Подходящие вакансии");
        assertPageContains("Java-разработчик");
    }

    @Test
    public void personMatchesShouldExplainIncompleteCriteria() {
        Person person = persistPerson(
                "Крылов Артем Сергеевич",
                "Высшее техническое",
                true,
                null,
                null
        );

        openPersonMatchesThroughCard(person.getId());

        assertPageContains("Для содержательного подбора у человека должны быть заполнены желаемая должность и желаемая зарплата.");
        assertPageNotContains("Подходящих вакансий по текущим условиям не найдено.");
    }

    @Test
    public void personMatchesShouldHideClosedVacanciesAndShowBlankEducationInfoThroughUi() {
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

        openPersonMatchesThroughCard(person.getId());

        assertPageContains("У человека не заполнено образование, поэтому фильтр по образованию не применялся.");
        assertPageContains("Открытая компания");
        assertPageNotContains("Закрытая компания");
    }

    @Test
    public void personMatchesShouldSortVisibleVacanciesThroughForm() {
        Person person = persistPerson(
                "Смирнов Андрей Викторович",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "200000.00"
        );
        Company firstCompany = persistCompany("Компания 1", "Открытая вакансия");
        Company secondCompany = persistCompany("Компания 2", "Открытая вакансия");

        persistVacancy(firstCompany, "Java-разработчик", "300000.00", true, "Высшее техническое");
        persistVacancy(secondCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");

        openPersonMatchesThroughCard(person.getId());

        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[3]")).getText(),
                "300000.00"
        );

        selectById("matches-sort-input").selectByValue("asc");
        element("matching-filter-submit").click();

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

        openPersonMatchesThroughCard(person.getId());

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

        openVacancyMatchesThroughCard(company.getId(), vacancy.getId());

        assertCurrentUrlEndsWith("/vacancies/" + vacancy.getId() + "/matches");
        assertPageContains("Подходящие резюме");
        assertPageContains("Соколова Мария Андреевна");
    }

    @Test
    public void vacancyMatchesShouldShowClosedVacancyAndBlankEducationInfoThroughUi() {
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

        openVacancyMatchesThroughCard(company.getId(), vacancy.getId());

        assertPageContains("Подбор выполняется для закрытой вакансии.");
        assertPageContains("У вакансии не заполнено требование к образованию, поэтому фильтр по образованию не применялся.");
        assertPageContains("Соколова Мария Андреевна");
        assertPageNotContains("Петров Кирилл Олегович");
    }

    @Test
    public void vacancyMatchesShouldSortVisiblePeopleThroughForm() {
        Company company = persistCompany("T-Банк", "Финтех");
        Vacancy vacancy = persistVacancy(company, "Аналитик", "250000.00", true, "Высшее экономическое");
        Person firstPerson = persistPerson(
                "Соколова Мария Андреевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "180000.00"
        );
        Person secondPerson = persistPerson(
                "Иванова Елена Сергеевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "230000.00"
        );

        openVacancyMatchesThroughCard(company.getId(), vacancy.getId());

        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[1]")).getText(),
                "Соколова Мария Андреевна"
        );

        selectById("matches-sort-input").selectByValue("desc");
        element("matching-filter-submit").click();

        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[1]")).getText(),
                "Иванова Елена Сергеевна"
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[5]")).getText(),
                "230000.00"
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[2]/td[5]")).getText(),
                "180000.00"
        );
    }

    @Test
    public void vacancyMatchesShouldShowEmptyStateWhenNoPeopleMatch() {
        Company company = persistCompany("VK", "Социальная сеть");
        Vacancy vacancy = persistVacancy(company, "Data Scientist", "300000.00", true, "Высшее техническое");

        openVacancyMatchesThroughCard(company.getId(), vacancy.getId());

        assertPageContains("Подходящих резюме по текущим условиям не найдено.");
    }
}
