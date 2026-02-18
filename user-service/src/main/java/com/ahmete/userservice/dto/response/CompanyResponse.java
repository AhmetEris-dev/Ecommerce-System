package com.ahmete.userservice.dto.response;

import com.ahmete.userservice.domain.CompanyStatus;

import java.time.Instant;

public record CompanyResponse(
		Long id,
		String name,
		String taxNumber,
		CompanyStatus status,
		Instant createdAt,
		Instant updatedAt
) {}