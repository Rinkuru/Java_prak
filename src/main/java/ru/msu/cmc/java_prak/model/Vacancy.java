package ru.msu.cmc.java_prak.model;

import java.math.BigDecimal;

public class Vacancy {
    private Long id;
    private Company company;
    private String position;
    private BigDecimal salary;
    private String requiredEducation;
    private String requirements;
    private Boolean status;

    public Vacancy() {
    }

    public Vacancy(Long id, Company company, String position, BigDecimal salary,
                   String requiredEducation, String requirements, Boolean status) {
        this.id = id;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.requiredEducation = requiredEducation;
        this.requirements = requirements;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRequiredEducation() {
        return requiredEducation;
    }

    public void setRequiredEducation(String requiredEducation) {
        this.requiredEducation = requiredEducation;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Vacancy{" +
                "id=" + id +
                ", company=" + company +
                ", position='" + position + '\'' +
                ", salary=" + salary +
                ", requiredEducation='" + requiredEducation + '\'' +
                ", requirements='" + requirements + '\'' +
                ", status=" + status +
                '}';
    }
}