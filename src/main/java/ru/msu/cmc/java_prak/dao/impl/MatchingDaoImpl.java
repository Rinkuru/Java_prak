package ru.msu.cmc.java_prak.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.java_prak.dao.MatchingDao;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

/**
 * Hibernate-реализация DAO для взаимного подбора кандидатов и вакансий.
 */
@Repository
@Transactional(readOnly = true)
public class MatchingDaoImpl implements MatchingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Vacancy> findSuitableVacanciesForPerson(
            Long personId,
            boolean onlyActive,
            boolean orderBySalaryAsc
    ) {
        Person person = entityManager.find(Person.class, personId);
        if (person == null || isBlank(person.getDesiredPosition()) || person.getDesiredSalary() == null) {
            return List.of();
        }

        StringBuilder jpql = new StringBuilder(
                """
                select vacancy
                from Vacancy vacancy
                join fetch vacancy.company
                where vacancy.position = :desiredPosition
                  and vacancy.salary >= :desiredSalary
                """
        );

        if (onlyActive) {
            jpql.append(" and vacancy.status = true");
        }
        if (!isBlank(person.getEducation())) {
            jpql.append(
                    """
                     and (
                        vacancy.requiredEducation is null
                        or trim(vacancy.requiredEducation) = ''
                        or vacancy.requiredEducation = :education
                    )
                    """
            );
        }

        jpql.append(" order by vacancy.salary ");
        jpql.append(orderBySalaryAsc ? "asc" : "desc");
        jpql.append(", vacancy.id asc");

        TypedQuery<Vacancy> query = entityManager.createQuery(jpql.toString(), Vacancy.class)
                .setParameter("desiredPosition", person.getDesiredPosition())
                .setParameter("desiredSalary", person.getDesiredSalary());

        if (!isBlank(person.getEducation())) {
            query.setParameter("education", person.getEducation());
        }

        return query.getResultList();
    }

    @Override
    public List<Person> findSuitablePersonsForVacancy(
            Long vacancyId,
            boolean onlyLookingForJob,
            boolean orderByDesiredSalaryAsc
    ) {
        Vacancy vacancy = entityManager.find(Vacancy.class, vacancyId);
        if (vacancy == null) {
            return List.of();
        }

        StringBuilder jpql = new StringBuilder(
                """
                select person
                from Person person
                where person.desiredPosition is not null
                  and person.desiredSalary is not null
                  and person.desiredPosition = :position
                  and person.desiredSalary <= :salary
                """
        );

        if (onlyLookingForJob) {
            jpql.append(" and person.status = true");
        }
        if (!isBlank(vacancy.getRequiredEducation())) {
            jpql.append(" and person.education = :requiredEducation");
        }

        jpql.append(" order by person.desiredSalary ");
        jpql.append(orderByDesiredSalaryAsc ? "asc" : "desc");
        jpql.append(", person.fullName asc");

        TypedQuery<Person> query = entityManager.createQuery(jpql.toString(), Person.class)
                .setParameter("position", vacancy.getPosition())
                .setParameter("salary", vacancy.getSalary());

        if (!isBlank(vacancy.getRequiredEducation())) {
            query.setParameter("requiredEducation", vacancy.getRequiredEducation());
        }

        return query.getResultList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
