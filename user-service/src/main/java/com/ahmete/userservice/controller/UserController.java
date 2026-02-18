package com.ahmete.userservice.controller;

import com.ahmete.userservice.constants.RestApis;
import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;
import com.ahmete.userservice.dto.request.RegisterBuyerRequest;
import com.ahmete.userservice.dto.request.RegisterSellerRequest;
import com.ahmete.userservice.dto.request.UpdateUserStatusRequest;
import com.ahmete.userservice.dto.response.UserResponse;
import com.ahmete.userservice.exception.ForbiddenException;
import com.ahmete.userservice.security.SecurityUtils;
import com.ahmete.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RestApis.Users.ROOT)
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping(RestApis.Users.REGISTER)
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
		return userService.registerBuyer(request);
	}
	
	@PostMapping(RestApis.Users.REGISTER_SELLER)
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse registerSeller(@Valid @RequestBody RegisterSellerRequest request) {
		return userService.registerSeller(request);
	}
	
	@GetMapping(RestApis.Users.ME)
	public UserResponse me() {
		Long userId = SecurityUtils.currentUserId();
		return userService.getMe(userId);
	}
	
	@GetMapping(RestApis.Users.ID)
	public UserResponse getById(@PathVariable Long id) {
		Role role = SecurityUtils.currentRole();
		Long currentUserId = SecurityUtils.currentUserId();
		
		if (role != Role.ADMIN && !id.equals(currentUserId)) {
			throw new ForbiddenException("Not allowed");
		}
		
		return userService.getById(id);
	}
	
	@GetMapping
	public List<UserResponse> list(
			@RequestParam(required = false) Role role,
			@RequestParam(required = false) UserStatus status,
			@RequestParam(required = false) Long companyId,
			@RequestParam(required = false) String q
	) {
		if (SecurityUtils.currentRole() != Role.ADMIN) {
			throw new ForbiddenException("ADMIN only");
		}
		return userService.list(role, status, companyId, q);
	}
	
	@PatchMapping(RestApis.Users.STATUS)
	public UserResponse updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateUserStatusRequest request
	) {
		if (SecurityUtils.currentRole() != Role.ADMIN) {
			throw new ForbiddenException("ADMIN only");
		}
		return userService.updateStatus(id, request);
	}
}