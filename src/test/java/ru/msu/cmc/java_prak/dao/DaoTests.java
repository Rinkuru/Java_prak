package ru.msu.cmc.java_prak.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.Hibernate;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@DataJpaTest
@Import({
        PersonDaoImpl.class,
        CompanyDaoImpl.class,
        VacancyDaoImpl.class,
        WorkExperienceDaoImpl.class,
        MatchingDaoImpl.class
})
public class DaoTests extends AbstractTestNGSpringContextTests {

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
    public void personDaoShouldSaveUpdateFindAndDeletePerson() {
        Person person = buildPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );

        Person savedPerson = personDao.save(person);
        flushAndClear();

        assertNotNull(savedPerson.getId());
        assertTrue(personDao.findById(savedPerson.getId()).isPresent());

        Person personForUpdate = personDao.findById(savedPerson.getId()).orElseThrow();
        personForUpdate.setHomeAddress("Москва, новый адрес");
        personDao.update(personForUpdate);
        flushAndClear();

        Person updatedPerson = personDao.findById(savedPerson.getId()).orElseThrow();
        assertEquals(updatedPerson.getHomeAddress(), "Москва, новый адрес");

        assertTrue(personDao.deleteById(savedPerson.getId()));
        flushAndClear();
        assertTrue(personDao.findById(savedPerson.getId()).isEmpty());
    }

    @Test
    public void personDaoShouldLoadCardAndSearchByFilters() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("1С", "Разработчик ПО");

        Person firstPerson = buildPerson(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );

        WorkExperience firstExperience = buildWorkExperience(
                firstPerson,
                secondCompany,
                "Разработчик 1С",
                "130000.00",
                LocalDate.of(2018, 7, 1),
                LocalDate.of(2021, 3, 31)
        );
        WorkExperience secondExperience = buildWorkExperience(
                firstPerson,
                firstCompany,
                "Backend-разработчик",
                "210000.00",
                LocalDate.of(2021, 4, 1),
                null
        );
        firstPerson.addWorkExperience(firstExperience);
        firstPerson.addWorkExperience(secondExperience);
        personDao.save(firstPerson);

        Person secondPerson = buildPerson(
                "Петров Пётр Сергеевич",
                "Высшее медицинское",
                false,
                "Терапевт",
                "100000.00"
        );
        personDao.save(secondPerson);
        flushAndClear();

        List<Person> allPeople = personDao.findAllOrderByFullName();
        assertEquals(allPeople.size(), 2);
        assertEquals(allPeople.get(0).getFullName(), "Иванов Алексей Дмитриевич");
        assertTrue(Hibernate.isInitialized(allPeople.get(0).getWorkExperiences()));

        Person personCard = personDao.findCardById(firstPerson.getId()).orElseThrow();
        assertTrue(Hibernate.isInitialized(personCard.getWorkExperiences()));
        assertEquals(personCard.getWorkExperiences().size(), 2);
        assertEquals(personCard.getWorkExperiences().get(0).getCompany().getName(), "ВКонтакте");

        assertEquals(personDao.findByEducation("Высшее техническое").size(), 1);
        assertEquals(personDao.findByStatus(true).size(), 1);
        assertEquals(personDao.findByDesiredSalaryBetween(new BigDecimal("200000.00"), new BigDecimal("240000.00")).size(), 1);
        assertEquals(personDao.findByWorkedCompanyId(firstCompany.getId()).size(), 1);
        assertEquals(personDao.findByWorkedPosition("Разработчик 1С").size(), 1);

        assertEquals(
                personDao.searchPeople(
                        "Высшее техническое",
                        true,
                        new BigDecimal("200000.00"),
                        new BigDecimal("240000.00"),
                        firstCompany.getId(),
                        "Backend-разработчик",
                        true
                ).size(),
                1
        );

        assertEquals(personDao.searchPeople(null, null, null, null, null, null, true).size(), 2);
    }

    @Test
    public void companyDaoShouldSaveUpdateFindAndDeleteCompany() {
        Company company = new Company();
        company.setName("Яндекс");
        company.setDescription("Технологическая компания");

        Company savedCompany = companyDao.save(company);
        flushAndClear();

        assertNotNull(savedCompany.getId());
        assertTrue(companyDao.findById(savedCompany.getId()).isPresent());

        Company companyForUpdate = companyDao.findById(savedCompany.getId()).orElseThrow();
        companyForUpdate.setDescription("Обновлённое описание");
        companyDao.update(companyForUpdate);
        flushAndClear();

        Company updatedCompany = companyDao.findById(savedCompany.getId()).orElseThrow();
        assertEquals(updatedCompany.getDescription(), "Обновлённое описание");
        assertEquals(companyDao.findAllOrderByName().size(), 1);

        assertTrue(companyDao.deleteById(savedCompany.getId()));
        flushAndClear();
        assertTrue(companyDao.findById(savedCompany.getId()).isEmpty());
    }

    @Test
    public void companyDaoShouldFindByFiltersAndLoadCard() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("Максимед", "Медицина");

        persistVacancy(firstCompany, "Backend-разработчик", "240000.00", "Высшее техническое", true);
        persistVacancy(secondCompany, "Терапевт", "110000.00", "Высшее медицинское", false);
        flushAndClear();

        assertEquals(companyDao.findByNameContaining("конт").size(), 1);
        assertEquals(companyDao.findWithOpenVacancies().size(), 1);
        assertEquals(companyDao.searchCompanies("мед", false).size(), 1);
        assertEquals(companyDao.searchCompanies(null, true).size(), 1);

        Company companyCard = companyDao.findCardById(firstCompany.getId()).orElseThrow();
        assertTrue(Hibernate.isInitialized(companyCard.getVacancies()));
        assertEquals(companyCard.getVacancies().size(), 1);
    }

    @Test
    public void vacancyDaoShouldSaveUpdateFindDeleteAndChangeStatus() {
        Company company = persistCompany("Яндекс", "Технологическая компания");

        Vacancy vacancy = new Vacancy();
        vacancy.setCompany(company);
        vacancy.setPosition("Java-разработчик");
        vacancy.setSalary(new BigDecimal("200000.00"));
        vacancy.setRequiredEducation("Высшее техническое");
        vacancy.setRequirements("Spring Boot");
        vacancy.setStatus(true);

        Vacancy savedVacancy = vacancyDao.save(vacancy);
        flushAndClear();

        assertNotNull(savedVacancy.getId());
        assertEquals(vacancyDao.findAllOrderById().size(), 1);
        assertTrue(vacancyDao.findById(savedVacancy.getId()).isPresent());

        Vacancy vacancyCard = vacancyDao.findCardById(savedVacancy.getId()).orElseThrow();
        assertTrue(Hibernate.isInitialized(vacancyCard.getCompany()));
        assertEquals(vacancyCard.getCompany().getName(), "Яндекс");

        Vacancy vacancyForUpdate = vacancyDao.findById(savedVacancy.getId()).orElseThrow();
        vacancyForUpdate.setRequirements("Spring Boot и PostgreSQL");
        vacancyDao.update(vacancyForUpdate);
        flushAndClear();

        assertEquals(
                vacancyDao.findCardById(savedVacancy.getId()).orElseThrow().getRequirements(),
                "Spring Boot и PostgreSQL"
        );

        Vacancy reopenedVacancy = vacancyDao.updateStatus(savedVacancy.getId(), false).orElseThrow();
        assertFalse(reopenedVacancy.isStatus());
        flushAndClear();
        assertFalse(vacancyDao.findById(savedVacancy.getId()).orElseThrow().isStatus());

        assertTrue(vacancyDao.deleteById(savedVacancy.getId()));
        flushAndClear();
        assertTrue(vacancyDao.findById(savedVacancy.getId()).isEmpty());
    }

    @Test
    public void vacancyDaoShouldFindVacanciesByFilters() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("Максимед", "Медицина");

        Vacancy firstVacancy = persistVacancy(firstCompany, "Backend-разработчик", "240000.00", "Высшее техническое", true);
        persistVacancy(firstCompany, "Frontend-разработчик", "210000.00", "Высшее техническое", true);
        persistVacancy(secondCompany, "Терапевт", "110000.00", "Высшее медицинское", false);
        flushAndClear();

        assertEquals(vacancyDao.findByCompanyId(firstCompany.getId()).size(), 2);
        assertEquals(vacancyDao.findByPosition("Backend-разработчик").size(), 1);
        assertEquals(vacancyDao.findBySalaryBetween(new BigDecimal("200000.00"), new BigDecimal("250000.00")).size(), 2);
        assertEquals(vacancyDao.findByStatus(true).size(), 2);

        List<Vacancy> filteredVacancies = vacancyDao.searchVacancies(
                firstCompany.getId(),
                "конт",
                "Backend-разработчик",
                new BigDecimal("200000.00"),
                new BigDecimal("250000.00"),
                true
        );
        assertEquals(filteredVacancies.size(), 1);
        assertEquals(filteredVacancies.get(0).getId(), firstVacancy.getId());

        assertEquals(vacancyDao.searchVacancies(null, null, null, null, null, null).size(), 3);
    }

    @Test
    public void workExperienceDaoShouldSaveUpdateFindAndDeleteExperience() {
        Company company = persistCompany("1С", "Разработчик ПО");
        Person person = persistPersonWithoutExperience(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );

        WorkExperience workExperience = buildWorkExperience(
                person,
                company,
                "Разработчик 1С",
                "130000.00",
                LocalDate.of(2018, 7, 1),
                LocalDate.of(2021, 3, 31)
        );

        WorkExperience savedWorkExperience = workExperienceDao.save(workExperience);
        flushAndClear();

        assertNotNull(savedWorkExperience.getId());
        assertTrue(workExperienceDao.findById(savedWorkExperience.getId()).isPresent());

        WorkExperience workExperienceForUpdate = workExperienceDao.findById(savedWorkExperience.getId()).orElseThrow();
        workExperienceForUpdate.setSalary(new BigDecimal("140000.00"));
        workExperienceDao.update(workExperienceForUpdate);
        flushAndClear();

        assertEquals(
                workExperienceDao.findById(savedWorkExperience.getId()).orElseThrow().getSalary(),
                new BigDecimal("140000.00")
        );

        assertTrue(workExperienceDao.deleteById(savedWorkExperience.getId()));
        flushAndClear();
        assertTrue(workExperienceDao.findById(savedWorkExperience.getId()).isEmpty());
    }

    @Test
    public void workExperienceDaoShouldFindExperienceAndDetectOverlaps() {
        Company firstCompany = persistCompany("1С", "Разработчик ПО");
        Company secondCompany = persistCompany("ВКонтакте", "IT-компания");
        Person person = persistPersonWithoutExperience(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );

        WorkExperience firstExperience = workExperienceDao.save(
                buildWorkExperience(
                        person,
                        firstCompany,
                        "Разработчик 1С",
                        "130000.00",
                        LocalDate.of(2018, 7, 1),
                        LocalDate.of(2021, 3, 31)
                )
        );
        workExperienceDao.save(
                buildWorkExperience(
                        person,
                        secondCompany,
                        "Backend-разработчик",
                        "210000.00",
                        LocalDate.of(2021, 4, 1),
                        null
                )
        );
        flushAndClear();

        List<WorkExperience> personHistory = workExperienceDao.findByPersonIdOrderByStartDateDesc(person.getId());
        assertEquals(personHistory.size(), 2);
        assertEquals(personHistory.get(0).getCompany().getName(), "ВКонтакте");

        assertEquals(workExperienceDao.findByCompanyId(firstCompany.getId()).size(), 1);
        assertTrue(
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
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2020, 12, 31),
                        firstExperience.getId()
                )
        );
        assertFalse(
                workExperienceDao.existsOverlappingPeriod(
                        person.getId(),
                        LocalDate.of(2018, 1, 1),
                        LocalDate.of(2018, 6, 30),
                        null
                )
        );
    }

    @Test
    public void matchingDaoShouldFindSuitableVacanciesForPerson() {
        Company firstCompany = persistCompany("ВКонтакте", "IT-компания");
        Company secondCompany = persistCompany("Максимед", "Медицина");

        Vacancy matchingVacancy = persistVacancy(
                firstCompany,
                "Backend-разработчик",
                "240000.00",
                "Высшее техническое",
                true
        );
        persistVacancy(firstCompany, "Backend-разработчик", "220000.00", "Высшее техническое", true);
        persistVacancy(firstCompany, "Backend-разработчик", "260000.00", null, false);
        persistVacancy(secondCompany, "Терапевт", "110000.00", "Высшее медицинское", true);

        Person person = persistPersonWithoutExperience(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );
        flushAndClear();

        List<Vacancy> activeVacancies = matchingDao.findSuitableVacanciesForPerson(person.getId(), true, true);
        assertEquals(activeVacancies.size(), 1);
        assertEquals(activeVacancies.get(0).getId(), matchingVacancy.getId());
        assertEquals(activeVacancies.get(0).getCompany().getName(), "ВКонтакте");

        List<Vacancy> allMatchingVacancies = matchingDao.findSuitableVacanciesForPerson(person.getId(), false, true);
        assertEquals(allMatchingVacancies.size(), 2);
    }

    @Test
    public void matchingDaoShouldFindSuitablePersonsForVacancy() {
        Company company = persistCompany("ВКонтакте", "IT-компания");
        Vacancy vacancy = persistVacancy(
                company,
                "Backend-разработчик",
                "240000.00",
                "Высшее техническое",
                true
        );

        Person matchingPerson = persistPersonWithoutExperience(
                "Иванов Алексей Дмитриевич",
                "Высшее техническое",
                true,
                "Backend-разработчик",
                "230000.00"
        );
        persistPersonWithoutExperience(
                "Петров Пётр Сергеевич",
                "Высшее техническое",
                false,
                "Backend-разработчик",
                "220000.00"
        );
        persistPersonWithoutExperience(
                "Сидоров Сидор Иванович",
                "Высшее медицинское",
                true,
                "Backend-разработчик",
                "210000.00"
        );
        persistPersonWithoutExperience(
                "Козлов Алексей Игоревич",
                "Высшее техническое",
                true,
                "Java-разработчик",
                "200000.00"
        );
        flushAndClear();

        List<Person> onlyLookingPeople = matchingDao.findSuitablePersonsForVacancy(vacancy.getId(), true, true);
        assertEquals(onlyLookingPeople.size(), 1);
        assertEquals(onlyLookingPeople.get(0).getId(), matchingPerson.getId());

        List<Person> allMatchingPeople = matchingDao.findSuitablePersonsForVacancy(vacancy.getId(), false, true);
        assertEquals(allMatchingPeople.size(), 2);
    }

    private Company persistCompany(String name, String description) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        entityManager.persist(company);
        return company;
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

    private Person persistPersonWithoutExperience(
            String fullName,
            String education,
            boolean status,
            String desiredPosition,
            String desiredSalary
    ) {
        Person person = buildPerson(fullName, education, status, desiredPosition, desiredSalary);
        entityManager.persist(person);
        return person;
    }

    private Person buildPerson(
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
        return person;
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
