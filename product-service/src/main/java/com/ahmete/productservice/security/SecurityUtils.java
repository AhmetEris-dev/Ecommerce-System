package com.ahmete.productservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	
	private SecurityUtils() {}
	
	public static Long currentUserId() {
		ApiPrincipal p = principalOrNull();
		return p == null ? null : p.userId();
	}
	
	public static String currentRole() {
		ApiPrincipal p = principalOrNull();
		return p == null ? null : p.role();
	}
	
	public static Long currentCompanyId() {
		ApiPrincipal p = principalOrNull();
		return p == null ? null : p.companyId();
	}
	
	public static boolean isAdmin() {
		return "ADMIN".equalsIgnoreCase(currentRole());
	}
	
	public static boolean isSeller() {
		return "SELLER".equalsIgnoreCase(currentRole());
	}
	
	public static boolean isBuyer() {
		return "BUYER".equalsIgnoreCase(currentRole());
	}
	
	public static boolean isAuthenticated() {
		return principalOrNull() != null;
	}
	
	private static ApiPrincipal principalOrNull() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) return null;
		Object principal = auth.getPrincipal();
		if (principal instanceof ApiPrincipal p) return p;
		return null;
	}
}