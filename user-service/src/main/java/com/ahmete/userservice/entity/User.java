package com.ahmete.userservice.entity;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(
		name = "users",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_users_email", columnNames = "email")
		},
		indexes = {
				@Index(name = "idx_users_email", columnList = "email"),
				@Index(name = "idx_users_role", columnList = "role"),
				@Index(name = "idx_users_status", columnList = "status"),
				@Index(name = "idx_users_company_id", columnList = "company_id")
		}
)
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Email
	@NotBlank
	@Column(nullable = false, length = 255)
	private String email;
	
	@Column(nullable = false, length = 100)
	private String passwordHash;
	
	@NotBlank
	@Column(nullable = false, length = 120)
	private String firstName;
	
	@NotBlank
	@Column(nullable = false, length = 120)
	private String lastName;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private Role role;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private UserStatus status = UserStatus.ACTIVE;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(nullable = false)
	private Instant updatedAt;
	
	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) this.status = UserStatus.ACTIVE;
	}
	
	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}
	
	public Long getId() { return id; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public Role getRole() { return role; }
	public UserStatus getStatus() { return status; }
	public Company getCompany() { return company; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	
	public void setEmail(String email) { this.email = email; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public void setRole(Role role) { this.role = role; }
	public void setStatus(UserStatus status) { this.status = status; }
	public void setCompany(Company company) { this.company = company; }
}