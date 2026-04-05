package ru.msu.cmc.java_prak.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.WorkExperience;
import ru.msu.cmc.java_prak.web.form.WorkExperienceForm;

/**
 * Контроллер формы записи о работе.
 */
@Controller
@RequestMapping("/people/{personId}/work-experiences")
public class WorkExperienceController {

    private final WorkExperienceDao workExperienceDao;
    private final PersonDao personDao;
    private final CompanyDao companyDao;

    public WorkExperienceController(
            WorkExperienceDao workExperienceDao,
            PersonDao personDao,
            CompanyDao companyDao
    ) {
        this.workExperienceDao = workExperienceDao;
        this.personDao = personDao;
        this.companyDao = companyDao;
    }

    @GetMapping("/new")
    public String newWorkExperienceForm(@PathVariable Long personId, Model model) {
        Person person = getPersonOrThrow(personId);

        model.addAttribute("person", person);
        model.addAttribute("companies", companyDao.findAllOrderByName());
        model.addAttribute("workExperienceForm", new WorkExperienceForm());
        fillWorkExperienceCreatePage(model, personId);
        return "work-experiences/create";
    }

    @PostMapping
    public String createWorkExperience(
            @PathVariable Long personId,
            @Valid @ModelAttribute("workExperienceForm") WorkExperienceForm workExperienceForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Person person = getPersonOrThrow(personId);
        validateWorkExperienceDates(personId, null, workExperienceForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("person", person);
            model.addAttribute("companies", companyDao.findAllOrderByName());
            fillWorkExperienceCreatePage(model, personId);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "work-experiences/create";
        }

        Company company = companyDao.findById(workExperienceForm.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        WorkExperience workExperience = new WorkExperience();
        workExperience.setPerson(person);
        workExperience.setCompany(company);
        applyForm(workExperienceForm, workExperience);

        workExperienceDao.save(workExperience);
        redirectAttributes.addFlashAttribute("successMessage", "Запись о работе успешно добавлена.");
        return "redirect:/people/" + personId;
    }

    @GetMapping("/{workExperienceId}/edit")
    public String editWorkExperienceForm(
            @PathVariable Long personId,
            @PathVariable Long workExperienceId,
            Model model
    ) {
        WorkExperience workExperience = workExperienceDao.findById(workExperienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись о работе не найдена"));

        if (!workExperience.getPerson().getId().equals(personId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись о работе не найдена");
        }

        model.addAttribute("person", workExperience.getPerson());
        model.addAttribute("workExperience", workExperience);
        model.addAttribute("companies", companyDao.findAllOrderByName());
        model.addAttribute("workExperienceForm", toForm(workExperience));
        fillWorkExperienceEditPage(model, personId, workExperienceId);
        return "work-experiences/edit";
    }

    @PostMapping("/{workExperienceId}")
    public String updateWorkExperience(
            @PathVariable Long personId,
            @PathVariable Long workExperienceId,
            @Valid @ModelAttribute("workExperienceForm") WorkExperienceForm workExperienceForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        WorkExperience workExperience = getWorkExperienceOrThrow(personId, workExperienceId);
        validateWorkExperienceDates(personId, workExperienceId, workExperienceForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("person", workExperience.getPerson());
            model.addAttribute("workExperience", workExperience);
            model.addAttribute("companies", companyDao.findAllOrderByName());
            fillWorkExperienceEditPage(model, personId, workExperienceId);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "work-experiences/edit";
        }

        Company company = companyDao.findById(workExperienceForm.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        workExperience.setCompany(company);
        applyForm(workExperienceForm, workExperience);
        workExperienceDao.update(workExperience);
        redirectAttributes.addFlashAttribute("successMessage", "Запись о работе обновлена.");
        return "redirect:/people/" + personId;
    }

    @PostMapping("/{workExperienceId}/delete")
    public String deleteWorkExperience(
            @PathVariable Long personId,
            @PathVariable Long workExperienceId,
            RedirectAttributes redirectAttributes
    ) {
        getWorkExperienceOrThrow(personId, workExperienceId);

        if (!workExperienceDao.deleteById(workExperienceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись о работе не найдена");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Запись о работе удалена.");
        return "redirect:/people/" + personId;
    }

    private Person getPersonOrThrow(Long personId) {
        return personDao.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));
    }

    private WorkExperience getWorkExperienceOrThrow(Long personId, Long workExperienceId) {
        WorkExperience workExperience = workExperienceDao.findById(workExperienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись о работе не найдена"));

        if (!workExperience.getPerson().getId().equals(personId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись о работе не найдена");
        }

        return workExperience;
    }

    private WorkExperienceForm toForm(WorkExperience workExperience) {
        WorkExperienceForm workExperienceForm = new WorkExperienceForm();
        workExperienceForm.setCompanyId(workExperience.getCompany().getId());
        workExperienceForm.setPosition(workExperience.getPosition());
        workExperienceForm.setSalary(workExperience.getSalary());
        workExperienceForm.setStartDate(workExperience.getStartDate());
        workExperienceForm.setEndDate(workExperience.getEndDate());
        return workExperienceForm;
    }

    private void fillWorkExperienceCreatePage(Model model, Long personId) {
        model.addAttribute("pageTitle", "Добавление записи о работе");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + personId);
        model.addAttribute("formAction", "/people/" + personId + "/work-experiences");
    }

    private void fillWorkExperienceEditPage(Model model, Long personId, Long workExperienceId) {
        model.addAttribute("pageTitle", "Редактирование записи о работе");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + personId);
        model.addAttribute("formAction", "/people/" + personId + "/work-experiences/" + workExperienceId);
        model.addAttribute("deleteAction", "/people/" + personId + "/work-experiences/" + workExperienceId + "/delete");
    }

    private void applyForm(WorkExperienceForm workExperienceForm, WorkExperience workExperience) {
        workExperience.setPosition(workExperienceForm.getPosition().trim());
        workExperience.setSalary(workExperienceForm.getSalary());
        workExperience.setStartDate(workExperienceForm.getStartDate());
        workExperience.setEndDate(workExperienceForm.getEndDate());
    }

    private void validateWorkExperienceDates(
            Long personId,
            Long excludedWorkExperienceId,
            WorkExperienceForm workExperienceForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasFieldErrors("startDate") || bindingResult.hasGlobalErrors()) {
            return;
        }

        if (workExperienceDao.existsOverlappingPeriod(
                personId,
                workExperienceForm.getStartDate(),
                workExperienceForm.getEndDate(),
                excludedWorkExperienceId
        )) {
            bindingResult.reject(
                    "workExperienceOverlap",
                    "Период работы пересекается с другой записью этого человека."
            );
        }
    }
}
