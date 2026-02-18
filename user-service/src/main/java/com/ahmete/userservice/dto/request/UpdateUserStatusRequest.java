package com.ahmete.userservice.dto.request;

import com.ahmete.userservice.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
		@NotNull UserStatus status
) {}