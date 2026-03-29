package ru.msu.cmc.java_prak.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import ru.msu.cmc.java_prak.model.Vacancy;

/**
 * DAO для работы с вакансиями и фильтрами списка вакансий.
 */
public interface VacancyDao {

    List<Vacancy> findAllOrderById();

    Optional<Vacancy> findById(Long id);

    Optional<Vacancy> findCardById(Long id);

    Vacancy save(Vacancy vacancy);

    Vacancy update(Vacancy vacancy);

    boolean deleteById(Long id);

    List<Vacancy> findByCompanyId(Long companyId);

    List<Vacancy> findByPosition(String position);

    List<Vacancy> findBySalaryBetween(BigDecimal min, BigDecimal max);

    List<Vacancy> findByStatus(boolean status);

    Optional<Vacancy> updateStatus(Long vacancyId, boolean status);

    List<Vacancy> searchVacancies(
            Long companyId,
            String companyNamePart,
            String position,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Boolean status
    );
}
