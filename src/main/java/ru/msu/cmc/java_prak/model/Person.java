package ru.msu.cmc.java_prak.model;

import java.math.BigDecimal;

public class Person {
    private Long id;
    private String fullName;
    private String homeAddress;
    private String education;
    private Boolean status;
    private String desiredPosition;
    private BigDecimal desiredSalary;
    //не хватает WorkExperience, наверно хотелось бы хранить прям тут ссылки на опыт работы

    public Person() {}

    public Person(Long id, String fullName, String homeAddress, String education,
                  Boolean status, String desiredPosition, BigDecimal desiredSalary) {
        this.id = id;
        this.fullName = fullName;
        this.homeAddress = homeAddress;
        this.education = education;
        this.status = status;
        this.desiredPosition = desiredPosition;
        this.desiredSalary = desiredSalary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDesiredPosition() {
        return desiredPosition;
    }

    public void setDesiredPosition(String desiredPosition) {
        this.desiredPosition = desiredPosition;
    }

    public BigDecimal getDesiredSalary() {
        return desiredSalary;
    }

    public void setDesiredSalary(BigDecimal desiredSalary) {
        this.desiredSalary = desiredSalary;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", homeAddress='" + homeAddress + '\'' +
                ", education='" + education + '\'' +
                ", status=" + status +
                ", desiredPosition='" + desiredPosition + '\'' +
                ", desiredSalary=" + desiredSalary +
                '}';
    }
}