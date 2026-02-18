package com.ahmete.userservice.service;

import com.ahmete.userservice.domain.CompanyStatus;
import com.ahmete.userservice.dto.request.CreateCompanyRequest;
import com.ahmete.userservice.dto.response.CompanyResponse;
import com.ahmete.userservice.entity.Company;
import com.ahmete.userservice.exception.BadRequestException;
import com.ahmete.userservice.exception.DuplicateException;
import com.ahmete.userservice.exception.NotFoundException;
import com.ahmete.userservice.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {
	
	private final CompanyRepository companyRepository;
	
	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
	@Transactional
	public CompanyResponse create(CreateCompanyRequest req) {
		String name = req.name().trim();
		if (companyRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateException("Company name already exists");
		}
		
		String tax = req.taxNumber();
		if (tax != null && !tax.isBlank()) {
			String taxTrim = tax.trim();
			if (companyRepository.existsByTaxNumber(taxTrim)) {
				throw new DuplicateException("Company taxNumber already exists");
			}
		}
		
		Company c = new Company();
		c.setName(name);
		if (tax != null && !tax.isBlank()) c.setTaxNumber(tax.trim());
		c.setStatus(CompanyStatus.ACTIVE);
		
		Company saved = companyRepository.save(c);
		return toResponse(saved);
	}
	
	@Transactional(readOnly = true)
	public Company getEntity(Long id) {
		return companyRepository.findById(id).orElseThrow(() -> new NotFoundException("Company not found"));
	}
	
	@Transactional(readOnly = true)
	public CompanyResponse get(Long id) {
		return toResponse(getEntity(id));
	}
	
	@Transactional(readOnly = true)
	public List<CompanyResponse> list() {
		return companyRepository.findAll().stream().map(this::toResponse).toList();
	}
	
	@Transactional
	public Company createIfNeeded(String companyName, String taxNumber) {
		if (companyName == null || companyName.isBlank()) {
			throw new BadRequestException("companyName is required when companyId is not provided");
		}
		
		String name = companyName.trim();
		if (companyRepository.existsByNameIgnoreCase(name)) {
			// If you want strict uniqueness -> duplicate
			throw new DuplicateException("Company name already exists");
		}
		
		if (taxNumber != null && !taxNumber.isBlank()) {
			String taxTrim = taxNumber.trim();
			if (companyRepository.existsByTaxNumber(taxTrim)) {
				throw new DuplicateException("Company taxNumber already exists");
			}
		}
		
		Company c = new Company();
		c.setName(name);
		if (taxNumber != null && !taxNumber.isBlank()) c.setTaxNumber(taxNumber.trim());
		
		return companyRepository.save(c);
	}
	
	private CompanyResponse toResponse(Company c) {
		return new CompanyResponse(
				c.getId(),
				c.getName(),
				c.getTaxNumber(),
				c.getStatus(),
				c.getCreatedAt(),
				c.getUpdatedAt()
		);
	}
}