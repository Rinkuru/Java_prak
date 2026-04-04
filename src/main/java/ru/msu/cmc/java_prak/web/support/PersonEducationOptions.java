package ru.msu.cmc.java_prak.web.support;

import java.util.List;

/**
 * Единый справочник допустимых значений образования для человека.
 */
public final class PersonEducationOptions {

    private static final List<String> OPTIONS = List.of(
            "Среднее специальное",
            "Высшее медицинское",
            "Высшее техническое",
            "Высшее"
    );

    private PersonEducationOptions() {
    }

    public static List<String> values() {
        return OPTIONS;
    }

    public static boolean isAllowed(String education) {
        return OPTIONS.contains(normalize(education));
    }

    public static String normalize(String education) {
        if (education == null) {
            return null;
        }

        String trimmedEducation = education.trim();
        return trimmedEducation.isEmpty() ? null : trimmedEducation;
    }
}
