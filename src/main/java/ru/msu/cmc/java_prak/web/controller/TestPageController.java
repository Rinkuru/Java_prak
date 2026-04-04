package ru.msu.cmc.java_prak.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Тестовый контроллер для первой проверочной страницы.
 */
@Controller
public class TestPageController {

    @GetMapping("/test")
    public String testPage(Model model) {
        model.addAttribute("pageTitle", "Тестовая страница");
        model.addAttribute("activePage", "home");
        return "test";
    }

    @GetMapping("/test/fail")
    public String failPage() {
        throw new IllegalStateException("Synthetic failure for error-page testing");
    }
}
