package ru.msu.cmc.java_prak.dao;

import java.util.List;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;

/**
 * DAO для взаимного подбора вакансий и резюме.
 */
public interface MatchingDao {

    List<Vacancy> findSuitableVacanciesForPerson(
            Long personId,
            boolean onlyActive,
            boolean orderBySalaryAsc
    );

    List<Person> findSuitablePersonsForVacancy(
            Long vacancyId,
            boolean onlyLookingForJob,
            boolean orderByDesiredSalaryAsc
    );
}
