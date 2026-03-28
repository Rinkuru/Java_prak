package ru.msu.cmc.java_prak.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность записи трудовой истории человека.
 * Хранит компанию, должность, зарплату и период работы.
 */
@Entity
@Table(name = "work_experience")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Запись опыта работы всегда принадлежит конкретному человеку.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /**
     * Запись опыта работы ссылается на компанию, где работал человек.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    @ToString.Include
    private String position;

    @Column(nullable = false, precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal salary;

    @Column(name = "start_date", nullable = false)
    @ToString.Include
    private LocalDate startDate;

    @Column(name = "end_date")
    @ToString.Include
    private LocalDate endDate;
}
