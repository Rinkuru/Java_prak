package ru.msu.cmc.java_prak.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Каркас формы вакансии для web-интерфейса.
 */
@Getter
@Setter
public class VacancyForm {

    @NotNull(message = "Укажите компанию.")
    private Long companyId;

    @NotBlank(message = "Укажите должность.")
    @Size(max = 255, message = "Должность должна быть не длиннее 255 символов.")
    private String position;

    @NotNull(message = "Укажите зарплату.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Зарплата не может быть отрицательной.")
    @Digits(integer = 10, fraction = 2, message = "Зарплата должна быть в формате NUMERIC(12,2).")
    private BigDecimal salary;

    @Size(max = 255, message = "Требование к образованию должно быть не длиннее 255 символов.")
    private String requiredEducation;

    @Size(max = 4000, message = "Требования должны быть не длиннее 4000 символов.")
    private String requirements;

    private boolean status = true;
}
