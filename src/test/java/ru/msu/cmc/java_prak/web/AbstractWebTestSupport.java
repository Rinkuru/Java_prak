package ru.msu.cmc.java_prak.web;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.model.WorkExperience;

import static org.testng.Assert.assertEquals;

/**
 * Общая база для web- и integration-тестов.
 */
public abstract class AbstractWebTestSupport extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PersonDao personDao;

    @Autowired
    protected CompanyDao companyDao;

    @Autowired
    protected VacancyDao vacancyDao;

    @Autowired
    protected WorkExperienceDao workExperienceDao;

    @BeforeMethod(alwaysRun = true)
    public void resetDatabase() {
        jdbcTemplate.execute("DELETE FROM work_experience");
        jdbcTemplate.execute("DELETE FROM vacancy");
        jdbcTemplate.execute("DELETE FROM person");
        jdbcTemplate.execute("DELETE FROM company");
    }

    protected String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    protected HttpResponse<String> postForm(String path, String... keyValuePairs) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData(keyValuePairs)))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected String formData(String... keyValuePairs) {
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

    protected Person persistPerson(
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

    protected Company persistCompany(String name, String description) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        return companyDao.save(company);
    }

    protected Vacancy persistVacancy(
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

    protected WorkExperience persistWorkExperience(
            Person person,
            Company company,
            String position,
            String salary,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Person managedPerson = personDao.findById(person.getId()).orElseThrow();
        Company managedCompany = companyDao.findById(company.getId()).orElseThrow();

        WorkExperience workExperience = new WorkExperience();
        workExperience.setPerson(managedPerson);
        workExperience.setCompany(managedCompany);
        workExperience.setPosition(position);
        workExperience.setSalary(new BigDecimal(salary));
        workExperience.setStartDate(startDate);
        workExperience.setEndDate(endDate);
        return workExperienceDao.save(workExperience);
    }

    protected void addWorkExperience(Person person, Company company, String position) {
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

    protected void assertEducationOptions(Select educationSelect) {
        assertEquals(educationSelect.getOptions().size(), 5);
        assertEquals(educationSelect.getOptions().get(0).getText(), "Выберите образование");
        assertEquals(educationSelect.getOptions().get(1).getText(), "Среднее специальное");
        assertEquals(educationSelect.getOptions().get(2).getText(), "Высшее медицинское");
        assertEquals(educationSelect.getOptions().get(3).getText(), "Высшее техническое");
        assertEquals(educationSelect.getOptions().get(4).getText(), "Высшее");
    }
}
