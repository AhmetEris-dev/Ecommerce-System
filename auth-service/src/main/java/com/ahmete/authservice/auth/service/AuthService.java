package com.ahmete.authservice.auth.service;

import com.ahmete.authservice.auth.dto.TokenResponse;
import com.ahmete.authservice.common.exception.ApiException;
import com.ahmete.authservice.jwt.service.JwtService;
import com.ahmete.authservice.refresh.entity.RefreshToken;
import com.ahmete.authservice.refresh.repository.RefreshTokenRepository;
import com.ahmete.authservice.refresh.service.RefreshTokenService;
import com.ahmete.authservice.userclient.UserServiceClient;
import com.ahmete.authservice.userclient.dto.UserVerifyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
	
	private final UserServiceClient userServiceClient;
	private final JwtService jwtService;
	
	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenService refreshTokenService;
	
	public AuthService(
			UserServiceClient userServiceClient,
			JwtService jwtService,
			RefreshTokenRepository refreshTokenRepository,
			RefreshTokenService refreshTokenService
	) {
		this.userServiceClient = userServiceClient;
		this.jwtService = jwtService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenService = refreshTokenService;
	}
	
	@Transactional
	public TokenResponse login(String email, String password, String ip, String userAgent) {
		UserVerifyResponse verified = userServiceClient.verify(email, password);
		
		if (verified.userId() == null || verified.role() == null) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Invalid verify response from user-service");
		}
		if ("PASSIVE".equalsIgnoreCase(verified.status())) {
			// user-service already should return 403, but keep defensive.
			throw new ApiException(HttpStatus.FORBIDDEN, "User status is PASSIVE");
		}
		
		var access = jwtService.createAccessToken(verified.userId(), verified.role(), verified.companyId());
		var refresh = refreshTokenService.createAndPersist(verified.userId(), verified.role(), verified.companyId(), ip, userAgent);
		
		return new TokenResponse(
				access.token(),
				refresh.rawToken(),
				"Bearer",
				access.expiresAt()
		);
	}
	
	@Transactional
	public TokenResponse refresh(String rawRefreshToken, String ip, String userAgent) {
		Instant now = Instant.now();
		
		String hash = refreshTokenService.hashRefreshToken(rawRefreshToken);
		RefreshToken current = refreshTokenRepository.findByTokenHash(hash)
		                                             .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
		
		if (current.isExpired(now)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
		}
		
		// REUSE DETECTION:
		// If token is already revoked, assume replay -> revoke all active tokens for the user.
		if (current.isRevoked()) {
			refreshTokenService.revokeAllActiveByUserId(current.getUserId(), now);
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token reuse detected. All sessions revoked.");
		}
		
		// ROTATION:
		// 1) revoke current token
		current.setRevokedAt(now);
		
		// ensure we have claims to mint a new access token
		String role = current.getRole();
		Long companyId = current.getCompanyId();
		if (role == null) {
			// Backward-compat for old tokens without embedded claims: force re-login.
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token missing claims. Please login again.");
		}
		
		// 2) create new token row (rotation) with same claims
		var created = refreshTokenService.createAndPersist(current.getUserId(), role, companyId, ip, userAgent);
		
		// 3) link old -> new
		current.setReplacedByTokenId(created.entity().getId());
		
		refreshTokenRepository.save(current);
		
		// mint new access token based on stored claims
		var access = jwtService.createAccessToken(current.getUserId(), role, companyId);
		
		return new TokenResponse(
				access.token(),
				created.rawToken(),
				"Bearer",
				access.expiresAt()
		);
	}
	
	@Transactional
	public void logout(String rawRefreshToken) {
		Instant now = Instant.now();
		String hash = refreshTokenService.hashRefreshToken(rawRefreshToken);
		
		refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
			if (token.getRevokedAt() == null) {
				token.setRevokedAt(now);
				refreshTokenRepository.save(token);
			}
		});
	}
}