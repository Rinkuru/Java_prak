package ru.msu.cmc.java_prak.dao;

import java.util.List;
import java.util.Optional;
import ru.msu.cmc.java_prak.model.Company;

/**
 * DAO для работы с компаниями и их списками в разделе работодателей.
 */
public interface CompanyDao {

    List<Company> findAllOrderByName();

    Optional<Company> findById(Long id);

    Optional<Company> findCardById(Long id);

    Company save(Company company);

    Company update(Company company);

    boolean deleteById(Long id);

    List<Company> findByNameContaining(String namePart);

    List<Company> findWithOpenVacancies();

    List<Company> searchCompanies(String namePart, Boolean onlyWithOpenVacancies);
}
