package ru.msu.cmc.java_prak.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.java_prak.dao.WorkExperienceDao;
import ru.msu.cmc.java_prak.model.WorkExperience;

/**
 * Hibernate-реализация DAO для истории работы.
 */
@Repository
@Transactional(readOnly = true)
public class WorkExperienceDaoImpl implements WorkExperienceDao {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<WorkExperience> findById(Long id) {
        return entityManager.createQuery(
                """
                select workExperience
                from WorkExperience workExperience
                join fetch workExperience.person
                join fetch workExperience.company
                where workExperience.id = :id
                """,
                WorkExperience.class
        ).setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<WorkExperience> findByPersonIdOrderByStartDateDesc(Long personId) {
        return executeQuery(
                """
                select workExperience
                from WorkExperience workExperience
                join fetch workExperience.person
                join fetch workExperience.company
                where workExperience.person.id = :personId
                order by workExperience.startDate desc
                """,
                Map.of("personId", personId)
        );
    }

    @Override
    public List<WorkExperience> findByCompanyId(Long companyId) {
        return executeQuery(
                """
                select workExperience
                from WorkExperience workExperience
                join fetch workExperience.person
                join fetch workExperience.company
                where workExperience.company.id = :companyId
                order by workExperience.startDate desc
                """,
                Map.of("companyId", companyId)
        );
    }

    @Override
    @Transactional
    public WorkExperience save(WorkExperience workExperience) {
        entityManager.persist(workExperience);
        entityManager.flush();
        return workExperience;
    }

    @Override
    @Transactional
    public WorkExperience update(WorkExperience workExperience) {
        WorkExperience mergedWorkExperience = entityManager.merge(workExperience);
        entityManager.flush();
        return mergedWorkExperience;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        WorkExperience workExperience = entityManager.find(WorkExperience.class, id);
        if (workExperience == null) {
            return false;
        }

        entityManager.remove(workExperience);
        entityManager.flush();
        return true;
    }

    @Override
    public boolean existsOverlappingPeriod(
            Long personId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludedWorkExperienceId
    ) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;

        Long overlapsCount = entityManager.createQuery(
                """
                select count(workExperience)
                from WorkExperience workExperience
                where workExperience.person.id = :personId
                  and (:excludedWorkExperienceId is null
                       or workExperience.id <> :excludedWorkExperienceId)
                  and workExperience.startDate <= :effectiveEndDate
                  and coalesce(workExperience.endDate, :maxDate) >= :startDate
                """,
                Long.class
        ).setParameter("personId", personId)
                .setParameter("excludedWorkExperienceId", excludedWorkExperienceId)
                .setParameter("effectiveEndDate", effectiveEndDate)
                .setParameter("maxDate", MAX_DATE)
                .setParameter("startDate", startDate)
                .getSingleResult();

        return overlapsCount > 0;
    }

    private List<WorkExperience> executeQuery(String jpql, Map<String, Object> parameters) {
        TypedQuery<WorkExperience> query = entityManager.createQuery(jpql, WorkExperience.class);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }
}
