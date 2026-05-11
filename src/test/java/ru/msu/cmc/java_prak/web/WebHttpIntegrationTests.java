package ru.msu.cmc.java_prak.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Test
    public void companySuggestionsShouldSearchCaseInsensitivelyAndIgnoreShortQuery() throws Exception {
        persistCompany("Alpha Tech", "IT");
        persistCompany("Beta Labs", "IT");
        persistCompany("Gamma Retail", "Retail");

        assertEquals(getJsonStringList("/api/suggestions/companies?q=TECH"), List.of("Alpha Tech"));
        assertEquals(getJsonStringList("/api/suggestions/companies?q=t"), List.of());
        assertEquals(getJsonStringList("/api/suggestions/companies"), List.of());
    }

    @Test
    public void peopleSuggestionsShouldReturnMatchingFullNames() throws Exception {
        persistPerson("Ann Able", "Высшее", true, "Аналитик", "160000.00");
        persistPerson("Anna Smith", "Высшее техническое", true, "Java-разработчик", "220000.00");
        persistPerson("Boris Stone", "Высшее", false, null, null);

        assertEquals(getJsonStringList("/api/suggestions/people?q=ann"), List.of("Ann Able", "Anna Smith"));
    }

    @Test
    public void vacancyPositionSuggestionsShouldReturnDistinctLimitedValues() throws Exception {
        Company company = persistCompany("Alpha Tech", "IT");
        persistVacancy(company, "Java 09", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 01", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 01", "200000.00", true, "Высшее");
        persistVacancy(company, "Java 02", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 03", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 04", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 05", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 06", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 07", "190000.00", true, "Высшее");
        persistVacancy(company, "Java 08", "190000.00", true, "Высшее");

        List<String> suggestions = getJsonStringList("/api/suggestions/positions?source=vacancy&q=java");

        assertEquals(suggestions, List.of(
                "Java 01",
                "Java 02",
                "Java 03",
                "Java 04",
                "Java 05",
                "Java 06",
                "Java 07",
                "Java 08"
        ));
        assertEquals(new HashSet<>(suggestions).size(), suggestions.size());
    }

    @Test
    public void positionSuggestionsShouldUseSelectedSource() throws Exception {
        Company company = persistCompany("Alpha Tech", "IT");
        Person person = persistPerson(
                "Daria Lane",
                "Высшее",
                true,
                "QA Analyst",
                "170000.00"
        );
        persistVacancy(company, "QA Lead", "230000.00", true, "Высшее");
        persistWorkExperience(
                person,
                company,
                "QA Engineer",
                "150000.00",
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2023, 1, 1)
        );

        assertEquals(getJsonStringList("/api/suggestions/positions?source=vacancy&q=qa"), List.of("QA Lead"));
        assertEquals(getJsonStringList("/api/suggestions/positions?source=work&q=qa"), List.of("QA Engineer"));
        assertEquals(getJsonStringList("/api/suggestions/positions?source=desired&q=qa"), List.of("QA Analyst"));
    }

    @Test
    public void candidatePositionSuggestionsShouldMergeDesiredAndWorkPositionsOnly() throws Exception {
        Company company = persistCompany("Candidate Source Test", "IT");
        Person person = persistPerson(
                "Elena Candidate",
                "Высшее",
                true,
                "Candidate 02",
                "170000.00"
        );
        persistPerson(
                "Maria Candidate",
                "Высшее",
                true,
                "Candidate 01",
                "180000.00"
        );
        persistPerson(
                "Olga Candidate",
                "Высшее",
                true,
                "Candidate 01",
                "190000.00"
        );
        for (int i = 4; i <= 9; i++) {
            persistPerson(
                    "Candidate Person " + i,
                    "Высшее",
                    true,
                    "Candidate 0" + i,
                    "180000.00"
            );
        }
        persistWorkExperience(
                person,
                company,
                "Candidate 03",
                "150000.00",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 1, 1)
        );
        persistVacancy(company, "Candidate 00", "230000.00", true, "Высшее");

        assertEquals(getJsonStringList("/api/suggestions/positions?source=candidate&q=candidate"), List.of(
                "Candidate 01",
                "Candidate 02",
                "Candidate 03",
                "Candidate 04",
                "Candidate 05",
                "Candidate 06",
                "Candidate 07",
                "Candidate 08"
        ));
    }

    @Test
    public void positionSuggestionsShouldRejectUnknownSource() throws Exception {
        HttpResponse<String> response = getPage("/api/suggestions/positions?source=unknown&q=java");

        assertEquals(response.statusCode(), 400);
    }

    private List<String> getJsonStringList(String path) throws Exception {
        HttpResponse<String> response = getPage(path);

        assertEquals(response.statusCode(), 200);
        return parseJsonStringList(response.body());
    }

    private List<String> parseJsonStringList(String body) {
        String trimmedBody = body.trim();
        assertTrue(trimmedBody.startsWith("["));
        assertTrue(trimmedBody.endsWith("]"));

        if ("[]".equals(trimmedBody)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean insideString = false;
        boolean escaping = false;

        for (int i = 1; i < trimmedBody.length() - 1; i++) {
            char currentChar = trimmedBody.charAt(i);

            if (escaping) {
                currentValue.append(currentChar);
                escaping = false;
                continue;
            }

            if (currentChar == '\\') {
                escaping = true;
                continue;
            }

            if (currentChar == '"') {
                if (insideString) {
                    values.add(currentValue.toString());
                    currentValue.setLength(0);
                }
                insideString = !insideString;
                continue;
            }

            if (insideString) {
                currentValue.append(currentChar);
            }
        }

        return values;
    }
}
