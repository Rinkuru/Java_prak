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
import ru.msu.cmc.java_prak.dao.PersonDao;
import ru.msu.cmc.java_prak.model.Person;

/**
 * Hibernate-реализация DAO для работы с кандидатами.
 * Содержит CRUD и запросы, которые нужны для списков и фильтров раздела "Люди".
 */
@Repository
@Transactional(readOnly = true)
public class PersonDaoImpl implements PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Person> findAllOrderByFullName() {
        return entityManager.createQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                order by p.fullName asc
                """,
                Person.class
        ).getResultList();
    }

    @Override
    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Person.class, id));
    }

    @Override
    public Optional<Person> findCardById(Long id) {
        return entityManager.createQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where p.id = :id
                """,
                Person.class
        ).setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional
    public Person save(Person person) {
        entityManager.persist(person);
        entityManager.flush();
        return person;
    }

    @Override
    @Transactional
    public Person update(Person person) {
        Person mergedPerson = entityManager.merge(person);
        entityManager.flush();
        return mergedPerson;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Person person = entityManager.find(Person.class, id);
        if (person == null) {
            return false;
        }

        entityManager.remove(person);
        entityManager.flush();
        return true;
    }

    @Override
    public List<Person> findByEducation(String education) {
        return executeFetchQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where p.education = :education
                order by p.fullName asc
                """,
                Map.of("education", education)
        );
    }

    @Override
    public List<Person> findByStatus(boolean status) {
        return executeFetchQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where p.status = :status
                order by p.fullName asc
                """,
                Map.of("status", status)
        );
    }

    @Override
    public List<Person> findByDesiredSalaryBetween(BigDecimal min, BigDecimal max) {
        return executeFetchQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where p.desiredSalary between :min and :max
                order by p.desiredSalary asc, p.fullName asc
                """,
                Map.of("min", min, "max", max)
        );
    }

    @Override
    public List<Person> findByWorkedCompanyId(Long companyId) {
        return executeFetchQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where exists (
                    select 1
                    from WorkExperience workExperience
                    where workExperience.person = p
                      and workExperience.company.id = :companyId
                )
                order by p.fullName asc
                """,
                Map.of("companyId", companyId)
        );
    }

    @Override
    public List<Person> findByWorkedPosition(String position) {
        return executeFetchQuery(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where exists (
                    select 1
                    from WorkExperience workExperience
                    where workExperience.person = p
                      and workExperience.position = :position
                )
                order by p.fullName asc
                """,
                Map.of("position", position)
        );
    }

    @Override
    public List<Person> searchPeople(
            String education,
            Boolean status,
            BigDecimal minDesiredSalary,
            BigDecimal maxDesiredSalary,
            Long workedCompanyId,
            String workedPosition,
            boolean orderByDesiredSalaryAsc
    ) {
        String normalizedEducation = normalize(education);
        String normalizedWorkedPosition = normalize(workedPosition);

        StringBuilder jpql = new StringBuilder(
                """
                select distinct p
                from Person p
                left join fetch p.workExperiences we
                left join fetch we.company
                where 1 = 1
                """
        );

        Map<String, Object> parameters = new LinkedHashMap<>();

        // Условия добавляются только для реально переданных фильтров,
        // чтобы один метод покрывал и простые, и комбинированные поиски.
        if (normalizedEducation != null) {
            jpql.append(" and p.education = :education");
            parameters.put("education", normalizedEducation);
        }
        if (status != null) {
            jpql.append(" and p.status = :status");
            parameters.put("status", status);
        }
        if (minDesiredSalary != null) {
            jpql.append(" and p.desiredSalary >= :minDesiredSalary");
            parameters.put("minDesiredSalary", minDesiredSalary);
        }
        if (maxDesiredSalary != null) {
            jpql.append(" and p.desiredSalary <= :maxDesiredSalary");
            parameters.put("maxDesiredSalary", maxDesiredSalary);
        }
        if (workedCompanyId != null) {
            jpql.append(
                    """
                     and exists (
                        select 1
                        from WorkExperience companyExperience
                        where companyExperience.person = p
                          and companyExperience.company.id = :workedCompanyId
                    )
                    """
            );
            parameters.put("workedCompanyId", workedCompanyId);
        }
        if (normalizedWorkedPosition != null) {
            jpql.append(
                    """
                     and exists (
                        select 1
                        from WorkExperience positionExperience
                        where positionExperience.person = p
                          and positionExperience.position = :workedPosition
                    )
                    """
            );
            parameters.put("workedPosition", normalizedWorkedPosition);
        }

        jpql.append(" order by case when p.desiredSalary is null then 1 else 0 end, p.desiredSalary ");
        jpql.append(orderByDesiredSalaryAsc ? "asc" : "desc");
        jpql.append(", p.fullName asc");

        return executeFetchQuery(jpql.toString(), parameters);
    }

    private List<Person> executeFetchQuery(String jpql, Map<String, Object> parameters) {
        TypedQuery<Person> query = entityManager.createQuery(jpql, Person.class);
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
