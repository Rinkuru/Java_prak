package ru.msu.cmc.java_prak.web;

import java.time.LocalDate;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.WorkExperience;

import static org.testng.Assert.assertTrue;

/**
 * Системные Selenium-тесты сценариев истории работы.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkExperienceSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void personCardShouldShowEmptyHistoryStateAndOpenCreateForm() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );

        openPersonCardThroughList(person.getId());

        assertPageContains("История работы пока не заполнена.");
        element("add-work-experience-link").click();

        assertCurrentUrlEndsWith("/people/" + person.getId() + "/work-experiences/new");
        assertPageContains("Новая запись о работе");
    }

    @Test
    public void workExperienceCreateShouldValidateRequiredFields() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );
        persistCompany("VK", "Социальная сеть");

        openWorkExperienceCreateFormThroughPersonCard(person.getId());
        element("save-work-experience-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите компанию.");
        assertPageContains("Укажите должность.");
        assertPageContains("Укажите зарплату.");
        assertPageContains("Укажите дату начала.");
    }

    @Test
    public void workExperienceCreateShouldRejectOverlappingPeriod() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );
        Company firstCompany = persistCompany("VK", "Социальная сеть");
        Company secondCompany = persistCompany("Яндекс", "Технологии");
        persistWorkExperience(
                person,
                firstCompany,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31)
        );

        openWorkExperienceCreateFormThroughPersonCard(person.getId());

        selectById("work-company-input").selectByVisibleText("Яндекс");
        clearAndType("work-position-input", "Системный аналитик");
        clearAndType("work-salary-input", "190000");
        clearAndType("work-start-input", "2020-06-01");
        clearAndType("work-end-input", "2020-11-30");
        element("save-work-experience-button").click();

        assertPageContains("Период работы пересекается с другой записью этого человека.");
    }

    @Test
    public void workExperienceCreateShouldSaveValidRecord() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );
        Company company = persistCompany("Яндекс", "Технологии");

        openWorkExperienceCreateFormThroughPersonCard(person.getId());

        selectById("work-company-input").selectByVisibleText("Яндекс");
        clearAndType("work-position-input", "Системный аналитик");
        clearAndType("work-salary-input", "190000");
        clearAndType("work-start-input", "2021-02-01");
        clearAndType("work-end-input", "2021-12-31");
        element("save-work-experience-button").click();

        assertPageContains("Запись о работе успешно добавлена.");
        assertPageContains("Системный аналитик");
        assertPageContains(company.getName());
    }

    @Test
    public void workExperienceEditShouldUpdateRecord() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );
        Company company = persistCompany("VK", "Социальная сеть");
        WorkExperience workExperience = persistWorkExperience(
                person,
                company,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31)
        );

        openWorkExperienceEditFormThroughPersonCard(person.getId(), workExperience.getId());

        clearAndType("work-position-input", "Ведущий аналитик");
        element("save-work-experience-button").click();

        assertPageContains("Запись о работе обновлена.");
        assertPageContains("Ведущий аналитик");
    }

    @Test
    public void workExperienceEditShouldDeleteRecord() {
        Person person = persistPerson(
                "Сидоров Максим Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "240000.00"
        );
        Company company = persistCompany("VK", "Социальная сеть");
        WorkExperience workExperience = persistWorkExperience(
                person,
                company,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31)
        );

        openWorkExperienceEditFormThroughPersonCard(person.getId(), workExperience.getId());
        element("delete-work-experience-button").click();

        assertPageContains("Запись о работе удалена.");
        assertTrue(
                workExperienceDao.findByPersonIdOrderByStartDateDesc(person.getId())
                        .stream()
                        .noneMatch(currentWorkExperience -> currentWorkExperience.getId().equals(workExperience.getId()))
        );
    }

    @Test
    public void workExperienceCreateShouldRejectNegativeSalaryAndInvalidDateRange() {
        Person person = persistPerson(
                "Кузнецов Денис Андреевич",
                "Высшее техническое",
                false,
                null,
                null
        );
        persistCompany("VK", "Социальная сеть");

        openWorkExperienceCreateFormThroughPersonCard(person.getId());

        selectById("work-company-input").selectByVisibleText("VK");
        clearAndType("work-position-input", "Д".repeat(256));
        clearAndType("work-salary-input", "-1");
        clearAndType("work-start-input", "2022-12-31");
        clearAndType("work-end-input", "2022-01-01");
        element("save-work-experience-button").click();

        assertPageContains("Должность должна быть не длиннее 255 символов.");
        assertPageContains("Зарплата не может быть отрицательной.");
        assertPageContains("Дата окончания не может быть раньше даты начала.");
    }
}
