package ru.msu.cmc.java_prak.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Каркас формы компании для web-интерфейса.
 */
@Getter
@Setter
public class CompanyForm {

    @NotBlank(message = "Укажите название компании.")
    @Size(max = 255, message = "Название компании должно быть не длиннее 255 символов.")
    private String name;

    @Size(max = 4000, message = "Описание компании должно быть не длиннее 4000 символов.")
    private String description;
}
