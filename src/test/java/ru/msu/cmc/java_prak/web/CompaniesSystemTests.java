package ru.msu.cmc.java_prak.web;

import java.time.LocalDate;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Системные Selenium-тесты раздела "Компании".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompaniesSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard() {
        Company targetCompany = persistCompany("Т-Банк", "Финтех");
        Company closedCompany = persistCompany("СберБанк", "Банк");
        Company otherCompany = persistCompany("Городская клиника", "Медицина");
        persistVacancy(targetCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");
        persistVacancy(closedCompany, "Java-разработчик", "220000.00", false, "Высшее техническое");
        persistVacancy(otherCompany, "Врач", "180000.00", true, "Высшее медицинское");

        openCompaniesListPage();

        clearAndType("company-name-filter", "банк");
        element("company-open-filter").click();
        element("company-filter-submit").click();

        assertPageContains("Т-Банк");
        assertPageNotContains("СберБанк");
        assertPageNotContains("Городская клиника");

        element("company-link-" + targetCompany.getId()).click();
        assertPageContains("Карточка компании");
        assertPageContains("Т-Банк");
    }

    @Test
    public void companiesListShouldFilterByVacancyCriteriaThroughForm() {
        Company javaCompany = persistCompany("ВКонтакте", "IT-компания");
        Company lowSalaryCompany = persistCompany("Небольшая студия", "Низкие зарплаты");
        Company medicalCompany = persistCompany("Городская клиника", "Медицина");
        persistVacancy(javaCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");
        persistVacancy(lowSalaryCompany, "Java-разработчик", "150000.00", true, "Высшее техническое");
        persistVacancy(medicalCompany, "Врач-терапевт", "180000.00", true, "Высшее медицинское");

        openCompaniesListPage();

        clearAndType("company-position-filter", "java");
        clearAndType("company-min-salary-filter", "200000");
        element("company-open-filter").click();
        element("company-filter-submit").click();

        assertPageContains("Показаны компании, в которых есть вакансии по текущим условиям. Чтобы увидеть сами вакансии, откройте карточку компании.");
        assertPageContains("ВКонтакте");
        assertPageNotContains("Небольшая студия");
        assertPageNotContains("Городская клиника");
    }

    @Test
    public void companyCreateShouldSaveValidCompany() {
        openCompanyCreateFormThroughList();

        clearAndType("company-name-input", "Яндекс Практикум");
        clearAndType("company-description-input", "Образовательная IT-компания");
        element("save-company-button").click();

        assertPageContains("Компания успешно добавлена.");
        assertPageContains("Яндекс Практикум");
        assertEquals(companyDao.findAllOrderByName().size(), 1);
    }

    @Test
    public void companyCreateShouldRequireName() {
        openCompanyCreateFormThroughList();

        element("save-company-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите название компании.");
    }

    @Test
    public void companyCreateShouldRejectTooLongFields() {
        openCompanyCreateFormThroughList();

        clearAndType("company-name-input", "Н".repeat(256));
        clearAndType("company-description-input", "О".repeat(4001));
        element("save-company-button").click();

        assertPageContains("Название компании должно быть не длиннее 255 символов.");
        assertPageContains("Описание компании должно быть не длиннее 4000 символов.");
    }

    @Test
    public void companyCreateShouldRejectDuplicateName() {
        persistCompany("ВКонтакте", "IT-компания");

        openCompanyCreateFormThroughList();

        clearAndType("company-name-input", "ВКонтакте");
        clearAndType("company-description-input", "Дубликат");
        element("save-company-button").click();

        assertPageContains("Компания с таким названием уже существует.");
    }

    @Test
    public void companyEditShouldUpdateExistingCompany() {
        Company company = persistCompany("T-Банк", "Финтех");

        openCompanyEditFormThroughCard(company.getId());

        clearAndType("company-name-input", "T-Банк Технологии");
        clearAndType("company-description-input", "Новая технологическая компания");
        element("save-company-button").click();

        assertPageContains("Данные компании обновлены.");
        assertPageContains("T-Банк Технологии");
        assertEquals(companyDao.findById(company.getId()).orElseThrow().getName(), "T-Банк Технологии");
    }

    @Test
    public void companyCardShouldBlockDeletionWhenReferenced() {
        Company lockedCompany = persistCompany("ВКонтакте", "IT-компания");
        Person person = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "220000.00"
        );
        persistWorkExperience(
                person,
                lockedCompany,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2021, 1, 1),
                null
        );

        openCompanyCardThroughList(lockedCompany.getId());
        element("delete-company-button").click();

        assertPageContains("Компания не может быть удалена, потому что на неё ссылается история работы кандидатов.");
        assertTrue(companyDao.findById(lockedCompany.getId()).isPresent());
    }

    @Test
    public void companyCardShouldDeleteFreeCompany() {
        Company freeCompany = persistCompany("Яндекс", "Поиск и технологии");

        openCompanyCardThroughList(freeCompany.getId());
        element("delete-company-button").click();

        assertTrue(driver.getCurrentUrl().contains("/companies"));
        assertPageContains("Компания удалена.");
        assertTrue(companyDao.findById(freeCompany.getId()).isEmpty());
    }

    @Test
    public void companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive() {
        Company company = persistCompany("T-Банк", "Финтех");
        persistVacancy(company, "Java-разработчик", "250000.00", true, "Высшее техническое");
        persistVacancy(company, "Java-разработчик", "260000.00", false, "Высшее техническое");
        persistVacancy(company, "Java-разработчик", "180000.00", true, "Высшее техническое");
        persistVacancy(company, "Системный аналитик", "270000.00", true, "Высшее");

        openCompanyCardThroughList(company.getId());

        clearAndType("vacancy-position-filter", "java");
        clearAndType("vacancy-min-salary-filter", "240000");
        element("vacancy-only-active-filter").click();
        element("vacancy-filter-submit").click();

        assertPageContains("Показаны вакансии компании по текущим условиям поиска.");
        assertEquals(driver.findElements(By.xpath("//table[@class='data-table']/tbody/tr")).size(), 1);
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[1]")).getText(),
                "Java-разработчик"
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[2]")).getText(),
                "250000.00"
        );
        assertPageNotContains("Системный аналитик");
    }

    @Test
    public void companyCardShouldShowNoVacanciesState() {
        Company emptyCompany = persistCompany("Пустая компания", "Без вакансий");

        openCompanyCardThroughList(emptyCompany.getId());

        assertPageContains("У компании пока нет вакансий.");
    }

    @Test
    public void companyCardShouldShowNoMatchingVacanciesState() {
        Company company = persistCompany("Рабочая компания", "С вакансиями");
        persistVacancy(company, "Java-разработчик", "240000.00", true, "Высшее техническое");

        openCompanyCardThroughList(company.getId());

        clearAndType("vacancy-position-filter", "DevOps");
        element("vacancy-filter-submit").click();

        assertPageContains("По текущим фильтрам вакансии не найдены.");
        assertPageNotContains("Java-разработчик");
    }
}
