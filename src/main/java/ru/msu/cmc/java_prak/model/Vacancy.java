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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Сущность вакансии.
 * Описывает открытую или закрытую позицию конкретной компании.
 */
@Entity
@Table(name = "vacancy")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Каждая вакансия принадлежит одной компании и хранит внешний ключ company_id.
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

    @Column(name = "required_education")
    @ToString.Include
    private String requiredEducation;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "TEXT")
    @ToString.Include
    private String requirements;

    @Column(nullable = false)
    @ToString.Include
    private boolean status;
}
