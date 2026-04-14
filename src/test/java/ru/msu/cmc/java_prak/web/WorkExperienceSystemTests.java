package ru.msu.cmc.java_prak.web;

import java.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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

        driver.get(baseUrl() + "/people/" + person.getId());

        assertPageContains("История работы пока не заполнена.");
        driver.findElement(By.linkText("Создать первую запись")).click();

        assertCurrentUrlEndsWith("/people/" + person.getId() + "/work-experiences/new");
        assertPageContains("Новая запись о работе");
    }

    @Test
    public void workExperienceFormsShouldValidateOverlapUpdateAndDelete() {
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

        driver.get(baseUrl() + "/people/" + person.getId() + "/work-experiences/new");
        element("save-work-experience-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите компанию.");
        assertPageContains("Укажите должность.");
        assertPageContains("Укажите зарплату.");
        assertPageContains("Укажите дату начала.");

        selectById("work-company-input").selectByVisibleText("Яндекс");
        clearAndType("work-position-input", "Системный аналитик");
        clearAndType("work-salary-input", "190000");
        clearAndType("work-start-input", "2020-06-01");
        clearAndType("work-end-input", "2020-11-30");
        element("save-work-experience-button").click();

        assertPageContains("Период работы пересекается с другой записью этого человека.");

        clearAndType("work-start-input", "2021-02-01");
        clearAndType("work-end-input", "2021-12-31");
        element("save-work-experience-button").click();

        assertPageContains("Запись о работе успешно добавлена.");
        assertPageContains("Системный аналитик");

        WorkExperience createdWorkExperience = workExperienceDao.findByPersonIdOrderByStartDateDesc(person.getId())
                .stream()
                .filter(workExperience -> "Системный аналитик".equals(workExperience.getPosition()))
                .findFirst()
                .orElseThrow();

        driver.get(baseUrl() + "/people/" + person.getId() + "/work-experiences/" + createdWorkExperience.getId() + "/edit");

        clearAndType("work-position-input", "Ведущий аналитик");
        element("save-work-experience-button").click();

        assertPageContains("Запись о работе обновлена.");
        assertPageContains("Ведущий аналитик");

        driver.get(baseUrl() + "/people/" + person.getId() + "/work-experiences/" + createdWorkExperience.getId() + "/edit");
        element("delete-work-experience-button").click();

        assertPageContains("Запись о работе удалена.");
        assertTrue(
                workExperienceDao.findByPersonIdOrderByStartDateDesc(person.getId())
                        .stream()
                        .noneMatch(workExperience -> workExperience.getId().equals(createdWorkExperience.getId()))
        );
    }

    @Test
    public void workExperienceFormsShouldRejectNegativeSalaryAndInvalidDateRange() {
        Person person = persistPerson(
                "Кузнецов Денис Андреевич",
                "Высшее техническое",
                false,
                null,
                null
        );
        Company company = persistCompany("VK", "Социальная сеть");

        driver.get(baseUrl() + "/people/" + person.getId() + "/work-experiences/new");

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
