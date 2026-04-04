package ru.msu.cmc.java_prak.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Глобальная обработка непредвиденных ошибок.
 */
@ControllerAdvice
public class GlobalErrorControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(
            Exception exception,
            HttpServletRequest request,
            Model model
    ) throws Exception {
        if (exception instanceof ErrorResponseException errorResponseException) {
            throw errorResponseException;
        }

        String requestUri = request.getRequestURI();
        LOGGER.error("Unhandled exception for request {}", requestUri, exception);

        model.addAttribute("pageTitle", "Непредвиденная ошибка");
        model.addAttribute("activePage", resolveActivePage(requestUri));
        model.addAttribute("requestUri", requestUri);
        model.addAttribute("sectionLink", resolveSectionLink(requestUri));
        model.addAttribute("sectionLabel", resolveSectionLabel(requestUri));
        return "error/500";
    }

    private String resolveActivePage(String requestUri) {
        if (requestUri.startsWith("/companies") || requestUri.startsWith("/vacancies")) {
            return "companies";
        }
        if (requestUri.startsWith("/people")) {
            return "people";
        }
        return "home";
    }

    private String resolveSectionLink(String requestUri) {
        if (requestUri.startsWith("/companies") || requestUri.startsWith("/vacancies")) {
            return "/companies";
        }
        if (requestUri.startsWith("/people")) {
            return "/people";
        }
        return "/";
    }

    private String resolveSectionLabel(String requestUri) {
        if (requestUri.startsWith("/companies") || requestUri.startsWith("/vacancies")) {
            return "Перейти к компаниям";
        }
        if (requestUri.startsWith("/people")) {
            return "Перейти к людям";
        }
        return "Вернуться на главную";
    }
}
