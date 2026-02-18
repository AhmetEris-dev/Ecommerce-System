package com.ahmete.userservice.security;

import com.ahmete.userservice.domain.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	
	private SecurityUtils() {}
	
	public static Long currentUserId() {
		return principal().userId();
	}
	
	public static Role currentRole() {
		return principal().role();
	}
	
	public static Long currentCompanyId() {
		return principal().companyId();
	}
	
	private static ApiPrincipal principal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof ApiPrincipal p)) {
			throw new IllegalStateException("No authenticated principal");
		}
		return p;
	}
}