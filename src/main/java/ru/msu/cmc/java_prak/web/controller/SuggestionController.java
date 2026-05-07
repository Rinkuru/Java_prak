package ru.msu.cmc.java_prak.web.controller;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;

/**
 * JSON API для автодополнения текстовых полей в фильтрах и формах.
 */
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int SUGGESTION_LIMIT = 8;

    private final CompanyDao companyDao;
    private final PersonDao personDao;
    private final VacancyDao vacancyDao;
    private final WorkExperienceDao workExperienceDao;

    public SuggestionController(
            CompanyDao companyDao,
            PersonDao personDao,
            VacancyDao vacancyDao,
            WorkExperienceDao workExperienceDao
    ) {
        this.companyDao = companyDao;
        this.personDao = personDao;
        this.vacancyDao = vacancyDao;
        this.workExperienceDao = workExperienceDao;
    }

    @GetMapping("/companies")
    public List<String> companySuggestions(@RequestParam(required = false) String q) {
        String query = normalizeQuery(q);
        if (query == null) {
            return List.of();
        }

        return companyDao.findNameSuggestions(query, SUGGESTION_LIMIT);
    }

    @GetMapping("/people")
    public List<String> peopleSuggestions(@RequestParam(required = false) String q) {
        String query = normalizeQuery(q);
        if (query == null) {
            return List.of();
        }

        return personDao.findFullNameSuggestions(query, SUGGESTION_LIMIT);
    }

    @GetMapping("/positions")
    public List<String> positionSuggestions(
            @RequestParam String source,
            @RequestParam(required = false) String q
    ) {
        String normalizedSource = source.toLowerCase(Locale.ROOT);
        String query = normalizeQuery(q);

        return switch (normalizedSource) {
            case "vacancy" -> query == null ? List.of() : vacancyDao.findPositionSuggestions(query, SUGGESTION_LIMIT);
            case "work" -> query == null ? List.of() : workExperienceDao.findPositionSuggestions(query, SUGGESTION_LIMIT);
            case "desired" -> query == null ? List.of() : personDao.findDesiredPositionSuggestions(query, SUGGESTION_LIMIT);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестный источник подсказок.");
        };
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }

        String trimmedQuery = query.trim();
        return trimmedQuery.length() < MIN_QUERY_LENGTH ? null : trimmedQuery;
    }
}
