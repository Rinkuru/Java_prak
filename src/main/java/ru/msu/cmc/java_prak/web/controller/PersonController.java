package ru.msu.cmc.java_prak.web.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.web.form.PersonForm;
import ru.msu.cmc.java_prak.web.support.PersonEducationOptions;

/**
 * Контроллер раздела "Люди".
 */
@Controller
@RequestMapping("/people")
public class PersonController {

    private final PersonDao personDao;
    private final CompanyDao companyDao;

    public PersonController(PersonDao personDao, CompanyDao companyDao) {
        this.personDao = personDao;
        this.companyDao = companyDao;
    }

    @ModelAttribute("educationOptions")
    public List<String> educationOptions() {
        return PersonEducationOptions.values();
    }

    @GetMapping
    public String listPeople(
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minDesiredSalary,
            @RequestParam(required = false) BigDecimal maxDesiredSalary,
            @RequestParam(required = false) Long workedCompanyId,
            @RequestParam(required = false) String workedPosition,
            @RequestParam(defaultValue = "asc") String sort,
            Model model
    ) {
        boolean orderBySalaryAsc = !"desc".equalsIgnoreCase(sort);
        boolean hasFilterError = hasInvalidSalaryRange(minDesiredSalary, maxDesiredSalary);

        List<Person> people;
        if (hasFilterError) {
            people = List.of();
            model.addAttribute(
                    "errorMessage",
                    "Значение \"Зарплата от\" не может быть больше значения \"Зарплата до\"."
            );
        } else {
            people = personDao.searchPeople(
                    education,
                    parseStatus(status),
                    minDesiredSalary,
                    maxDesiredSalary,
                    workedCompanyId,
                    workedPosition,
                    orderBySalaryAsc
            );
        }

        model.addAttribute("people", people);
        model.addAttribute("hasFilterError", hasFilterError);
        model.addAttribute("companies", companyDao.findAllOrderByName());
        model.addAttribute("pageTitle", "Люди");
        model.addAttribute("activePage", "people");
        model.addAttribute("filterEducation", emptyToBlank(education));
        model.addAttribute("filterStatus", emptyToBlank(status));
        model.addAttribute("filterMinDesiredSalary", minDesiredSalary);
        model.addAttribute("filterMaxDesiredSalary", maxDesiredSalary);
        model.addAttribute("filterWorkedCompanyId", workedCompanyId);
        model.addAttribute("filterWorkedPosition", emptyToBlank(workedPosition));
        model.addAttribute("sortDirection", orderBySalaryAsc ? "asc" : "desc");
        return "people/list";
    }

    @GetMapping("/new")
    public String newPersonForm(Model model) {
        model.addAttribute("personForm", new PersonForm());
        fillPersonFormPage(model, "Добавление человека", "/people", false);
        model.addAttribute("backLink", "/people");
        return "people/create";
    }

    @PostMapping
    public String createPerson(
            @Valid @ModelAttribute("personForm") PersonForm personForm,
            org.springframework.validation.BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            fillPersonFormPage(model, "Добавление человека", "/people", false);
            model.addAttribute("backLink", "/people");
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "people/create";
        }

        Person person = new Person();
        applyForm(personForm, person);
        Person savedPerson = personDao.save(person);
        redirectAttributes.addFlashAttribute("successMessage", "Человек успешно добавлен.");
        return "redirect:/people/" + savedPerson.getId();
    }

    @GetMapping("/{id}")
    public String viewPerson(@PathVariable Long id, Model model) {
        Person person = getPersonOrThrow(id, true);
        model.addAttribute("person", person);
        model.addAttribute("pageTitle", person.getFullName());
        model.addAttribute("activePage", "people");
        model.addAttribute("workExperienceBackLink", "/people/" + id);
        return "people/view";
    }

    @GetMapping("/{id}/edit")
    public String editPersonForm(@PathVariable Long id, Model model) {
        Person person = getPersonOrThrow(id, false);
        model.addAttribute("personForm", toForm(person));
        fillPersonFormPage(model, "Редактирование человека", "/people/" + id, true);
        model.addAttribute("personId", id);
        model.addAttribute("backLink", "/people/" + id);
        return "people/edit";
    }

    @PostMapping("/{id}")
    public String updatePerson(
            @PathVariable Long id,
            @Valid @ModelAttribute("personForm") PersonForm personForm,
            org.springframework.validation.BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            fillPersonFormPage(model, "Редактирование человека", "/people/" + id, true);
            model.addAttribute("personId", id);
            model.addAttribute("backLink", "/people/" + id);
            model.addAttribute("errorMessage", "Форма содержит ошибки. Исправьте их и отправьте заново.");
            return "people/edit";
        }

        Person person = getPersonOrThrow(id, false);
        applyForm(personForm, person);
        personDao.update(person);
        redirectAttributes.addFlashAttribute("successMessage", "Данные человека обновлены.");
        return "redirect:/people/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePerson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!personDao.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Человек удалён.");
        return "redirect:/people";
    }

    private Person getPersonOrThrow(Long id, boolean withCardData) {
        Optional<Person> person = withCardData ? personDao.findCardById(id) : personDao.findById(id);
        return person.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));
    }

    private PersonForm toForm(Person person) {
        PersonForm personForm = new PersonForm();
        personForm.setFullName(person.getFullName());
        personForm.setHomeAddress(person.getHomeAddress());
        personForm.setEducation(person.getEducation());
        personForm.setStatus(person.isStatus());
        personForm.setDesiredPosition(person.getDesiredPosition());
        personForm.setDesiredSalary(person.getDesiredSalary());
        return personForm;
    }

    private void applyForm(PersonForm personForm, Person person) {
        person.setFullName(personForm.getFullName().trim());
        person.setHomeAddress(normalize(personForm.getHomeAddress()));
        person.setEducation(personForm.getEducation().trim());
        person.setStatus(personForm.isStatus());

        if (personForm.isStatus()) {
            person.setDesiredPosition(personForm.getDesiredPosition().trim());
            person.setDesiredSalary(personForm.getDesiredSalary());
        } else {
            person.setDesiredPosition(null);
            person.setDesiredSalary(null);
        }
    }

    private void fillPersonFormPage(Model model, String pageTitle, String formAction, boolean editMode) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", "people");
        model.addAttribute("formAction", formAction);
        model.addAttribute("editMode", editMode);
    }

    private Boolean parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }
        return Boolean.valueOf(statusValue);
    }

    private boolean hasInvalidSalaryRange(BigDecimal minDesiredSalary, BigDecimal maxDesiredSalary) {
        return minDesiredSalary != null
                && maxDesiredSalary != null
                && minDesiredSalary.compareTo(maxDesiredSalary) > 0;
    }

    private String emptyToBlank(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
