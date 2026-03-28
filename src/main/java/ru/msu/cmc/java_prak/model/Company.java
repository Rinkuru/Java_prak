package ru.msu.cmc.java_prak.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Сущность компании-работодателя.
 * Хранит описание компании и связанные с ней вакансии и записи трудовой истории.
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @ToString.Include
    private String name;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "TEXT")
    @ToString.Include
    private String description;

    /**
     * Вакансии компании полностью принадлежат компании,
     * поэтому сохраняются и удаляются вместе с ней.
     */
    @OneToMany(
            mappedBy = "company",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    private List<Vacancy> vacancies = new ArrayList<>();

    /**
     * История работы лишь ссылается на компанию.
     * Каскадное удаление здесь не используется, чтобы сохранить ограничения БД.
     */
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @OrderBy("startDate DESC")
    private List<WorkExperience> workExperiences = new ArrayList<>();

    /**
     * Добавляет вакансию в компанию и синхронизирует обе стороны связи.
     */
    public void addVacancy(Vacancy vacancy) {
        vacancies.add(vacancy);
        vacancy.setCompany(this);
    }

    /**
     * Удаляет вакансию из компании и отвязывает её от компании.
     */
    public void removeVacancy(Vacancy vacancy) {
        vacancies.remove(vacancy);
        vacancy.setCompany(null);
    }

}
