package com.ahmete.userservice.controller;

import com.ahmete.userservice.constants.RestApis;
import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.dto.request.CreateCompanyRequest;
import com.ahmete.userservice.dto.response.CompanyResponse;
import com.ahmete.userservice.exception.ForbiddenException;
import com.ahmete.userservice.security.SecurityUtils;
import com.ahmete.userservice.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RestApis.Companies.ROOT)
public class CompanyController {
	
	private final CompanyService companyService;
	
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CompanyResponse create(@Valid @RequestBody CreateCompanyRequest request) {
		if (SecurityUtils.currentRole() != Role.ADMIN) {
			throw new ForbiddenException("ADMIN only");
		}
		return companyService.create(request);
	}
	
	@GetMapping(RestApis.Companies.ID)
	public CompanyResponse get(@PathVariable Long id) {
		Role role = SecurityUtils.currentRole();
		if (role == Role.ADMIN) {
			return companyService.get(id);
		}
		
		if (role == Role.SELLER) {
			Long tokenCompanyId = SecurityUtils.currentCompanyId();
			if (tokenCompanyId == null || !tokenCompanyId.equals(id)) {
				throw new ForbiddenException("Not allowed");
			}
			return companyService.get(id);
		}
		
		throw new ForbiddenException("Not allowed");
	}
	
	@GetMapping
	public List<CompanyResponse> list() {
		if (SecurityUtils.currentRole() != Role.ADMIN) {
			throw new ForbiddenException("ADMIN only");
		}
		return companyService.list();
	}
}