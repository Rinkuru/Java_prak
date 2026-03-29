package ru.msu.cmc.java_prak.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.java_prak.dao.CompanyDao;
import ru.msu.cmc.java_prak.model.Company;

/**
 * Hibernate-реализация DAO для компаний.
 */
@Repository
@Transactional(readOnly = true)
public class CompanyDaoImpl implements CompanyDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Company> findAllOrderByName() {
        return entityManager.createQuery(
                """
                select c
                from Company c
                order by c.name asc
                """,
                Company.class
        ).getResultList();
    }

    @Override
    public Optional<Company> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Company.class, id));
    }

    @Override
    public Optional<Company> findCardById(Long id) {
        return entityManager.createQuery(
                """
                select distinct c
                from Company c
                left join fetch c.vacancies vacancy
                where c.id = :id
                """,
                Company.class
        ).setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional
    public Company save(Company company) {
        entityManager.persist(company);
        entityManager.flush();
        return company;
    }

    @Override
    @Transactional
    public Company update(Company company) {
        Company mergedCompany = entityManager.merge(company);
        entityManager.flush();
        return mergedCompany;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Company company = entityManager.find(Company.class, id);
        if (company == null) {
            return false;
        }

        entityManager.remove(company);
        entityManager.flush();
        return true;
    }

    @Override
    public List<Company> findByNameContaining(String namePart) {
        String normalizedNamePart = normalize(namePart);
        if (normalizedNamePart == null) {
            return findAllOrderByName();
        }

        return executeQuery(
                """
                select c
                from Company c
                where lower(c.name) like lower(:namePart)
                order by c.name asc
                """,
                Map.of("namePart", "%" + normalizedNamePart + "%")
        );
    }

    @Override
    public List<Company> findWithOpenVacancies() {
        return entityManager.createQuery(
                """
                select c
                from Company c
                where exists (
                    select 1
                    from Vacancy vacancy
                    where vacancy.company = c
                      and vacancy.status = true
                )
                order by c.name asc
                """,
                Company.class
        ).getResultList();
    }

    @Override
    public List<Company> searchCompanies(String namePart, Boolean onlyWithOpenVacancies) {
        String normalizedNamePart = normalize(namePart);

        StringBuilder jpql = new StringBuilder(
                """
                select c
                from Company c
                where 1 = 1
                """
        );

        Map<String, Object> parameters = new LinkedHashMap<>();

        if (normalizedNamePart != null) {
            jpql.append(" and lower(c.name) like lower(:namePart)");
            parameters.put("namePart", "%" + normalizedNamePart + "%");
        }

        if (Boolean.TRUE.equals(onlyWithOpenVacancies)) {
            jpql.append(
                    """
                     and exists (
                        select 1
                        from Vacancy vacancy
                        where vacancy.company = c
                          and vacancy.status = true
                    )
                    """
            );
        }

        jpql.append(" order by c.name asc");
        return executeQuery(jpql.toString(), parameters);
    }

    private List<Company> executeQuery(String jpql, Map<String, Object> parameters) {
        TypedQuery<Company> query = entityManager.createQuery(jpql, Company.class);
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
