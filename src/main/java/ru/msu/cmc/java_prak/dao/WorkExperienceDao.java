package ru.msu.cmc.java_prak.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import ru.msu.cmc.java_prak.model.WorkExperience;

/**
 * DAO для работы с историей трудоустройства кандидатов.
 */
public interface WorkExperienceDao {

    Optional<WorkExperience> findById(Long id);

    List<WorkExperience> findByPersonIdOrderByStartDateDesc(Long personId);

    List<WorkExperience> findByCompanyId(Long companyId);

    WorkExperience save(WorkExperience workExperience);

    WorkExperience update(WorkExperience workExperience);

    boolean deleteById(Long id);

    boolean existsOverlappingPeriod(
            Long personId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludedWorkExperienceId
    );
}
