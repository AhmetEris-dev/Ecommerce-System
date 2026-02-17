package com.ahmete.authservice.refresh.service;

import com.ahmete.authservice.jwt.config.JwtProperties;
import com.ahmete.authservice.refresh.entity.RefreshToken;
import com.ahmete.authservice.refresh.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {
	
	private final RefreshTokenRepository repo;
	private final JwtProperties jwtProperties;
	
	private final SecureRandom secureRandom = new SecureRandom();
	
	public RefreshTokenService(RefreshTokenRepository repo, JwtProperties jwtProperties) {
		this.repo = repo;
		this.jwtProperties = jwtProperties;
	}
	
	public record CreatedRefreshToken(String rawToken, RefreshToken entity) {}
	
	public String hashRefreshToken(String rawToken) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return toHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
	
	public String generateRawRefreshToken() {
		// >= 256 bits
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
	
	@Transactional
	public CreatedRefreshToken createAndPersist(Long userId, String createdByIp, String userAgent) {
		Instant now = Instant.now();
		Instant exp = now.plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS);
		
		String raw = generateRawRefreshToken();
		String hash = hashRefreshToken(raw);
		
		RefreshToken rt = new RefreshToken();
		rt.setUserId(userId);
		rt.setTokenHash(hash);
		rt.setExpiresAt(exp);
		rt.setCreatedAt(now);
		rt.setCreatedByIp(createdByIp);
		rt.setUserAgent(userAgent);
		
		RefreshToken saved = repo.save(rt);
		return new CreatedRefreshToken(raw, saved);
	}
	
	@Transactional
	public void revoke(RefreshToken token, Instant now) {
		if (token.getRevokedAt() == null) {
			token.setRevokedAt(now);
			repo.save(token);
		}
	}
	
	@Transactional
	public int revokeAllActiveByUserId(Long userId, Instant now) {
		return repo.revokeAllActiveByUserId(userId, now);
	}
	
	private static String toHex(byte[] bytes) {
		char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}