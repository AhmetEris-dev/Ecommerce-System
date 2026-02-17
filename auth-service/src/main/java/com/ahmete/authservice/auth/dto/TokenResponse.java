package com.ahmete.authservice.auth.dto;

import java.time.Instant;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		Instant expiresAt
) {
}