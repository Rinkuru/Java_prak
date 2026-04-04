package ru.msu.cmc.java_prak.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.dao.VacancyDao;

/**
 * Контроллер главной страницы.
 */
@Controller
public class HomeController {

    private final PersonDao personDao;
    private final CompanyDao companyDao;
    private final VacancyDao vacancyDao;

    public HomeController(PersonDao personDao, CompanyDao companyDao, VacancyDao vacancyDao) {
        this.personDao = personDao;
        this.companyDao = companyDao;
        this.vacancyDao = vacancyDao;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Кадровое агентство");
        model.addAttribute("activePage", "home");
        model.addAttribute("peopleCount", personDao.findAllOrderByFullName().size());
        model.addAttribute("companyCount", companyDao.findAllOrderByName().size());
        model.addAttribute("openVacancyCount", vacancyDao.findByStatus(true).size());
        return "home";
    }
}
