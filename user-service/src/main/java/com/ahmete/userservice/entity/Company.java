package com.ahmete.userservice.entity;

import com.ahmete.userservice.domain.CompanyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(
		name = "companies",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_companies_name", columnNames = "name"),
				@UniqueConstraint(name = "uk_companies_tax_number", columnNames = "tax_number")
		}
)
public class Company {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	@Column(nullable = false, length = 200)
	private String name;
	
	@Column(name = "tax_number", unique = true, length = 64)
	private String taxNumber;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private CompanyStatus status = CompanyStatus.ACTIVE;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(nullable = false)
	private Instant updatedAt;
	
	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) this.status = CompanyStatus.ACTIVE;
	}
	
	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}
	
	public Long getId() { return id; }
	public String getName() { return name; }
	public String getTaxNumber() { return taxNumber; }
	public CompanyStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	
	public void setName(String name) { this.name = name; }
	public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
	public void setStatus(CompanyStatus status) { this.status = status; }
}