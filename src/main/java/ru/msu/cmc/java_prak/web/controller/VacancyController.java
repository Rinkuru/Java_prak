package ru.msu.cmc.java_prak.web.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.web.form.VacancyForm;

/**
 * Контроллер страниц вакансий.
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
        model.addAttribute("vacancyForm", vacancyForm);
        fillVacancyCreatePage(model, companyId, company);
        return "vacancies/create";
    }

    @PostMapping("/companies/{companyId}/vacancies")
    public String createVacancy(
            @PathVariable Long companyId,
            @Valid @ModelAttribute("vacancyForm") VacancyForm vacancyForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Company company = getCompanyOrThrow(companyId);
        validateCompanyBinding(companyId, vacancyForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("company", company);
            fillVacancyCreatePage(model, companyId, company);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "vacancies/create";
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setCompany(company);
        applyForm(vacancyForm, vacancy);

        Vacancy savedVacancy = vacancyDao.save(vacancy);
        redirectAttributes.addFlashAttribute("successMessage", "Вакансия успешно добавлена.");
        return "redirect:/vacancies/" + savedVacancy.getId();
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
        fillVacancyEditPage(model, id);
        return "vacancies/edit";
    }

    @PostMapping("/vacancies/{id}")
    public String updateVacancy(
            @PathVariable Long id,
            @Valid @ModelAttribute("vacancyForm") VacancyForm vacancyForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Vacancy vacancy = getVacancyOrThrow(id, true);
        if (bindingResult.hasErrors()) {
            model.addAttribute("vacancy", vacancy);
            model.addAttribute("companies", companyDao.findAllOrderByName());
            fillVacancyEditPage(model, id);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "vacancies/edit";
        }

        Company company = getCompanyOrThrow(vacancyForm.getCompanyId());
        vacancy.setCompany(company);
        applyForm(vacancyForm, vacancy);
        vacancyDao.update(vacancy);
        redirectAttributes.addFlashAttribute("successMessage", "Данные вакансии обновлены.");
        return "redirect:/vacancies/" + id;
    }

    @PostMapping("/vacancies/{id}/delete")
    public String deleteVacancy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vacancy vacancy = getVacancyOrThrow(id, true);
        Long companyId = vacancy.getCompany().getId();

        if (!vacancyDao.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Вакансия удалена.");
        return "redirect:/companies/" + companyId;
    }

    @PostMapping("/vacancies/{id}/close")
    public String closeVacancy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vacancyDao.updateStatus(id, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
        redirectAttributes.addFlashAttribute("successMessage", "Вакансия закрыта.");
        return "redirect:/vacancies/" + id;
    }

    @PostMapping("/vacancies/{id}/reopen")
    public String reopenVacancy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vacancyDao.updateStatus(id, true)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
        redirectAttributes.addFlashAttribute("successMessage", "Вакансия снова открыта.");
        return "redirect:/vacancies/" + id;
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

    private void fillVacancyCreatePage(Model model, Long companyId, Company company) {
        model.addAttribute("company", company);
        model.addAttribute("pageTitle", "Добавление вакансии");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/companies/" + companyId);
        model.addAttribute("formAction", "/companies/" + companyId + "/vacancies");
    }

    private void fillVacancyEditPage(Model model, Long vacancyId) {
        model.addAttribute("pageTitle", "Редактирование вакансии");
        model.addAttribute("activePage", "companies");
        model.addAttribute("backLink", "/vacancies/" + vacancyId);
        model.addAttribute("formAction", "/vacancies/" + vacancyId);
    }

    private void applyForm(VacancyForm vacancyForm, Vacancy vacancy) {
        vacancy.setPosition(vacancyForm.getPosition().trim());
        vacancy.setSalary(vacancyForm.getSalary());
        vacancy.setRequiredEducation(normalize(vacancyForm.getRequiredEducation()));
        vacancy.setRequirements(normalize(vacancyForm.getRequirements()));
        vacancy.setStatus(vacancyForm.isStatus());
    }

    private void validateCompanyBinding(Long companyId, VacancyForm vacancyForm, BindingResult bindingResult) {
        if (vacancyForm.getCompanyId() == null) {
            return;
        }

        if (!companyId.equals(vacancyForm.getCompanyId())) {
            vacancyForm.setCompanyId(companyId);
            bindingResult.reject("companyIdMismatch", "Компания вакансии не совпадает с выбранной карточкой компании.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
