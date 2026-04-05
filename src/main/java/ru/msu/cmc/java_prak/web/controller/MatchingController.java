package ru.msu.cmc.java_prak.web.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.msu.cmc.java_prak.dao.MatchingDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

/**
 * Контроллер страницы результатов подбора.
 */
@Controller
public class MatchingController {

    private static final String SORT_ASC = "asc";
    private static final String SORT_DESC = "desc";

    private final MatchingDao matchingDao;
    private final PersonDao personDao;
    private final VacancyDao vacancyDao;

    public MatchingController(MatchingDao matchingDao, PersonDao personDao, VacancyDao vacancyDao) {
        this.matchingDao = matchingDao;
        this.personDao = personDao;
        this.vacancyDao = vacancyDao;
    }

    @GetMapping("/people/{id}/matches")
    public String personMatches(
            @PathVariable Long id,
            @RequestParam(required = false) String onlyActive,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        Person person = personDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));

        List<String> validationErrors = new ArrayList<>();
        boolean effectiveOnlyActive = parseBooleanParameter(
                onlyActive,
                true,
                "фильтра показа только открытых вакансий",
                validationErrors
        );
        String effectiveSort = parseSortParameter(sort, SORT_DESC, validationErrors);
        boolean orderBySalaryAsc = SORT_ASC.equals(effectiveSort);
        boolean hasMatchingCriteria = hasText(person.getDesiredPosition()) && person.getDesiredSalary() != null;

        List<String> infoMessages = new ArrayList<>();
        if (!hasMatchingCriteria) {
            infoMessages.add("Для содержательного подбора у человека должны быть заполнены желаемая должность и желаемая зарплата.");
        }
        if (!hasText(person.getEducation())) {
            infoMessages.add("У человека не заполнено образование, поэтому фильтр по образованию не применялся.");
        }

        List<ru.msu.cmc.java_prak.model.Vacancy> vacancies = matchingDao.findSuitableVacanciesForPerson(
                id,
                effectiveOnlyActive,
                orderBySalaryAsc
        );

        model.addAttribute("mode", "vacanciesForPerson");
        model.addAttribute("person", person);
        model.addAttribute("vacancies", vacancies);
        model.addAttribute("onlyActive", effectiveOnlyActive);
        model.addAttribute("sortDirection", effectiveSort);
        model.addAttribute("infoMessages", infoMessages);
        model.addAttribute("showEmptyState", hasMatchingCriteria && vacancies.isEmpty());
        model.addAttribute("pageTitle", "Подбор вакансий");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + id);
        addErrorMessage(model, validationErrors);
        return "matching/results";
    }

    @GetMapping("/vacancies/{id}/matches")
    public String vacancyMatches(
            @PathVariable Long id,
            @RequestParam(required = false) String onlyLookingForJob,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        Vacancy vacancy = vacancyDao.findCardById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));

        List<String> validationErrors = new ArrayList<>();
        boolean effectiveOnlyLookingForJob = parseBooleanParameter(
                onlyLookingForJob,
                true,
                "фильтра показа только людей, которые ищут работу",
                validationErrors
        );
        String effectiveSort = parseSortParameter(sort, SORT_ASC, validationErrors);
        boolean orderByDesiredSalaryAsc = SORT_ASC.equals(effectiveSort);

        List<String> infoMessages = new ArrayList<>();
        if (!hasText(vacancy.getRequiredEducation())) {
            infoMessages.add("У вакансии не заполнено требование к образованию, поэтому фильтр по образованию не применялся.");
        }
        if (!vacancy.isStatus()) {
            infoMessages.add("Подбор выполняется для закрытой вакансии.");
        }

        List<ru.msu.cmc.java_prak.model.Person> people = matchingDao.findSuitablePersonsForVacancy(
                id,
                effectiveOnlyLookingForJob,
                orderByDesiredSalaryAsc
        );

        model.addAttribute("mode", "peopleForVacancy");
        model.addAttribute("vacancy", vacancy);
        model.addAttribute("people", people);
        model.addAttribute("onlyLookingForJob", effectiveOnlyLookingForJob);
        model.addAttribute("sortDirection", effectiveSort);
        model.addAttribute("infoMessages", infoMessages);
        model.addAttribute("showEmptyState", people.isEmpty());
        model.addAttribute("pageTitle", "Подбор резюме");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/vacancies/" + id);
        addErrorMessage(model, validationErrors);
        return "matching/results";
    }

    private boolean parseBooleanParameter(
            String rawValue,
            boolean defaultValue,
            String parameterDescription,
            List<String> validationErrors
    ) {
        if (rawValue == null) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(rawValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(rawValue)) {
            return false;
        }

        validationErrors.add("Передано некорректное значение " + parameterDescription + ". Применено значение по умолчанию.");
        return defaultValue;
    }

    private String parseSortParameter(String rawSort, String defaultSort, List<String> validationErrors) {
        if (rawSort == null) {
            return defaultSort;
        }
        if (SORT_ASC.equalsIgnoreCase(rawSort)) {
            return SORT_ASC;
        }
        if (SORT_DESC.equalsIgnoreCase(rawSort)) {
            return SORT_DESC;
        }

        validationErrors.add("Передан некорректный параметр сортировки. Применено значение по умолчанию.");
        return defaultSort;
    }

    private void addErrorMessage(Model model, List<String> validationErrors) {
        if (!validationErrors.isEmpty()) {
            model.addAttribute("errorMessage", String.join(" ", validationErrors));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
