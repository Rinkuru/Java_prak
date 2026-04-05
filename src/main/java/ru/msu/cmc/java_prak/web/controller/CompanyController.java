package ru.msu.cmc.java_prak.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.web.form.CompanyForm;

/**
 * Контроллер раздела "Компании".
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
        fillCompanyFormPage(model, "Добавление компании", "/companies", "/companies");
        return "companies/create";
    }

    @PostMapping
    public String createCompany(
            @Valid @ModelAttribute("companyForm") CompanyForm companyForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateCompanyName(companyForm, bindingResult, null);
        if (bindingResult.hasErrors()) {
            fillCompanyFormPage(model, "Добавление компании", "/companies", "/companies");
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "companies/create";
        }

        Company company = new Company();
        applyForm(companyForm, company);

        Company savedCompany = companyDao.save(company);
        redirectAttributes.addFlashAttribute("successMessage", "Компания успешно добавлена.");
        return "redirect:/companies/" + savedCompany.getId();
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
        fillCompanyFormPage(model, "Редактирование компании", "/companies/" + id, "/companies/" + id);
        return "companies/edit";
    }

    @PostMapping("/{id}")
    public String updateCompany(
            @PathVariable Long id,
            @Valid @ModelAttribute("companyForm") CompanyForm companyForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Company company = getCompanyOrThrow(id, false);
        validateCompanyName(companyForm, bindingResult, id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("company", company);
            fillCompanyFormPage(model, "Редактирование компании", "/companies/" + id, "/companies/" + id);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "companies/edit";
        }

        applyForm(companyForm, company);
        companyDao.update(company);
        redirectAttributes.addFlashAttribute("successMessage", "Данные компании обновлены.");
        return "redirect:/companies/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        getCompanyOrThrow(id, false);

        if (!workExperienceDao.findByCompanyId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Компания не может быть удалена, потому что на неё ссылается история работы кандидатов."
            );
            return "redirect:/companies/" + id;
        }

        if (!companyDao.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Компания удалена.");
        return "redirect:/companies";
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

    private void fillCompanyFormPage(Model model, String pageTitle, String backLink, String formAction) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", backLink);
        model.addAttribute("formAction", formAction);
    }

    private void applyForm(CompanyForm companyForm, Company company) {
        company.setName(companyForm.getName().trim());
        company.setDescription(normalize(companyForm.getDescription()));
    }

    private void validateCompanyName(CompanyForm companyForm, BindingResult bindingResult, Long excludedCompanyId) {
        if (bindingResult.hasFieldErrors("name")) {
            return;
        }

        String normalizedName = normalize(companyForm.getName());
        if (normalizedName == null) {
            return;
        }

        boolean alreadyExists = companyDao.findAllOrderByName()
                .stream()
                .anyMatch(company -> !company.getId().equals(excludedCompanyId) && normalizedName.equals(company.getName()));

        if (alreadyExists) {
            bindingResult.rejectValue("name", "duplicate", "Компания с таким названием уже существует.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
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
