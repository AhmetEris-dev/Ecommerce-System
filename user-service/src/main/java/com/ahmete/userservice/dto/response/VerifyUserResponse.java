package com.ahmete.userservice.dto.response;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;

public record VerifyUserResponse(
		Long userId,
		Role role,
		Long companyId,
		UserStatus status
) {}