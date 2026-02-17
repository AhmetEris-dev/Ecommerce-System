package com.ahmete.authservice.refresh.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
		name = "refresh_tokens",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_refresh_tokens_token_hash", columnNames = "token_hash")
		},
		indexes = {
				@Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
				@Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
		}
)
public class RefreshToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	@Column(name = "token_hash", nullable = false, updatable = false, length = 64)
	private String tokenHash;
	
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;
	
	@Column(name = "revoked_at")
	private Instant revokedAt;
	
	@Column(name = "replaced_by_token_id")
	private Long replacedByTokenId;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(name = "created_by_ip")
	private String createdByIp;
	
	@Column(name = "user_agent", length = 512)
	private String userAgent;
	
	public RefreshToken() {
	}
	
	public boolean isExpired(Instant now) {
		return expiresAt.isBefore(now) || expiresAt.equals(now);
	}
	
	public boolean isRevoked() {
		return revokedAt != null;
	}
	
	// Getters / Setters
	
	public Long getId() {
		return id;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public String getTokenHash() {
		return tokenHash;
	}
	
	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}
	
	public Instant getExpiresAt() {
		return expiresAt;
	}
	
	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}
	
	public Instant getRevokedAt() {
		return revokedAt;
	}
	
	public void setRevokedAt(Instant revokedAt) {
		this.revokedAt = revokedAt;
	}
	
	public Long getReplacedByTokenId() {
		return replacedByTokenId;
	}
	
	public void setReplacedByTokenId(Long replacedByTokenId) {
		this.replacedByTokenId = replacedByTokenId;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	
	public String getCreatedByIp() {
		return createdByIp;
	}
	
	public void setCreatedByIp(String createdByIp) {
		this.createdByIp = createdByIp;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}