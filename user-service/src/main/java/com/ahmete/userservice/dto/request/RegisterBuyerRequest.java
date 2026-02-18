package com.ahmete.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterBuyerRequest(
		@Email @NotBlank String email,
		@NotBlank @Size(min = 6, max = 72) String password,
		@NotBlank String firstName,
		@NotBlank String lastName
) {}