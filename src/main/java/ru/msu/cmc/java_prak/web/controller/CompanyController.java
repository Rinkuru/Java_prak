package ru.msu.cmc.java_prak.web.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.web.form.CompanyForm;

/**
 * Контроллер каркаса раздела "Компании".
 */
@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyDao companyDao;
    private final VacancyDao vacancyDao;
    private final WorkExperienceDao workExperienceDao;

    public CompanyController(
            CompanyDao companyDao,
            VacancyDao vacancyDao,
            WorkExperienceDao workExperienceDao
    ) {
        this.companyDao = companyDao;
        this.vacancyDao = vacancyDao;
        this.workExperienceDao = workExperienceDao;
    }

    @GetMapping
    public String listCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "false") boolean onlyWithOpenVacancies,
            Model model
    ) {
        List<CompanyListItem> companyItems = companyDao.searchCompanies(
                        name,
                        onlyWithOpenVacancies ? Boolean.TRUE : null
                )
                .stream()
                .map(this::toListItem)
                .toList();

        model.addAttribute("companyItems", companyItems);
        model.addAttribute("pageTitle", "Компании");
        model.addAttribute("activePage", "companies");
        model.addAttribute("filterName", name == null ? "" : name);
        model.addAttribute("filterOnlyWithOpenVacancies", onlyWithOpenVacancies);
        return "companies/list";
    }

    @GetMapping("/new")
    public String newCompanyForm(Model model) {
        model.addAttribute("companyForm", new CompanyForm());
        model.addAttribute("pageTitle", "Добавление компании");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/companies");
        return "companies/create";
    }

    @GetMapping("/{id}")
    public String viewCompany(@PathVariable Long id, Model model) {
        Company company = getCompanyOrThrow(id, true);
        boolean hasWorkExperienceLinks = !workExperienceDao.findByCompanyId(id).isEmpty();

        model.addAttribute("company", company);
        model.addAttribute("pageTitle", company.getName());
        model.addAttribute("activePage", "companies");
        model.addAttribute("hasWorkExperienceLinks", hasWorkExperienceLinks);
        model.addAttribute("openVacancyCount", company.getVacancies().stream().filter(Vacancy::isStatus).count());
        return "companies/view";
    }

    @GetMapping("/{id}/edit")
    public String editCompanyForm(@PathVariable Long id, Model model) {
        Company company = getCompanyOrThrow(id, false);

        model.addAttribute("company", company);
        model.addAttribute("companyForm", toForm(company));
        model.addAttribute("pageTitle", "Редактирование компании");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/companies/" + id);
        return "companies/edit";
    }

    private Company getCompanyOrThrow(Long id, boolean withCardData) {
        Optional<Company> company = withCardData ? companyDao.findCardById(id) : companyDao.findById(id);
        return company.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));
    }

    private CompanyForm toForm(Company company) {
        CompanyForm companyForm = new CompanyForm();
        companyForm.setName(company.getName());
        companyForm.setDescription(company.getDescription());
        return companyForm;
    }

    private CompanyListItem toListItem(Company company) {
        List<Vacancy> vacancies = vacancyDao.findByCompanyId(company.getId());
        long openVacancyCount = vacancies.stream().filter(Vacancy::isStatus).count();
        return new CompanyListItem(
                company.getId(),
                company.getName(),
                company.getDescription(),
                vacancies.size(),
                openVacancyCount
        );
    }

    /**
     * Элемент списка компаний с уже рассчитанными счётчиками.
     */
    public record CompanyListItem(
            Long id,
            String name,
            String description,
            int vacancyCount,
            long openVacancyCount
    ) {
    }
}
