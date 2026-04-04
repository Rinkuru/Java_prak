package ru.msu.cmc.java_prak.web.controller;

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
 * Контроллер каркаса страницы результатов подбора.
 */
@Controller
public class MatchingController {

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
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @RequestParam(defaultValue = "desc") String sort,
            Model model
    ) {
        Person person = personDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));
        boolean orderBySalaryAsc = "asc".equalsIgnoreCase(sort);

        model.addAttribute("mode", "vacanciesForPerson");
        model.addAttribute("person", person);
        model.addAttribute("vacancies", matchingDao.findSuitableVacanciesForPerson(id, onlyActive, orderBySalaryAsc));
        model.addAttribute("onlyActive", onlyActive);
        model.addAttribute("sortDirection", orderBySalaryAsc ? "asc" : "desc");
        model.addAttribute("pageTitle", "Подбор вакансий");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + id);
        return "matching/results";
    }

    @GetMapping("/vacancies/{id}/matches")
    public String vacancyMatches(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean onlyLookingForJob,
            @RequestParam(defaultValue = "asc") String sort,
            Model model
    ) {
        Vacancy vacancy = vacancyDao.findCardById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
        boolean orderByDesiredSalaryAsc = !"desc".equalsIgnoreCase(sort);

        model.addAttribute("mode", "peopleForVacancy");
        model.addAttribute("vacancy", vacancy);
        model.addAttribute(
                "people",
                matchingDao.findSuitablePersonsForVacancy(id, onlyLookingForJob, orderByDesiredSalaryAsc)
        );
        model.addAttribute("onlyLookingForJob", onlyLookingForJob);
        model.addAttribute("sortDirection", orderByDesiredSalaryAsc ? "asc" : "desc");
        model.addAttribute("pageTitle", "Подбор резюме");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/vacancies/" + id);
        return "matching/results";
    }
}
