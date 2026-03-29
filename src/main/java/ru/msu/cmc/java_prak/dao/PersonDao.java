package ru.msu.cmc.java_prak.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import ru.msu.cmc.java_prak.model.Person;

/**
 * DAO для работы с резюме людей и типовыми поисковыми запросами по кандидатам.
 */
public interface PersonDao {

    List<Person> findAllOrderByFullName();

    Optional<Person> findById(Long id);

    Optional<Person> findCardById(Long id);

    Person save(Person person);

    Person update(Person person);

    boolean deleteById(Long id);

    List<Person> findByEducation(String education);

    List<Person> findByStatus(boolean status);

    List<Person> findByDesiredSalaryBetween(BigDecimal min, BigDecimal max);

    List<Person> findByWorkedCompanyId(Long companyId);

    List<Person> findByWorkedPosition(String position);

    List<Person> searchPeople(
            String education,
            Boolean status,
            BigDecimal minDesiredSalary,
            BigDecimal maxDesiredSalary,
            Long workedCompanyId,
            String workedPosition,
            boolean orderByDesiredSalaryAsc
    );
}
