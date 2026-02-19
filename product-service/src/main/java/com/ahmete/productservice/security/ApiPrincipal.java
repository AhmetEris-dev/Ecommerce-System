package com.ahmete.productservice.security;

public record ApiPrincipal(
		Long userId,
		String role,
		Long companyId
) {}