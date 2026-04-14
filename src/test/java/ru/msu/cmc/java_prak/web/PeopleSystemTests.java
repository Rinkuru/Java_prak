package ru.msu.cmc.java_prak.web;

import java.math.BigDecimal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Системные Selenium-тесты раздела "Люди".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PeopleSystemTests extends AbstractSeleniumSystemTest {

    @Test
    public void peopleListShouldFilterByEducationAndOpenPersonCard() {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        Person technicalPerson = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "220000.00"
        );
        addWorkExperience(technicalPerson, company, "Backend-разработчик");

        persistPerson(
                "Петров Пётр Сергеевич",
                "Высшее медицинское",
                false,
                null,
                null
        );

        openPeopleListPage();

        selectById("filter-education").selectByVisibleText("Высшее техническое");
        element("filter-submit").click();

        assertPageContains("Иванов Алексей Дмитриевич");
        assertPageNotContains("Петров Пётр Сергеевич");

        element("person-link-" + technicalPerson.getId()).click();

        assertPageContains("Карточка человека");
        assertPageContains("ВКонтакте");
        assertPageContains("Backend-разработчик");
    }

    @Test
    public void peopleListShouldApplyCombinedFiltersAndSortBySalary() {
        Company vk = persistCompany("VK", "Социальная сеть");
        Company yandex = persistCompany("Яндекс", "Технологии");

        Person firstMatch = persistPerson(
                "Смирнов Андрей Викторович",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "220000.00"
        );
        addWorkExperience(firstMatch, vk, "Backend-разработчик");

        Person secondMatch = persistPerson(
                "Крылов Артём Сергеевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "260000.00"
        );
        addWorkExperience(secondMatch, vk, "Backend-разработчик");

        Person wrongCompany = persistPerson(
                "Петров Кирилл Олегович",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "240000.00"
        );
        addWorkExperience(wrongCompany, yandex, "Backend-разработчик");

        Person wrongPosition = persistPerson(
                "Иванова Елена Сергеевна",
                "Высшее техническое",
                true,
                "Системный аналитик",
                "230000.00"
        );
        addWorkExperience(wrongPosition, vk, "Системный аналитик");

        openPeopleListPage();

        selectById("filter-education").selectByVisibleText("Высшее техническое");
        selectById("filter-status").selectByValue("true");
        clearAndType("filter-min-salary", "200000");
        selectById("filter-company").selectByVisibleText("VK");
        clearAndType("filter-position", "Backend-разработчик");
        selectById("filter-sort").selectByValue("desc");
        element("filter-submit").click();

        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[1]/td[1]")).getText(),
                secondMatch.getFullName()
        );
        assertEquals(
                driver.findElement(By.xpath("//table[@class='data-table']/tbody/tr[2]/td[1]")).getText(),
                firstMatch.getFullName()
        );
        assertPageNotContains(wrongCompany.getFullName());
        assertPageNotContains(wrongPosition.getFullName());
    }

    @Test
    public void peopleListShouldShowErrorWhenSalaryRangeIsInvalid() {
        persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "220000.00"
        );

        openPeopleListPage();

        clearAndType("filter-min-salary", "300000");
        clearAndType("filter-max-salary", "200000");
        element("filter-submit").click();

        assertPageContains("Значение \"Зарплата от\" не может быть больше значения \"Зарплата до\".");
        assertPageNotContains("По текущим фильтрам ничего не найдено.");
        assertPageNotContains("Иванов Алексей Дмитриевич");
        assertEquals(element("filter-min-salary").getAttribute("value"), "300000");
        assertEquals(element("filter-max-salary").getAttribute("value"), "200000");
    }

    @Test
    public void createPersonShouldShowValidationErrors() {
        openPersonCreateFormThroughList();

        selectById("status-input").selectByValue("true");
        element("save-person-button").click();

        assertPageContains("Форма содержит ошибки");
        assertPageContains("Укажите ФИО.");
        assertPageContains("Укажите образование.");
        assertPageContains("Если человек ищет работу, укажите желаемую должность и зарплату.");
    }

    @Test
    public void createPersonShouldRejectNegativeSalaryAndTooLongFields() {
        openPersonCreateFormThroughList();

        clearAndType("fullName-input", "А".repeat(256));
        clearAndType("homeAddress-input", "Б".repeat(501));
        selectById("education-input").selectByVisibleText("Высшее техническое");
        selectById("status-input").selectByValue("true");
        clearAndType("desiredPosition-input", "П".repeat(256));
        clearAndType("desiredSalary-input", "-1");
        element("save-person-button").click();

        assertPageContains("ФИО должно быть не длиннее 255 символов.");
        assertPageContains("Домашний адрес должен быть не длиннее 500 символов.");
        assertPageContains("Желаемая должность должна быть не длиннее 255 символов.");
        assertPageContains("Желаемая зарплата не может быть отрицательной.");
    }

    @Test
    public void createPersonShouldSaveValidPerson() {
        openPersonCreateFormThroughList();

        clearAndType("fullName-input", "Сидоров Максим Игоревич");
        clearAndType("homeAddress-input", "Москва, Ленинские горы, 1");
        selectById("education-input").selectByVisibleText("Высшее техническое");
        selectById("status-input").selectByValue("true");
        clearAndType("desiredPosition-input", "Java-разработчик");
        clearAndType("desiredSalary-input", "240000");
        element("save-person-button").click();

        assertPageContains("Человек успешно добавлен.");
        assertPageContains("Сидоров Максим Игоревич");
        assertEquals(personDao.findAllOrderByFullName().size(), 1);
        assertEquals(personDao.findAllOrderByFullName().get(0).getDesiredPosition(), "Java-разработчик");
    }

    @Test
    public void editPersonShouldUpdateExistingPerson() {
        Person person = persistPerson(
                "Орлов Денис Павлович",
                "Высшее техническое",
                false,
                null,
                null
        );

        openPersonEditFormThroughCard(person.getId());

        clearAndType("homeAddress-input", "Москва, новый адрес");
        selectById("education-input").selectByVisibleText("Высшее");
        selectById("status-input").selectByValue("true");
        clearAndType("desiredPosition-input", "Системный аналитик");
        clearAndType("desiredSalary-input", "180000");
        element("save-person-button").click();

        assertPageContains("Данные человека обновлены.");
        assertPageContains("Москва, новый адрес");
        assertPageContains("Системный аналитик");

        Person updatedPerson = personDao.findById(person.getId()).orElseThrow();
        assertEquals(updatedPerson.getEducation(), "Высшее");
        assertTrue(updatedPerson.isStatus());
        assertEquals(updatedPerson.getDesiredPosition(), "Системный аналитик");
        assertEquals(updatedPerson.getDesiredSalary(), new BigDecimal("180000.00"));
    }

    @Test
    public void personCardShouldDeletePersonAndReturnToList() {
        Person person = persistPerson(
                "Лебедев Сергей Викторович",
                "Высшее техническое",
                false,
                null,
                null
        );

        openPersonCardThroughList(person.getId());
        element("delete-person-button").click();

        assertTrue(driver.getCurrentUrl().contains("/people"));
        assertPageContains("Человек удалён.");
        assertTrue(personDao.findById(person.getId()).isEmpty());
    }

    @Test
    public void personFormsShouldRenderReferenceEducationOptions() {
        Person person = persistPerson(
                "Орлов Денис Павлович",
                "Высшее техническое",
                false,
                null,
                null
        );

        openPersonCreateFormThroughList();
        assertEducationOptions(new Select(element("education-input")));

        openPersonEditFormThroughCard(person.getId());
        Select educationSelect = new Select(element("education-input"));
        assertEducationOptions(educationSelect);
        assertEquals(educationSelect.getFirstSelectedOption().getText(), "Высшее техническое");
    }

    @Test
    public void editPersonShouldNotSilentlyKeepLegacyInvalidEducation() {
        Person person = persistPerson(
                "Лебедева Ирина Сергеевна",
                "Среднее специальное2",
                false,
                null,
                null
        );

        openPersonEditFormThroughCard(person.getId());

        Select educationSelect = new Select(element("education-input"));
        assertEducationOptions(educationSelect);
        assertEquals(educationSelect.getFirstSelectedOption().getAttribute("value"), "");
        assertPageNotContains("Среднее специальное2");

        element("save-person-button").click();

        assertPageContains("Укажите образование.");
        Person unchangedPerson = personDao.findById(person.getId()).orElseThrow();
        assertEquals(unchangedPerson.getEducation(), "Среднее специальное2");
    }
}
