package ru.msu.cmc.java_prak.web;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.model.WorkExperience;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Системные Selenium-тесты для первой web-вертикали "Главная + Люди".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonSystemTests extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PersonDao personDao;

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private VacancyDao vacancyDao;

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM work_experience");
        jdbcTemplate.execute("DELETE FROM vacancy");
        jdbcTemplate.execute("DELETE FROM person");
        jdbcTemplate.execute("DELETE FROM company");
        driver = new HtmlUnitDriver(true);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void homePageShouldOpenAndNavigateToPeopleList() {
        persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "220000.00"
        );

        driver.get(baseUrl() + "/");

        assertTrue(driver.getPageSource().contains("Веб-интерфейс кадрового агентства"));
        driver.findElement(By.id("home-people-link")).click();

        assertTrue(driver.getCurrentUrl().endsWith("/people"));
        assertTrue(driver.getPageSource().contains("Список резюме"));
        assertTrue(driver.getPageSource().contains("Иванов Алексей Дмитриевич"));
    }

    @Test
    public void homePageShouldOpenCompaniesListAndCompanyCard() {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        persistVacancy(company, "Java-разработчик", "240000.00", true, "Высшее техническое");

        driver.get(baseUrl() + "/");
        driver.findElement(By.id("home-companies-link")).click();

        assertTrue(driver.getCurrentUrl().endsWith("/companies"));
        assertTrue(driver.getPageSource().contains("Список компаний"));
        driver.findElement(By.id("company-link-" + company.getId())).click();

        assertTrue(driver.getPageSource().contains("Карточка компании"));
        assertTrue(driver.getPageSource().contains("Java-разработчик"));
    }

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

        driver.get(baseUrl() + "/people");

        Select educationFilter = new Select(driver.findElement(By.id("filter-education")));
        educationFilter.selectByVisibleText("Высшее техническое");
        driver.findElement(By.id("filter-submit")).click();

        assertTrue(driver.getPageSource().contains("Иванов Алексей Дмитриевич"));
        assertFalse(driver.getPageSource().contains("Петров Пётр Сергеевич"));

        driver.findElement(By.id("person-link-" + technicalPerson.getId())).click();

        assertTrue(driver.getPageSource().contains("Карточка человека"));
        assertTrue(driver.getPageSource().contains("ВКонтакте"));
        assertTrue(driver.getPageSource().contains("Backend-разработчик"));
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

        driver.get(baseUrl() + "/people");

        WebElement minSalaryInput = driver.findElement(By.id("filter-min-salary"));
        minSalaryInput.clear();
        minSalaryInput.sendKeys("300000");

        WebElement maxSalaryInput = driver.findElement(By.id("filter-max-salary"));
        maxSalaryInput.clear();
        maxSalaryInput.sendKeys("200000");

        driver.findElement(By.id("filter-submit")).click();

        assertTrue(driver.getPageSource().contains("Значение \"Зарплата от\" не может быть больше значения \"Зарплата до\"."));
        assertFalse(driver.getPageSource().contains("По текущим фильтрам ничего не найдено."));
        assertFalse(driver.getPageSource().contains("Иванов Алексей Дмитриевич"));
        assertEquals(driver.findElement(By.id("filter-min-salary")).getAttribute("value"), "300000");
        assertEquals(driver.findElement(By.id("filter-max-salary")).getAttribute("value"), "200000");
    }

    @Test
    public void personCardShouldOpenWorkExperienceFormAndMatchesPage() {
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
        driver.findElement(By.id("add-work-experience-link")).click();

        assertTrue(driver.getCurrentUrl().endsWith("/people/" + person.getId() + "/work-experiences/new"));
        assertTrue(driver.getPageSource().contains("Новая запись о работе"));

        driver.get(baseUrl() + "/people/" + person.getId());
        driver.findElement(By.id("person-matches-link")).click();

        assertTrue(driver.getCurrentUrl().endsWith("/people/" + person.getId() + "/matches"));
        assertTrue(driver.getPageSource().contains("Подходящие вакансии"));
        assertTrue(driver.getPageSource().contains("Java-разработчик"));
    }

    @Test
    public void createPersonShouldShowValidationErrors() {
        driver.get(baseUrl() + "/people/new");

        Select statusSelect = new Select(driver.findElement(By.id("status-input")));
        statusSelect.selectByValue("true");
        driver.findElement(By.id("save-person-button")).click();

        assertTrue(driver.getPageSource().contains("Форма содержит ошибки"));
        assertTrue(driver.getPageSource().contains("Укажите ФИО."));
        assertTrue(driver.getPageSource().contains("Укажите образование."));
        assertTrue(driver.getPageSource().contains("Если человек ищет работу, укажите желаемую должность и зарплату."));
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

        driver.get(baseUrl() + "/people/new");
        assertEducationOptions(new Select(driver.findElement(By.id("education-input"))));

        driver.get(baseUrl() + "/people/" + person.getId() + "/edit");
        Select educationSelect = new Select(driver.findElement(By.id("education-input")));
        assertEducationOptions(educationSelect);
        assertEquals(educationSelect.getFirstSelectedOption().getText(), "Высшее техническое");
    }

    @Test
    public void createPersonShouldSaveValidPerson() {
        driver.get(baseUrl() + "/people/new");

        driver.findElement(By.id("fullName-input")).sendKeys("Сидоров Максим Игоревич");
        driver.findElement(By.id("homeAddress-input")).sendKeys("Москва, Ленинские горы, 1");
        new Select(driver.findElement(By.id("education-input"))).selectByVisibleText("Высшее техническое");
        new Select(driver.findElement(By.id("status-input"))).selectByValue("true");
        driver.findElement(By.id("desiredPosition-input")).sendKeys("Java-разработчик");
        driver.findElement(By.id("desiredSalary-input")).sendKeys("240000");
        driver.findElement(By.id("save-person-button")).click();

        assertTrue(driver.getPageSource().contains("Человек успешно добавлен."));
        assertTrue(driver.getPageSource().contains("Сидоров Максим Игоревич"));
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

        driver.get(baseUrl() + "/people/" + person.getId() + "/edit");

        WebElement addressInput = driver.findElement(By.id("homeAddress-input"));
        addressInput.clear();
        addressInput.sendKeys("Москва, новый адрес");

        new Select(driver.findElement(By.id("education-input"))).selectByVisibleText("Высшее");
        new Select(driver.findElement(By.id("status-input"))).selectByValue("true");
        driver.findElement(By.id("desiredPosition-input")).sendKeys("Системный аналитик");
        driver.findElement(By.id("desiredSalary-input")).sendKeys("180000");
        driver.findElement(By.id("save-person-button")).click();

        assertTrue(driver.getPageSource().contains("Данные человека обновлены."));
        assertTrue(driver.getPageSource().contains("Москва, новый адрес"));
        assertTrue(driver.getPageSource().contains("Системный аналитик"));

        Person updatedPerson = personDao.findById(person.getId()).orElseThrow();
        assertEquals(updatedPerson.getEducation(), "Высшее");
        assertTrue(updatedPerson.isStatus());
        assertEquals(updatedPerson.getDesiredPosition(), "Системный аналитик");
        assertEquals(updatedPerson.getDesiredSalary(), new BigDecimal("180000.00"));
    }

    @Test
    public void createPersonShouldRejectInvalidEducationOnDirectPost() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/people"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData(
                        "fullName", "Тестовый Человек",
                        "education", "Среднее специальное2",
                        "status", "false"
                )))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Выберите образование из списка."));
        assertEquals(personDao.findAllOrderByFullName().size(), 0);
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

        driver.get(baseUrl() + "/people/" + person.getId() + "/edit");

        Select educationSelect = new Select(driver.findElement(By.id("education-input")));
        assertEducationOptions(educationSelect);
        assertEquals(educationSelect.getFirstSelectedOption().getAttribute("value"), "");
        assertFalse(driver.getPageSource().contains("Среднее специальное2"));

        driver.findElement(By.id("save-person-button")).click();

        assertTrue(driver.getPageSource().contains("Укажите образование."));
        Person unchangedPerson = personDao.findById(person.getId()).orElseThrow();
        assertEquals(unchangedPerson.getEducation(), "Среднее специальное2");
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
        driver.findElement(By.id("vacancy-matches-link")).click();

        assertTrue(driver.getCurrentUrl().endsWith("/vacancies/" + vacancy.getId() + "/matches"));
        assertTrue(driver.getPageSource().contains("Подходящие резюме"));
        assertTrue(driver.getPageSource().contains("Соколова Мария Андреевна"));
    }

    @Test
    public void unexpectedErrorsShouldRenderFriendlyPage() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/test/fail"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 500);
        assertTrue(response.body().contains("Что-то пошло не так"));
        assertTrue(response.body().contains("/test/fail"));
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private void assertEducationOptions(Select educationSelect) {
        assertEquals(educationSelect.getOptions().size(), 5);
        assertEquals(educationSelect.getOptions().get(0).getText(), "Выберите образование");
        assertEquals(educationSelect.getOptions().get(1).getText(), "Среднее специальное");
        assertEquals(educationSelect.getOptions().get(2).getText(), "Высшее медицинское");
        assertEquals(educationSelect.getOptions().get(3).getText(), "Высшее техническое");
        assertEquals(educationSelect.getOptions().get(4).getText(), "Высшее");
    }

    private String formData(String... keyValuePairs) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (body.length() > 0) {
                body.append('&');
            }

            body.append(URLEncoder.encode(keyValuePairs[i], StandardCharsets.UTF_8));
            body.append('=');
            body.append(URLEncoder.encode(keyValuePairs[i + 1], StandardCharsets.UTF_8));
        }
        return body.toString();
    }

    private Person persistPerson(
            String fullName,
            String education,
            boolean status,
            String desiredPosition,
            String desiredSalary
    ) {
        Person person = new Person();
        person.setFullName(fullName);
        person.setEducation(education);
        person.setStatus(status);
        person.setDesiredPosition(desiredPosition);
        person.setDesiredSalary(desiredSalary == null ? null : new BigDecimal(desiredSalary));
        return personDao.save(person);
    }

    private Company persistCompany(String name, String description) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        return companyDao.save(company);
    }

    private Vacancy persistVacancy(
            Company company,
            String position,
            String salary,
            boolean status,
            String requiredEducation
    ) {
        Vacancy vacancy = new Vacancy();
        vacancy.setCompany(company);
        vacancy.setPosition(position);
        vacancy.setSalary(new BigDecimal(salary));
        vacancy.setStatus(status);
        vacancy.setRequiredEducation(requiredEducation);
        return vacancyDao.save(vacancy);
    }

    private void addWorkExperience(Person person, Company company, String position) {
        Person managedPerson = personDao.findCardById(person.getId()).orElseThrow();

        WorkExperience workExperience = new WorkExperience();
        workExperience.setPerson(managedPerson);
        workExperience.setCompany(company);
        workExperience.setPosition(position);
        workExperience.setSalary(new BigDecimal("190000.00"));
        workExperience.setStartDate(LocalDate.of(2022, 1, 1));

        managedPerson.addWorkExperience(workExperience);
        personDao.update(managedPerson);
    }
}
