package com.ahmete.userservice.security;

import com.ahmete.userservice.domain.Role;

public record ApiPrincipal(
		Long userId,
		Role role,
		Long companyId
) {}