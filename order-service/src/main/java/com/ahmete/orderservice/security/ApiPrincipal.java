package com.ahmete.orderservice.security;

public record ApiPrincipal(
		Long userId,
		String role,
		Long companyId
) {}