package com.ahmete.authservice.jwt.service;

import com.ahmete.authservice.jwt.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
	
	private final JwtProperties props;
	private final SecretKey key;
	
	public JwtService(JwtProperties props) {
		this.props = props;
		
		byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
		// HS256 requires >= 256-bit key (32 bytes). Fail fast.
		if (secretBytes.length < 32) {
			throw new IllegalStateException("jwt.secret must be at least 32 bytes for HS256");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
	}
	
	public AccessTokenResult createAccessToken(Long userId, String role, Long companyId) {
		Instant now = Instant.now();
		Instant exp = now.plus(props.accessTokenMinutes(), ChronoUnit.MINUTES);
		
		var builder = Jwts.builder()
		                  .issuer(props.issuer())
		                  .subject(String.valueOf(userId))
		                  .issuedAt(Date.from(now))
		                  .expiration(Date.from(exp))
		                  .claim("role", role);
		
		if (companyId != null) {
			builder.claim("companyId", companyId);
		}
		
		String jwt = builder
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
		
		return new AccessTokenResult(jwt, exp);
	}
	
	public record AccessTokenResult(String token, Instant expiresAt) {}
}