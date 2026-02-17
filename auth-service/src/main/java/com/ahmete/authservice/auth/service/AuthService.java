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
		var refresh = refreshTokenService.createAndPersist(verified.userId(), ip, userAgent);
		
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
		
		// 2) create new token row
		var created = refreshTokenService.createAndPersist(current.getUserId(), ip, userAgent);
		
		// 3) link old -> new
		current.setReplacedByTokenId(created.entity().getId());
		
		refreshTokenRepository.save(current);
		
		// Access token claims come from user-service originally, but refresh endpoint does NOT call user-service.
		// We must embed role/companyId in refresh table to restore claims OR keep it minimal.
		// Your requirement says access token must contain role & companyId.
		// So we MUST fetch them. Pragmatic choice: call user-service to read current role/companyId/status.
		// BUT you did not provide an endpoint. Therefore:
		// We'll keep role/companyId in JWT only at login/refresh by requiring client to re-login if claims changed.
		// To satisfy your claim requirement NOW, we store them as part of refresh flow is impossible without data.
		// So: we will encode role/companyId from the *latest access token*? Not available here.
		//
		// Solution that DOES satisfy requirements without new endpoints:
		// - Store role/companyId in refresh_tokens table (extra columns).
		// You explicitly said "title/fields exactly", no extras. So we won't.
		//
		// Therefore we do the only correct thing given constraints:
		// throw 500 if role/companyId cannot be determined.
		//
		// ----
		// If you want refresh to mint access tokens with role/companyId, you MUST either:
		// (A) add role/companyId to RefreshToken entity (recommended), OR
		// (B) add user-service internal endpoint to fetch auth claims by userId.
		//
		// I'm not going to silently violate your constraints by adding columns.
		throw new ApiException(HttpStatus.NOT_IMPLEMENTED,
		                       "Refresh cannot mint access token with role/companyId without an internal user-service claims endpoint or storing claims in refresh_tokens");
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