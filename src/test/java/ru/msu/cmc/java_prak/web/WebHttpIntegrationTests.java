package ru.msu.cmc.java_prak.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration/HTTP-тесты для веток, которые нельзя честно воспроизвести через браузерный UI.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebHttpIntegrationTests extends AbstractWebTestSupport {

    @Test
    public void createPersonShouldRejectInvalidEducationOnDirectPost() throws Exception {
        HttpResponse<String> response = postForm(
                "/people",
                "fullName", "Тестовый Человек",
                "education", "Среднее специальное2",
                "status", "false"
        );

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Выберите образование из списка."));
        assertEquals(personDao.findAllOrderByFullName().size(), 0);
    }

    @Test
    public void vacancyCreateShouldRejectCompanyBindingMismatchOnDirectPost() throws Exception {
        Company firstCompany = persistCompany("VK", "Социальная сеть");
        Company secondCompany = persistCompany("Яндекс", "Технологии");

        HttpResponse<String> response = postForm(
                "/companies/" + firstCompany.getId() + "/vacancies",
                "companyId", String.valueOf(secondCompany.getId()),
                "position", "Java-разработчик",
                "salary", "250000",
                "requiredEducation", "Высшее техническое",
                "status", "true"
        );

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Компания вакансии не совпадает с выбранной карточкой компании."));
        assertEquals(vacancyDao.findAllOrderById().size(), 0);
    }

    @Test
    public void companiesListShouldReportInvalidVacancyFiltersOnDirectGet() throws Exception {
        Company javaCompany = persistCompany("ВКонтакте", "IT-компания");
        Company medicalCompany = persistCompany("Городская клиника", "Медицина");
        persistVacancy(javaCompany, "Java-разработчик", "240000.00", true, "Высшее техническое");
        persistVacancy(medicalCompany, "Врач-терапевт", "180000.00", true, "Высшее медицинское");

        HttpResponse<String> response = getPage("/companies?position=java&minSalary=abc&onlyWithOpenVacancies=oops");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Передано некорректное значение фильтра"));
        assertTrue(response.body().contains("Зарплата от"));
        assertTrue(response.body().contains("Параметр не применён."));
        assertTrue(response.body().contains("Передано некорректное значение фильтра показа только компаний с открытыми вакансиями. Применено значение по умолчанию."));
        assertTrue(response.body().contains("ВКонтакте"));
        assertTrue(!response.body().contains("Городская клиника"));
    }

    @Test
    public void companyCardShouldReportInvalidVacancyFiltersOnDirectGet() throws Exception {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        persistVacancy(company, "Java-разработчик", "240000.00", true, "Высшее техническое");

        HttpResponse<String> response = getPage(
                "/companies/" + company.getId() + "?position=java&minSalary=abc&onlyActive=oops"
        );

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Передано некорректное значение фильтра"));
        assertTrue(response.body().contains("Зарплата от"));
        assertTrue(response.body().contains("Параметр не применён."));
        assertTrue(response.body().contains("Передано некорректное значение фильтра показа только открытых вакансий. Применено значение по умолчанию."));
        assertTrue(response.body().contains("Java-разработчик"));
    }

    @Test
    public void personMatchesShouldReportInvalidQueryParamsOnDirectGet() throws Exception {
        Person person = persistPerson(
                "Крылов Артем Сергеевич",
                "Высшее техническое",
                true,
                null,
                null
        );

        HttpResponse<String> response = getPage("/people/" + person.getId() + "/matches?onlyActive=oops&sort=sideways");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Передано некорректное значение фильтра показа только открытых вакансий. Применено значение по умолчанию."));
        assertTrue(response.body().contains("Передан некорректный параметр сортировки. Применено значение по умолчанию."));
        assertTrue(response.body().contains("Для содержательного подбора у человека должны быть заполнены желаемая должность и желаемая зарплата."));
    }

    @Test
    public void personMatchesShouldAllowClosedVacanciesOnDirectGetWhenExplicitlyRequested() throws Exception {
        Person person = persistPerson(
                "Смирнов Андрей Викторович",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "200000.00"
        );
        Company openCompany = persistCompany("Открытая компания", "Открытая вакансия");
        Company closedCompany = persistCompany("Закрытая компания", "Закрытая вакансия");
        persistVacancy(openCompany, "Java-разработчик", "300000.00", true, "Высшее техническое");
        persistVacancy(closedCompany, "Java-разработчик", "240000.00", false, "Высшее техническое");

        HttpResponse<String> response = getPage("/people/" + person.getId() + "/matches?onlyActive=false&sort=asc");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Открытая компания"));
        assertTrue(response.body().contains("Закрытая компания"));
        assertTrue(response.body().indexOf("240000.00") < response.body().indexOf("300000.00"));
    }

    @Test
    public void vacancyMatchesShouldReportInvalidQueryParamsOnDirectGet() throws Exception {
        Company company = persistCompany("T-Банк", "Финтех");
        Vacancy vacancy = persistVacancy(company, "Аналитик", "210000.00", false, "");

        HttpResponse<String> response = getPage(
                "/vacancies/" + vacancy.getId() + "/matches?onlyLookingForJob=wrong&sort=bad"
        );

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Передано некорректное значение фильтра показа только людей, которые ищут работу. Применено значение по умолчанию."));
        assertTrue(response.body().contains("Передан некорректный параметр сортировки. Применено значение по умолчанию."));
        assertTrue(response.body().contains("Подбор выполняется для закрытой вакансии."));
    }

    @Test
    public void vacancyMatchesShouldAllowPeopleNotLookingForJobOnDirectGetWhenExplicitlyRequested() throws Exception {
        Company company = persistCompany("T-Банк", "Финтех");
        Vacancy vacancy = persistVacancy(company, "Аналитик", "210000.00", true, "Высшее экономическое");
        persistPerson(
                "Соколова Мария Андреевна",
                "Высшее экономическое",
                true,
                "Аналитик",
                "180000.00"
        );
        persistPerson(
                "Петров Кирилл Олегович",
                "Высшее экономическое",
                false,
                "Аналитик",
                "170000.00"
        );

        HttpResponse<String> response = getPage(
                "/vacancies/" + vacancy.getId() + "/matches?onlyLookingForJob=false&sort=desc"
        );

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Соколова Мария Андреевна"));
        assertTrue(response.body().contains("Петров Кирилл Олегович"));
        assertTrue(response.body().indexOf("180000.00") < response.body().indexOf("170000.00"));
    }

    @Test
    public void unexpectedErrorsShouldRenderFriendlyPageWith500Status() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/test/fail"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 500);
        assertTrue(response.body().contains("Что-то пошло не так"));
        assertTrue(response.body().contains("/test/fail"));
    }
}
