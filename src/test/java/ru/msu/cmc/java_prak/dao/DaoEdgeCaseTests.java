package ru.msu.cmc.java_prak.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import ru.msu.cmc.java_prak.dao.impl.CompanyDaoImpl;
import ru.msu.cmc.java_prak.dao.impl.MatchingDaoImpl;
import ru.msu.cmc.java_prak.dao.impl.PersonDaoImpl;
import ru.msu.cmc.java_prak.dao.impl.VacancyDaoImpl;
import ru.msu.cmc.java_prak.dao.impl.WorkExperienceDaoImpl;
import ru.msu.cmc.java_prak.model.Company;
import ru.msu.cmc.java_prak.model.Person;
import ru.msu.cmc.java_prak.model.Vacancy;
import ru.msu.cmc.java_prak.model.WorkExperience;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/*
 * Пограничные и альтернативные сценарии для DAO-методов.
 * Эти тесты закрывают ветви "ничего не найдено", blank/null фильтры и разные варианты поведения.
 */
@DataJpaTest
@Import({
        PersonDaoImpl.class,
        CompanyDaoImpl.class,
        VacancyDaoImpl.class,
        WorkExperienceDaoImpl.class,
        MatchingDaoImpl.class
})
public class DaoEdgeCaseTests extends AbstractTestNGSpringContextTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonDao personDao;

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private VacancyDao vacancyDao;

    @Autowired
    private WorkExperienceDao workExperienceDao;

    @Autowired
    private MatchingDao matchingDao;

    @Test
    public void personDaoShouldHandleMissingRecordsEmptyFiltersAndDescendingSort() {
        Company company = persistCompany("ВКонтакте", "IT-компания");

        Person higherSalaryPerson = persistPerson(
                "Соколов Иван Николаевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "250000.00"
        );
        Person lowerSalaryPerson = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "210000.00"
        );

        entityManager.persist(buildWorkExperience(
                lowerSalaryPerson,
                company,
                "Backend-разработчик",
                "190000.00",
                LocalDate.of(2020, 2, 1),
                LocalDate.of(2023, 6, 30)
        ));
        flushAndClear();

        assertTrue(personDao.findById(999_999L).isEmpty());
        assertTrue(personDao.findCardById(999_999L).isEmpty());
        assertFalse(personDao.deleteById(999_999L));

        assertTrue(personDao.findByEducation("Неизвестное образование").isEmpty());
        assertTrue(personDao.findByStatus(false).isEmpty());
        assertTrue(personDao.findByDesiredSalaryBetween(new BigDecimal("1.00"), new BigDecimal("100.00")).isEmpty());
        assertTrue(personDao.findByWorkedCompanyId(999_999L).isEmpty());
        assertTrue(personDao.findByWorkedPosition("Несуществующая должность").isEmpty());

        List<Person> blankSearchResult = personDao.searchPeople("   ", null, null, null, null, "   ", false);
        assertEquals(blankSearchResult.size(), 2);
        assertEquals(blankSearchResult.get(0).getId(), higherSalaryPerson.getId());
        assertEquals(blankSearchResult.get(1).getId(), lowerSalaryPerson.getId());

        assertTrue(
                personDao.searchPeople(
                        "Высшее медицинское",
                        true,
                        new BigDecimal("500000.00"),
                        new BigDecimal("600000.00"),
                        company.getId(),
                        "Терапевт",
                        true
                ).isEmpty()
        );
    }

    @Test
    public void companyDaoShouldHandleMissingRecordsBlankFiltersAndEmptyOpenVacancySearch() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("Максимед", "Медицина");
        persistVacancy(firstCompany, "Backend-разработчик", "240000.00", "Высшее техническое", false);
        flushAndClear();

        assertTrue(companyDao.findById(999_999L).isEmpty());
        assertTrue(companyDao.findCardById(999_999L).isEmpty());
        assertFalse(companyDao.deleteById(999_999L));

        List<Company> allCompaniesByBlankName = companyDao.findByNameContaining("   ");
        assertEquals(allCompaniesByBlankName.size(), 2);
        assertEquals(allCompaniesByBlankName.get(0).getName(), "ВКонтакте");
        assertEquals(allCompaniesByBlankName.get(1).getName(), "Максимед");

        assertTrue(companyDao.findWithOpenVacancies().isEmpty());
        assertTrue(companyDao.searchCompanies("zzz", true).isEmpty());
        assertEquals(companyDao.searchCompanies("   ", false).size(), 2);

        assertEquals(companyDao.findCardById(secondCompany.getId()).orElseThrow().getVacancies().size(), 0);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void companyDaoShouldFailToDeleteCompanyReferencedByWorkExperience() {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        Person person = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );
        entityManager.persist(buildWorkExperience(
                person,
                company,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2021, 1, 1),
                null
        ));
        flushAndClear();

        companyDao.deleteById(company.getId());
    }

    @Test
    public void vacancyDaoShouldHandleMissingRecordsEmptyFiltersAndBlankSearch() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("1С", "Разработчик ПО");
        persistVacancy(firstCompany, "Backend-разработчик", "240000.00", "Высшее техническое", true);
        persistVacancy(secondCompany, "Тестировщик", "120000.00", "Высшее техническое", true);
        flushAndClear();

        assertTrue(vacancyDao.findById(999_999L).isEmpty());
        assertTrue(vacancyDao.findCardById(999_999L).isEmpty());
        assertFalse(vacancyDao.deleteById(999_999L));
        assertTrue(vacancyDao.updateStatus(999_999L, true).isEmpty());

        assertTrue(vacancyDao.findByCompanyId(999_999L).isEmpty());
        assertTrue(vacancyDao.findByPosition("Несуществующая вакансия").isEmpty());
        assertTrue(vacancyDao.findBySalaryBetween(new BigDecimal("1.00"), new BigDecimal("10.00")).isEmpty());
        assertTrue(vacancyDao.findByStatus(false).isEmpty());

        List<Vacancy> blankSearchResult = vacancyDao.searchVacancies(null, "   ", "   ", null, null, null);
        assertEquals(blankSearchResult.size(), 2);
        assertTrue(vacancyDao.searchVacancies(null, "zzz", "Backend-разработчик", null, null, true).isEmpty());
    }

    @Test
    public void workExperienceDaoShouldHandleMissingRecordsAndOpenEndedOverlapCases() {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        Person person = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );

        WorkExperience openEndedExperience = buildWorkExperience(
                person,
                company,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2021, 1, 1),
                null
        );
        entityManager.persist(openEndedExperience);
        flushAndClear();

        assertTrue(workExperienceDao.findById(999_999L).isEmpty());
        assertFalse(workExperienceDao.deleteById(999_999L));
        assertTrue(workExperienceDao.findByPersonIdOrderByStartDateDesc(999_999L).isEmpty());
        assertTrue(workExperienceDao.findByCompanyId(999_998L).isEmpty());

        assertTrue(
                workExperienceDao.existsOverlappingPeriod(
                        person.getId(),
                        LocalDate.of(2022, 1, 1),
                        null,
                        null
                )
        );
        assertFalse(
                workExperienceDao.existsOverlappingPeriod(
                        person.getId(),
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2020, 12, 31),
                        null
                )
        );
        assertFalse(
                workExperienceDao.existsOverlappingPeriod(
                        person.getId(),
                        LocalDate.of(2022, 1, 1),
                        null,
                        openEndedExperience.getId()
                )
        );
    }

    @Test
    public void matchingDaoShouldHandleMissingDataBlankEducationAndBothSortDirections() {
        Company company = persistCompany("ВКонтакте", "IT-компания");

        Vacancy lowerSalaryVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "210000.00",
                "Высшее техническое",
                true
        );
        Vacancy higherSalaryVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "260000.00",
                "Высшее техническое",
                true
        );
        Vacancy closedVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "300000.00",
                "Высшее техническое",
                false
        );

        Person matchingPerson = persistPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "200000.00"
        );
        Person withoutDesiredPosition = persistPerson(
                "Петров Пётр Сергеевич",
                "Высшее техническое",
                true,
                null,
                "190000.00"
        );
        Person withoutDesiredSalary = persistPerson(
                "Сидоров Сидор Иванович",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                null
        );

        Vacancy vacancyWithBlankEducation = persistVacancy(
                company,
                "Backend-разработчик",
                "260000.00",
                "   ",
                true
        );
        Person technicalPerson = persistPerson(
                "Технический кандидат",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "240000.00"
        );
        Person medicalPerson = persistPerson(
                "Медицинский кандидат",
                "Высшее медицинское",
                true,
                "Backend-разработчик",
                "210000.00"
        );
        Person notLookingPerson = persistPerson(
                "Не ищет работу",
                "Высшее техническое",
                false,
                "Backend-разработчик",
                "230000.00"
        );
        flushAndClear();

        assertTrue(matchingDao.findSuitableVacanciesForPerson(999_999L, true, true).isEmpty());
        assertTrue(matchingDao.findSuitableVacanciesForPerson(withoutDesiredPosition.getId(), true, true).isEmpty());
        assertTrue(matchingDao.findSuitableVacanciesForPerson(withoutDesiredSalary.getId(), true, true).isEmpty());

        List<Vacancy> ascendingVacancies = matchingDao.findSuitableVacanciesForPerson(matchingPerson.getId(), true, true);
        assertEquals(ascendingVacancies.size(), 3);
        assertEquals(ascendingVacancies.get(0).getId(), lowerSalaryVacancy.getId());
        assertEquals(ascendingVacancies.get(1).getId(), higherSalaryVacancy.getId());
        assertEquals(ascendingVacancies.get(2).getId(), vacancyWithBlankEducation.getId());

        List<Vacancy> descendingVacancies = matchingDao.findSuitableVacanciesForPerson(matchingPerson.getId(), false, false);
        assertEquals(descendingVacancies.size(), 4);
        assertEquals(descendingVacancies.get(0).getId(), closedVacancy.getId());

        assertTrue(matchingDao.findSuitablePersonsForVacancy(999_999L, true, true).isEmpty());

        List<Person> ascendingPeople = matchingDao.findSuitablePersonsForVacancy(vacancyWithBlankEducation.getId(), true, true);
        assertEquals(ascendingPeople.size(), 3);
        assertEquals(ascendingPeople.get(0).getId(), matchingPerson.getId());
        assertEquals(ascendingPeople.get(1).getId(), medicalPerson.getId());
        assertEquals(ascendingPeople.get(2).getId(), technicalPerson.getId());

        List<Person> descendingPeople = matchingDao.findSuitablePersonsForVacancy(vacancyWithBlankEducation.getId(), false, false);
        assertEquals(descendingPeople.size(), 4);
        assertEquals(descendingPeople.get(0).getId(), technicalPerson.getId());
        assertEquals(descendingPeople.get(1).getId(), notLookingPerson.getId());
    }

    @Test
    public void matchingDaoShouldIgnoreBlankPersonEducationWhenSearchingVacancies() {
        Company company = persistCompany("ВКонтакте", "IT-компания");

        Vacancy technicalVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "210000.00",
                "Высшее техническое",
                true
        );
        Vacancy medicalVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "220000.00",
                "Высшее медицинское",
                true
        );
        Vacancy noEducationVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "230000.00",
                null,
                true
        );
        Vacancy closedVacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "240000.00",
                "Среднее специальное",
                false
        );
        persistVacancy(
                company,
                "Тестировщик",
                "260000.00",
                "Высшее техническое",
                true
        );
        persistVacancy(
                company,
                "Backend-разработчик",
                "190000.00",
                "Высшее техническое",
                true
        );

        Person personWithBlankEducation = persistPerson(
                "Кандидат без указанного образования",
                "   ",
                true,
                "Backend-разработчик",
                "200000.00"
        );
        flushAndClear();

        List<Vacancy> activeVacancies = matchingDao.findSuitableVacanciesForPerson(
                personWithBlankEducation.getId(),
                true,
                true
        );
        assertEquals(activeVacancies.size(), 3);
        assertEquals(activeVacancies.get(0).getId(), technicalVacancy.getId());
        assertEquals(activeVacancies.get(1).getId(), medicalVacancy.getId());
        assertEquals(activeVacancies.get(2).getId(), noEducationVacancy.getId());

        List<Vacancy> allMatchingVacancies = matchingDao.findSuitableVacanciesForPerson(
                personWithBlankEducation.getId(),
                false,
                false
        );
        assertEquals(allMatchingVacancies.size(), 4);
        assertEquals(allMatchingVacancies.get(0).getId(), closedVacancy.getId());
        assertEquals(allMatchingVacancies.get(1).getId(), noEducationVacancy.getId());
        assertEquals(allMatchingVacancies.get(2).getId(), medicalVacancy.getId());
        assertEquals(allMatchingVacancies.get(3).getId(), technicalVacancy.getId());
    }

    private Company persistCompany(String name, String description) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        entityManager.persist(company);
        return company;
    }

    private Person persistPerson(
            String fullName,
            String education,
            boolean status,
            String desiredPosition,
            String desiredSalary
    ) {
        Person person = new Person();
        person.setFullName(fullName);
        person.setHomeAddress("Москва");
        person.setEducation(education);
        person.setStatus(status);
        person.setDesiredPosition(desiredPosition);
        person.setDesiredSalary(desiredSalary == null ? null : new BigDecimal(desiredSalary));
        entityManager.persist(person);
        return person;
    }

    private Vacancy persistVacancy(
            Company company,
            String position,
            String salary,
            String requiredEducation,
            boolean status
    ) {
        Vacancy vacancy = new Vacancy();
        vacancy.setCompany(company);
        vacancy.setPosition(position);
        vacancy.setSalary(new BigDecimal(salary));
        vacancy.setRequiredEducation(requiredEducation);
        vacancy.setRequirements("Требования к вакансии");
        vacancy.setStatus(status);
        entityManager.persist(vacancy);
        return vacancy;
    }

    private WorkExperience buildWorkExperience(
            Person person,
            Company company,
            String position,
            String salary,
            LocalDate startDate,
            LocalDate endDate
    ) {
        WorkExperience workExperience = new WorkExperience();
        workExperience.setPerson(person);
        workExperience.setCompany(company);
        workExperience.setPosition(position);
        workExperience.setSalary(new BigDecimal(salary));
        workExperience.setStartDate(startDate);
        workExperience.setEndDate(endDate);
        return workExperience;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
