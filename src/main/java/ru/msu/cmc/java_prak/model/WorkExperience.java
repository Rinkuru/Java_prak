package ru.msu.cmc.java_prak.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WorkExperience {
    private Long id;
    private Person person;
    private Company company;
    private String position;
    private BigDecimal salary;
    private LocalDate startDate;
    private LocalDate endDate;

    public WorkExperience() {
    }

    public WorkExperience(Long id, Person person, Company company, String position,
                          BigDecimal salary, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.person = person;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "WorkExperience{" +
                "id=" + id +
                ", person=" + person +
                ", company=" + company +
                ", position='" + position + '\'' +
                ", salary=" + salary +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}