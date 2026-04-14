package ru.msu.cmc.java_prak.web.controller;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String minSalary,
            @RequestParam(required = false) String maxSalary,
            @RequestParam(required = false) String onlyWithOpenVacancies,
            Model model
    ) {
        List<String> validationErrors = new ArrayList<>();
        String normalizedPosition = normalize(position);
        BigDecimal effectiveMinSalary = parseSalaryParameter(minSalary, "Зарплата от", validationErrors);
        BigDecimal effectiveMaxSalary = parseSalaryParameter(maxSalary, "Зарплата до", validationErrors);
        boolean effectiveOnlyWithOpenVacancies = parseBooleanParameter(
                onlyWithOpenVacancies,
                false,
                "фильтра показа только компаний с открытыми вакансиями",
                validationErrors
        );
        boolean hasVacancyFilters = normalizedPosition != null || effectiveMinSalary != null || effectiveMaxSalary != null;
        boolean hasSalaryRangeError = hasInvalidSalaryRange(effectiveMinSalary, effectiveMaxSalary);
        if (hasSalaryRangeError) {
            validationErrors.add("Значение \"Зарплата от\" не может быть больше значения \"Зарплата до\".");
        }

        List<CompanyListItem> companyItems;
        if (hasSalaryRangeError) {
            companyItems = List.of();
        } else {
            Boolean vacancyStatusFilter = effectiveOnlyWithOpenVacancies ? Boolean.TRUE : null;
            List<Company> companies = companyDao.searchCompanies(name, vacancyStatusFilter);

            if (hasVacancyFilters) {
                Set<Long> matchingCompanyIds = vacancyDao.searchVacancies(
                                null,
                                null,
                                normalizedPosition,
                                effectiveMinSalary,
                                effectiveMaxSalary,
                                vacancyStatusFilter
                        )
                        .stream()
                        .map(vacancy -> vacancy.getCompany().getId())
                        .collect(Collectors.toSet());

                companies = companies.stream()
                        .filter(company -> matchingCompanyIds.contains(company.getId()))
                        .toList();
            }

            companyItems = companies.stream()
                    .map(this::toListItem)
                    .toList();
        }

        addErrorMessage(model, validationErrors);
        model.addAttribute("companyItems", companyItems);
        model.addAttribute("hasFilterError", hasSalaryRangeError);
        model.addAttribute("hasVacancyFilters", hasVacancyFilters);
        model.addAttribute("pageTitle", "Компании");
        model.addAttribute("activePage", "companies");
        model.addAttribute("filterName", name == null ? "" : name);
        model.addAttribute("filterPosition", emptyToBlank(normalizedPosition));
        model.addAttribute("filterMinSalary", emptyToBlank(normalize(minSalary)));
        model.addAttribute("filterMaxSalary", emptyToBlank(normalize(maxSalary)));
        model.addAttribute("filterOnlyWithOpenVacancies", effectiveOnlyWithOpenVacancies);
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
    public String viewCompany(
            @PathVariable Long id,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String minSalary,
            @RequestParam(required = false) String maxSalary,
            @RequestParam(required = false) String onlyActive,
            Model model
    ) {
        Company company = getCompanyOrThrow(id, true);
        boolean hasWorkExperienceLinks = !workExperienceDao.findByCompanyId(id).isEmpty();
        List<String> validationErrors = new ArrayList<>();
        String normalizedPosition = normalize(position);
        BigDecimal effectiveMinSalary = parseSalaryParameter(minSalary, "Зарплата от", validationErrors);
        BigDecimal effectiveMaxSalary = parseSalaryParameter(maxSalary, "Зарплата до", validationErrors);
        boolean effectiveOnlyActive = parseBooleanParameter(
                onlyActive,
                false,
                "фильтра показа только открытых вакансий",
                validationErrors
        );
        boolean hasVacancyFilters = effectiveOnlyActive
                || normalizedPosition != null
                || effectiveMinSalary != null
                || effectiveMaxSalary != null;
        boolean hasSalaryRangeError = hasInvalidSalaryRange(effectiveMinSalary, effectiveMaxSalary);
        if (hasSalaryRangeError) {
            validationErrors.add("Значение \"Зарплата от\" не может быть больше значения \"Зарплата до\".");
        }
        List<Vacancy> filteredVacancies;

        if (hasSalaryRangeError) {
            filteredVacancies = List.of();
        } else if (hasVacancyFilters) {
            filteredVacancies = vacancyDao.searchVacancies(
                    id,
                    null,
                    normalizedPosition,
                    effectiveMinSalary,
                    effectiveMaxSalary,
                    effectiveOnlyActive ? Boolean.TRUE : null
            );
        } else {
            filteredVacancies = company.getVacancies();
        }

        int totalVacancyCount = company.getVacancies().size();

        addErrorMessage(model, validationErrors);
        model.addAttribute("company", company);
        model.addAttribute("filteredVacancies", filteredVacancies);
        model.addAttribute("totalVacancyCount", totalVacancyCount);
        model.addAttribute("displayedVacancyCount", filteredVacancies.size());
        model.addAttribute("hasVacancyFilters", hasVacancyFilters);
        model.addAttribute("hasFilterError", hasSalaryRangeError);
        model.addAttribute("showNoVacanciesState", !hasSalaryRangeError && totalVacancyCount == 0);
        model.addAttribute(
                "showNoMatchingVacanciesState",
                !hasSalaryRangeError && hasVacancyFilters && totalVacancyCount > 0 && filteredVacancies.isEmpty()
        );
        model.addAttribute("pageTitle", company.getName());
        model.addAttribute("activePage", "companies");
        model.addAttribute("hasWorkExperienceLinks", hasWorkExperienceLinks);
        model.addAttribute("openVacancyCount", company.getVacancies().stream().filter(Vacancy::isStatus).count());
        model.addAttribute("filterPosition", emptyToBlank(normalizedPosition));
        model.addAttribute("filterMinSalary", emptyToBlank(normalize(minSalary)));
        model.addAttribute("filterMaxSalary", emptyToBlank(normalize(maxSalary)));
        model.addAttribute("filterOnlyActive", effectiveOnlyActive);
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

    private boolean hasInvalidSalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        return minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0;
    }

    private String emptyToBlank(String value) {
        return value == null ? "" : value;
    }

    private void addErrorMessage(Model model, List<String> validationErrors) {
        if (!validationErrors.isEmpty()) {
            model.addAttribute("errorMessage", String.join(" ", validationErrors));
        }
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

    private BigDecimal parseSalaryParameter(String rawValue, String parameterLabel, List<String> validationErrors) {
        String normalizedValue = normalize(rawValue);
        if (normalizedValue == null) {
            return null;
        }

        try {
            BigDecimal salary = new BigDecimal(normalizedValue);
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                validationErrors.add("Значение фильтра \"" + parameterLabel + "\" не может быть отрицательным. Параметр не применён.");
                return null;
            }
            return salary;
        } catch (NumberFormatException exception) {
            validationErrors.add("Передано некорректное значение фильтра \"" + parameterLabel + "\". Параметр не применён.");
            return null;
        }
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
