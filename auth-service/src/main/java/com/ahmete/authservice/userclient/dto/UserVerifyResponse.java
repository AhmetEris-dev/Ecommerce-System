package com.ahmete.authservice.userclient.dto;

public record UserVerifyResponse(
		Long userId,
		String role,
		Long companyId,
		String status
) {
}