package com.ahmete.orderservice.security;

import com.ahmete.orderservice.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	
	private SecurityUtils() {}
	
	public static Long currentUserId() {
		ApiPrincipal p = principal();
		return p.userId();
	}
	
	public static String currentRole() {
		ApiPrincipal p = principal();
		return p.role();
	}
	
	public static Long currentCompanyId() {
		ApiPrincipal p = principal();
		return p.companyId();
	}
	
	public static boolean isAdmin() { return "ADMIN".equalsIgnoreCase(currentRole()); }
	
	public static boolean isSeller() { return "SELLER".equalsIgnoreCase(currentRole()); }
	
	public static boolean isBuyer() { return "BUYER".equalsIgnoreCase(currentRole()); }
	
	private static ApiPrincipal principal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
			throw new UnauthorizedException("Unauthorized");
		}
		if (!(auth.getPrincipal() instanceof ApiPrincipal p)) {
			throw new UnauthorizedException("Unauthorized");
		}
		return p;
	}
}