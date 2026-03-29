package ru.msu.cmc.java_prak.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

// Тесты helper-методов сущностей, которые синхронизируют двусторонние связи.
public class EntityHelperTests {

    @Test
    public void personHelperMethodsShouldSynchronizeWorkExperienceRelation() {
        Person person = new Person();
        WorkExperience workExperience = new WorkExperience();

        person.addWorkExperience(workExperience);

        assertEquals(person.getWorkExperiences().size(), 1);
        assertTrue(person.getWorkExperiences().contains(workExperience));
        assertEquals(workExperience.getPerson(), person);

        person.removeWorkExperience(workExperience);

        assertTrue(person.getWorkExperiences().isEmpty());
        assertFalse(person.getWorkExperiences().contains(workExperience));
        assertNull(workExperience.getPerson());
    }

    @Test
    public void companyHelperMethodsShouldSynchronizeVacancyRelation() {
        Company company = new Company();
        Vacancy vacancy = new Vacancy();

        company.addVacancy(vacancy);

        assertEquals(company.getVacancies().size(), 1);
        assertTrue(company.getVacancies().contains(vacancy));
        assertEquals(vacancy.getCompany(), company);

        company.removeVacancy(vacancy);

        assertTrue(company.getVacancies().isEmpty());
        assertFalse(company.getVacancies().contains(vacancy));
        assertNull(vacancy.getCompany());
    }
}
