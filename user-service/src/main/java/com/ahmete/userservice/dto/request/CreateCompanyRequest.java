package com.ahmete.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
		@NotBlank String name,
		String taxNumber
) {}