package ru.msu.cmc.java_prak.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import ru.msu.cmc.java_prak.web.support.PersonEducationOptions;

/**
 * Форма создания и редактирования человека в web-интерфейсе.
 */
@Getter
@Setter
public class PersonForm {

    @NotBlank(message = "Укажите ФИО.")
    @Size(max = 255, message = "ФИО должно быть не длиннее 255 символов.")
    private String fullName;

    @Size(max = 500, message = "Домашний адрес должен быть не длиннее 500 символов.")
    private String homeAddress;

    @NotBlank(message = "Укажите образование.")
    @Size(max = 255, message = "Образование должно быть не длиннее 255 символов.")
    private String education;

    private boolean status;

    @Size(max = 255, message = "Желаемая должность должна быть не длиннее 255 символов.")
    private String desiredPosition;

    @DecimalMin(value = "0.00", inclusive = true, message = "Желаемая зарплата не может быть отрицательной.")
    @Digits(integer = 10, fraction = 2, message = "Желаемая зарплата должна быть в формате NUMERIC(12,2).")
    private BigDecimal desiredSalary;

    @AssertTrue(message = "Если человек ищет работу, укажите желаемую должность и зарплату.")
    public boolean isDesiredJobDataFilledWhenPersonIsLookingForJob() {
        if (!status) {
            return true;
        }

        return desiredPosition != null
                && !desiredPosition.trim().isEmpty()
                && desiredSalary != null;
    }

    @AssertTrue(message = "Выберите образование из списка.")
    public boolean isEducationAllowed() {
        String normalizedEducation = PersonEducationOptions.normalize(education);
        if (normalizedEducation == null) {
            return true;
        }

        return PersonEducationOptions.isAllowed(normalizedEducation);
    }
}
