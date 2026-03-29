package ru.msu.cmc.java_prak.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.java_prak.dao.VacancyDao;
import ru.msu.cmc.java_prak.model.Vacancy;

/**
 * Hibernate-реализация DAO для вакансий.
 */
@Repository
@Transactional(readOnly = true)
public class VacancyDaoImpl implements VacancyDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Vacancy> findAllOrderById() {
        return entityManager.createQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                order by vacancy.id asc
                """,
                Vacancy.class
        ).getResultList();
    }

    @Override
    public Optional<Vacancy> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Vacancy.class, id));
    }

    @Override
    public Optional<Vacancy> findCardById(Long id) {
        return entityManager.createQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.id = :id
                """,
                Vacancy.class
        ).setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional
    public Vacancy save(Vacancy vacancy) {
        entityManager.persist(vacancy);
        entityManager.flush();
        return vacancy;
    }

    @Override
    @Transactional
    public Vacancy update(Vacancy vacancy) {
        Vacancy mergedVacancy = entityManager.merge(vacancy);
        entityManager.flush();
        return mergedVacancy;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Vacancy vacancy = entityManager.find(Vacancy.class, id);
        if (vacancy == null) {
            return false;
        }

        entityManager.remove(vacancy);
        entityManager.flush();
        return true;
    }

    @Override
    public List<Vacancy> findByCompanyId(Long companyId) {
        return executeQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.company.id = :companyId
                order by vacancy.id asc
                """,
                Map.of("companyId", companyId)
        );
    }

    @Override
    public List<Vacancy> findByPosition(String position) {
        return executeQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.position = :position
                order by vacancy.id asc
                """,
                Map.of("position", position)
        );
    }

    @Override
    public List<Vacancy> findBySalaryBetween(BigDecimal min, BigDecimal max) {
        return executeQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.salary between :min and :max
                order by vacancy.salary asc, vacancy.id asc
                """,
                Map.of("min", min, "max", max)
        );
    }

    @Override
    public List<Vacancy> findByStatus(boolean status) {
        return executeQuery(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.status = :status
                order by vacancy.id asc
                """,
                Map.of("status", status)
        );
    }

    @Override
    @Transactional
    public Optional<Vacancy> updateStatus(Long vacancyId, boolean status) {
        Vacancy vacancy = entityManager.find(Vacancy.class, vacancyId);
        if (vacancy == null) {
            return Optional.empty();
        }

        vacancy.setStatus(status);
        entityManager.flush();
        return Optional.of(vacancy);
    }

    @Override
    public List<Vacancy> searchVacancies(
            Long companyId,
            String companyNamePart,
            String position,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Boolean status
    ) {
        String normalizedCompanyNamePart = normalize(companyNamePart);
        String normalizedPosition = normalize(position);

        StringBuilder jpql = new StringBuilder(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company company
                where 1 = 1
                """
        );

        Map<String, Object> parameters = new LinkedHashMap<>();

        if (companyId != null) {
            jpql.append(" and company.id = :companyId");
            parameters.put("companyId", companyId);
        }
        if (normalizedCompanyNamePart != null) {
            jpql.append(" and lower(company.name) like lower(:companyNamePart)");
            parameters.put("companyNamePart", "%" + normalizedCompanyNamePart + "%");
        }
        if (normalizedPosition != null) {
            jpql.append(" and vacancy.position = :position");
            parameters.put("position", normalizedPosition);
        }
        if (minSalary != null) {
            jpql.append(" and vacancy.salary >= :minSalary");
            parameters.put("minSalary", minSalary);
        }
        if (maxSalary != null) {
            jpql.append(" and vacancy.salary <= :maxSalary");
            parameters.put("maxSalary", maxSalary);
        }
        if (status != null) {
            jpql.append(" and vacancy.status = :status");
            parameters.put("status", status);
        }

        jpql.append(" order by company.name asc, vacancy.id asc");
        return executeQuery(jpql.toString(), parameters);
    }

    private List<Vacancy> executeQuery(String jpql, Map<String, Object> parameters) {
        TypedQuery<Vacancy> query = entityManager.createQuery(jpql, Vacancy.class);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
