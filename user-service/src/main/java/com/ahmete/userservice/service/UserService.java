package com.ahmete.userservice.service;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;
import com.ahmete.userservice.dto.request.RegisterBuyerRequest;
import com.ahmete.userservice.dto.request.RegisterSellerRequest;
import com.ahmete.userservice.dto.request.UpdateUserStatusRequest;
import com.ahmete.userservice.dto.request.VerifyUserRequest;
import com.ahmete.userservice.dto.response.UserResponse;
import com.ahmete.userservice.dto.response.VerifyUserResponse;
import com.ahmete.userservice.entity.Company;
import com.ahmete.userservice.entity.User;
import com.ahmete.userservice.exception.BadRequestException;
import com.ahmete.userservice.exception.DuplicateException;
import com.ahmete.userservice.exception.ForbiddenException;
import com.ahmete.userservice.exception.NotFoundException;
import com.ahmete.userservice.exception.UnauthorizedException;
import com.ahmete.userservice.repository.UserRepository;
import com.ahmete.userservice.repository.spec.UserSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final CompanyService companyService;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, CompanyService companyService, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.companyService = companyService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public UserResponse registerBuyer(RegisterBuyerRequest req) {
		if (userRepository.existsByEmailIgnoreCase(req.email().trim())) {
			throw new DuplicateException("Email already exists");
		}
		
		User u = new User();
		u.setEmail(req.email().trim().toLowerCase());
		u.setPasswordHash(passwordEncoder.encode(req.password()));
		u.setFirstName(req.firstName().trim());
		u.setLastName(req.lastName().trim());
		u.setRole(Role.BUYER);
		u.setStatus(UserStatus.ACTIVE);
		u.setCompany(null);
		
		User saved = userRepository.save(u);
		return toResponse(saved);
	}
	
	@Transactional
	public UserResponse registerSeller(RegisterSellerRequest req) {
		if (userRepository.existsByEmailIgnoreCase(req.email().trim())) {
			throw new DuplicateException("Email already exists");
		}
		
		Company company;
		if (req.companyId() != null) {
			company = companyService.getEntity(req.companyId());
		} else {
			company = companyService.createIfNeeded(req.companyName(), req.taxNumber());
		}
		
		User u = new User();
		u.setEmail(req.email().trim().toLowerCase());
		u.setPasswordHash(passwordEncoder.encode(req.password()));
		u.setFirstName(req.firstName().trim());
		u.setLastName(req.lastName().trim());
		u.setRole(Role.SELLER);
		u.setStatus(UserStatus.ACTIVE);
		u.setCompany(company);
		
		User saved = userRepository.save(u);
		return toResponse(saved);
	}
	
	@Transactional(readOnly = true)
	public VerifyUserResponse verifyForAuth(VerifyUserRequest req) {
		User u = userRepository.findByEmailIgnoreCase(req.email().trim())
		                       .orElseThrow(() -> new NotFoundException("Email not found"));
		
		if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
			throw new UnauthorizedException("Wrong password");
		}
		
		if (u.getStatus() == UserStatus.PASSIVE) {
			throw new ForbiddenException("User is passive");
		}
		
		Long companyId = (u.getCompany() == null) ? null : u.getCompany().getId();
		return new VerifyUserResponse(u.getId(), u.getRole(), companyId, u.getStatus());
	}
	
	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		return toResponse(getEntity(id));
	}
	
	@Transactional(readOnly = true)
	public User getEntity(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
	}
	
	@Transactional(readOnly = true)
	public UserResponse getMe(Long userIdFromToken) {
		return toResponse(getEntity(userIdFromToken));
	}
	
	@Transactional(readOnly = true)
	public List<UserResponse> list(Role role, UserStatus status, Long companyId, String q) {
		Specification<User> spec = Specification
				.where(UserSpecifications.role(role))
				.and(UserSpecifications.status(status))
				.and(UserSpecifications.companyId(companyId))
				.and(UserSpecifications.q(q));
		
		return userRepository.findAll(spec).stream().map(this::toResponse).toList();
	}
	
	@Transactional
	public UserResponse updateStatus(Long id, UpdateUserStatusRequest req) {
		User u = getEntity(id);
		u.setStatus(req.status());
		return toResponse(userRepository.save(u));
	}
	
	public void validateCompanyRoleInvariant(Role role, Company company) {
		if (role == Role.SELLER) {
			if (company == null) throw new BadRequestException("SELLER must have a company");
		} else {
			if (company != null) throw new BadRequestException("BUYER/ADMIN must not have a company");
		}
	}
	
	private UserResponse toResponse(User u) {
		Long companyId = (u.getCompany() == null) ? null : u.getCompany().getId();
		return new UserResponse(
				u.getId(),
				u.getEmail(),
				u.getFirstName(),
				u.getLastName(),
				u.getRole(),
				u.getStatus(),
				companyId,
				u.getCreatedAt(),
				u.getUpdatedAt()
		);
	}
}