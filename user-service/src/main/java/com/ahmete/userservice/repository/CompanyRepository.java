package com.ahmete.userservice.repository;

import com.ahmete.userservice.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
	
	boolean existsByNameIgnoreCase(String name);
	
	Optional<Company> findByNameIgnoreCase(String name);
	
	Optional<Company> findByTaxNumber(String taxNumber);
	
	boolean existsByTaxNumber(String taxNumber);
}