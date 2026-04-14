package ru.msu.cmc.java_prak.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.model.Company;

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
