package com.ahmete.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
		name = "products",
		indexes = {
				@Index(name = "idx_products_company_id", columnList = "company_id"),
				@Index(name = "idx_products_status", columnList = "status"),
				@Index(name = "idx_products_name", columnList = "name")
		},
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
		}
)
public class Product {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Column(name = "company_id", nullable = false)
	private Long companyId;
	
	@NotBlank
	@Column(nullable = false, length = 200)
	private String name;
	
	@Column(length = 2000)
	private String description;
	
	@NotBlank
	@Column(nullable = false, length = 64, unique = true)
	private String sku;
	
	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal price;
	
	@NotNull
	@Min(0)
	@Column(nullable = false)
	private Integer stock;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private ProductStatus status = ProductStatus.ACTIVE;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(nullable = false)
	private Instant updatedAt;
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("sortOrder ASC, id ASC")
	private List<ProductImage> images = new ArrayList<>();
	
	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) this.status = ProductStatus.ACTIVE;
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
	
	public Long getId() {
		return id;
	}
	
	public Long getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getSku() {
		return sku;
	}
	
	public void setSku(String sku) {
		this.sku = sku;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public Integer getStock() {
		return stock;
	}
	
	public void setStock(Integer stock) {
		this.stock = stock;
	}
	
	public ProductStatus getStatus() {
		return status;
	}
	
	public void setStatus(ProductStatus status) {
		this.status = status;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Instant getUpdatedAt() {
		return updatedAt;
	}
	
	public List<ProductImage> getImages() {
		return images;
	}
	
	public void addImage(ProductImage image) {
		images.add(image);
		image.setProduct(this);
	}
	
	public void removeImage(ProductImage image) {
		images.remove(image);
		image.setProduct(null);
	}
}