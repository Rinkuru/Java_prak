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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность резюме человека.
 * Хранит персональные данные кандидата и его пожелания по будущей работе.
 */
@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "full_name", nullable = false)
    @ToString.Include
    private String fullName;

    @Column(name = "home_address", length = 500)
    @ToString.Include
    private String homeAddress;

    @Column(nullable = false)
    @ToString.Include
    private String education;

    @Column(nullable = false)
    @ToString.Include
    private boolean status;

    @Column(name = "desired_position")
    @ToString.Include
    private String desiredPosition;

    @Column(name = "desired_salary", precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal desiredSalary;

    /**
     * История работы человека хранится отдельными сущностями,
     * но доступна из резюме как обычная коллекция.
     */
    @OneToMany(
            mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("startDate DESC")
    private List<WorkExperience> workExperiences = new ArrayList<>();

    /**
     * Добавляет запись об опыте работы и синхронизирует обе стороны связи.
     */
    public void addWorkExperience(WorkExperience workExperience) {
        workExperiences.add(workExperience);
        workExperience.setPerson(this);
    }

    /**
     * Удаляет запись об опыте работы и отвязывает её от текущего человека.
     */
    public void removeWorkExperience(WorkExperience workExperience) {
        workExperiences.remove(workExperience);
        workExperience.setPerson(null);
    }
}
