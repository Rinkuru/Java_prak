package ru.msu.cmc.java_prak.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@DataJpaTest
public class EntityMappingTests extends AbstractTestNGSpringContextTests {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void shouldPersistCompanyWithVacancy() {
        Company company = new Company();
        company.setName("ВКонтакте");
        company.setDescription("Крупная IT-компания");

        Vacancy vacancy = new Vacancy();
        vacancy.setPosition("Backend-разработчик");
        vacancy.setSalary(new BigDecimal("240000.00"));
        vacancy.setRequiredEducation("Высшее техническое");
        vacancy.setRequirements("Опыт коммерческой backend-разработки");
        vacancy.setStatus(true);

        company.addVacancy(vacancy);

        entityManager.persistAndFlush(company);
        Long vacancyId = vacancy.getId();
        entityManager.clear();

        Vacancy savedVacancy = entityManager.getEntityManager()
                .createQuery(
                        "select v from Vacancy v join fetch v.company where v.id = :id",
                        Vacancy.class
                )
                .setParameter("id", vacancyId)
                .getSingleResult();

        assertNotNull(savedVacancy.getId());
        assertEquals(savedVacancy.getPosition(), "Backend-разработчик");
        assertEquals(savedVacancy.getSalary(), new BigDecimal("240000.00"));
        assertEquals(savedVacancy.getCompany().getName(), "ВКонтакте");
        assertTrue(savedVacancy.isStatus());
    }

    @Test
    public void shouldPersistPersonWithOrderedWorkExperience() {
        Company company = new Company();
        company.setName("1С");
        company.setDescription("Разработчик программного обеспечения");
        entityManager.persist(company);

        Person person = new Person();
        person.setFullName("Иванов Алексей Дмитриевич");
        person.setHomeAddress("Москва, Университетский проспект, д. 5, кв. 12");
        person.setEducation("Высшее техническое");
        person.setStatus(true);
        person.setDesiredPosition("Backend-разработчик");
        person.setDesiredSalary(new BigDecimal("230000.00"));

        WorkExperience olderExperience = new WorkExperience();
        olderExperience.setCompany(company);
        olderExperience.setPosition("Разработчик 1С");
        olderExperience.setSalary(new BigDecimal("130000.00"));
        olderExperience.setStartDate(LocalDate.of(2018, 7, 1));
        olderExperience.setEndDate(LocalDate.of(2021, 3, 31));

        WorkExperience newerExperience = new WorkExperience();
        newerExperience.setCompany(company);
        newerExperience.setPosition("Backend-разработчик");
        newerExperience.setSalary(new BigDecimal("210000.00"));
        newerExperience.setStartDate(LocalDate.of(2021, 4, 1));
        newerExperience.setEndDate(null);

        person.addWorkExperience(olderExperience);
        person.addWorkExperience(newerExperience);

        entityManager.persistAndFlush(person);
        Long personId = person.getId();
        entityManager.clear();

        Person savedPerson = entityManager.getEntityManager()
                .createQuery(
                        """
                        select distinct p
                        from Person p
                        left join fetch p.workExperiences we
                        left join fetch we.company
                        where p.id = :id
                        """,
                        Person.class
                )
                .setParameter("id", personId)
                .getSingleResult();

        assertNotNull(savedPerson.getId());
        assertEquals(savedPerson.getWorkExperiences().size(), 2);
        assertEquals(savedPerson.getWorkExperiences().get(0).getPosition(), "Backend-разработчик");
        assertEquals(savedPerson.getWorkExperiences().get(0).getStartDate(), LocalDate.of(2021, 4, 1));
        assertEquals(savedPerson.getWorkExperiences().get(1).getStartDate(), LocalDate.of(2018, 7, 1));
        assertEquals(savedPerson.getWorkExperiences().get(0).getCompany().getName(), "1С");
    }
}
