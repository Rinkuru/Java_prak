package ru.msu.cmc.java_prak.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.WorkExperience;
import ru.msu.cmc.java_prak.web.form.WorkExperienceForm;

/**
 * Контроллер каркаса формы записи о работе.
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
        model.addAttribute("pageTitle", "Добавление записи о работе");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + personId);
        return "work-experiences/create";
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
        model.addAttribute("pageTitle", "Редактирование записи о работе");
        model.addAttribute("activePage", "people");
        model.addAttribute("backLink", "/people/" + personId);
        return "work-experiences/edit";
    }

    private Person getPersonOrThrow(Long personId) {
        return personDao.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));
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
}
