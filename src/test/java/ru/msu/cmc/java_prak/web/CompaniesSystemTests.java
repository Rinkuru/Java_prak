package ru.msu.cmc.java_prak.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;

import java.time.LocalDate;

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

        driver.get(baseUrl() + "/companies");

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
    public void companiesListShouldValidateVacancyFilterParamsAndKeepSearchWorking() {
        Company javaCompany = persistCompany("ВКонтакте", "IT-компания");
        Company medicalCompany = persistCompany("Городская клиника", "Медицина");
        persistVacancy(javaCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");
        persistVacancy(medicalCompany, "Врач-терапевт", "180000.00", true, "Высшее медицинское");

        driver.get(baseUrl() + "/companies?position=java&minSalary=abc&onlyWithOpenVacancies=oops");

        assertPageContains("Передано некорректное значение фильтра \"Зарплата от\". Параметр не применён.");
        assertPageContains("Передано некорректное значение фильтра показа только компаний с открытыми вакансиями.");
        assertPageContains("Показаны компании, в которых есть вакансии по текущим условиям. Чтобы увидеть сами вакансии, откройте карточку компании.");
        assertPageContains("ВКонтакте");
        assertPageNotContains("Городская клиника");
    }

    @Test
    public void companyCreateShouldSaveValidCompany() {
        driver.get(baseUrl() + "/companies");
        element("add-company-link").click();

        clearAndType("company-name-input", "Яндекс Практикум");
        clearAndType("company-description-input", "Образовательная IT-компания");
        element("save-company-button").click();

        assertPageContains("Компания успешно добавлена.");
        assertPageContains("Яндекс Практикум");
        assertEquals(companyDao.findAllOrderByName().size(), 1);
    }

    @Test
    public void companyFormsShouldValidateRejectDuplicateAndTooLongFieldsAndUpdateData() {
        persistCompany("ВКонтакте", "IT-компания");
        Company companyToEdit = persistCompany("T-Банк", "Финтех");

        driver.get(baseUrl() + "/companies/new");
        element("save-company-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите название компании.");

        clearAndType("company-name-input", "Н".repeat(256));
        clearAndType("company-description-input", "О".repeat(4001));
        element("save-company-button").click();

        assertPageContains("Название компании должно быть не длиннее 255 символов.");
        assertPageContains("Описание компании должно быть не длиннее 4000 символов.");

        clearAndType("company-name-input", "ВКонтакте");
        clearAndType("company-description-input", "Дубликат");
        element("save-company-button").click();

        assertPageContains("Компания с таким названием уже существует.");

        driver.get(baseUrl() + "/companies/" + companyToEdit.getId() + "/edit");
        clearAndType("company-name-input", "T-Банк Технологии");
        clearAndType("company-description-input", "Новая технологическая компания");
        element("save-company-button").click();

        assertPageContains("Данные компании обновлены.");
        assertPageContains("T-Банк Технологии");
        assertEquals(companyDao.findById(companyToEdit.getId()).orElseThrow().getName(), "T-Банк Технологии");
    }

    @Test
    public void companyCardShouldBlockDeletionWhenReferencedAndDeleteFreeCompany() {
        Company lockedCompany = persistCompany("ВКонтакте", "IT-компания");
        Company freeCompany = persistCompany("Яндекс", "Поиск и технологии");
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

        driver.get(baseUrl() + "/companies/" + lockedCompany.getId());
        element("delete-company-button").click();

        assertPageContains("Компания не может быть удалена, потому что на неё ссылается история работы кандидатов.");
        assertTrue(companyDao.findById(lockedCompany.getId()).isPresent());

        driver.get(baseUrl() + "/companies/" + freeCompany.getId());
        element("delete-company-button").click();

        assertCurrentUrlEndsWith("/companies");
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

        driver.get(baseUrl() + "/companies/" + company.getId());

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
    public void companyCardShouldShowNoVacanciesAndNoMatchingStates() {
        Company emptyCompany = persistCompany("Пустая компания", "Без вакансий");
        Company filledCompany = persistCompany("Рабочая компания", "С вакансиями");
        persistVacancy(filledCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");

        driver.get(baseUrl() + "/companies/" + emptyCompany.getId());
        assertPageContains("У компании пока нет вакансий.");

        driver.get(baseUrl() + "/companies/" + filledCompany.getId() + "?position=DevOps");
        assertPageContains("По текущим фильтрам вакансии не найдены.");
        assertPageNotContains("Java-разработчик");
    }
}
