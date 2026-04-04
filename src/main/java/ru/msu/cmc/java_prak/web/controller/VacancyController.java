package ru.msu.cmc.java_prak.web.controller;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.web.form.VacancyForm;

/**
 * Контроллер каркаса страниц вакансий.
 */
@Controller
public class VacancyController {

    private final VacancyDao vacancyDao;
    private final CompanyDao companyDao;

    public VacancyController(VacancyDao vacancyDao, CompanyDao companyDao) {
        this.vacancyDao = vacancyDao;
        this.companyDao = companyDao;
    }

    @GetMapping("/companies/{companyId}/vacancies/new")
    public String newVacancyForm(@PathVariable Long companyId, Model model) {
        Company company = getCompanyOrThrow(companyId);
        VacancyForm vacancyForm = new VacancyForm();
        vacancyForm.setCompanyId(companyId);
        vacancyForm.setStatus(true);

        model.addAttribute("company", company);
        model.addAttribute("companies", companyDao.findAllOrderByName());
        model.addAttribute("vacancyForm", vacancyForm);
        model.addAttribute("pageTitle", "Добавление вакансии");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/companies/" + companyId);
        return "vacancies/create";
    }

    @GetMapping("/vacancies/{id}")
    public String viewVacancy(@PathVariable Long id, Model model) {
        Vacancy vacancy = getVacancyOrThrow(id, true);

        model.addAttribute("vacancy", vacancy);
        model.addAttribute("pageTitle", vacancy.getPosition());
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/companies/" + vacancy.getCompany().getId());
        return "vacancies/view";
    }

    @GetMapping("/vacancies/{id}/edit")
    public String editVacancyForm(@PathVariable Long id, Model model) {
        Vacancy vacancy = getVacancyOrThrow(id, true);

        model.addAttribute("vacancy", vacancy);
        model.addAttribute("companies", companyDao.findAllOrderByName());
        model.addAttribute("vacancyForm", toForm(vacancy));
        model.addAttribute("pageTitle", "Редактирование вакансии");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/vacancies/" + id);
        return "vacancies/edit";
    }

    private Vacancy getVacancyOrThrow(Long id, boolean withCardData) {
        Optional<Vacancy> vacancy = withCardData ? vacancyDao.findCardById(id) : vacancyDao.findById(id);
        return vacancy.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
    }

    private Company getCompanyOrThrow(Long id) {
        return companyDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));
    }

    private VacancyForm toForm(Vacancy vacancy) {
        VacancyForm vacancyForm = new VacancyForm();
        vacancyForm.setCompanyId(vacancy.getCompany().getId());
        vacancyForm.setPosition(vacancy.getPosition());
        vacancyForm.setSalary(vacancy.getSalary());
        vacancyForm.setRequiredEducation(vacancy.getRequiredEducation());
        vacancyForm.setRequirements(vacancy.getRequirements());
        vacancyForm.setStatus(vacancy.isStatus());
        return vacancyForm;
    }
}
