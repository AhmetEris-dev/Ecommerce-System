package com.ahmete.userservice.dto.response;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;

import java.time.Instant;

public record UserResponse(
		Long id,
		String email,
		String firstName,
		String lastName,
		Role role,
		UserStatus status,
		Long companyId,
		Instant createdAt,
		Instant updatedAt
) {}