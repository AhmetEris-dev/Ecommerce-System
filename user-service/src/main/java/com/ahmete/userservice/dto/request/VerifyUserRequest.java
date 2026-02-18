package com.ahmete.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyUserRequest(
		@Email @NotBlank String email,
		@NotBlank String password
) {}