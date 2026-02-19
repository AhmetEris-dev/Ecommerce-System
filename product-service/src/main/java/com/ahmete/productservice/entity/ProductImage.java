package com.ahmete.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(
		name = "product_images",
		indexes = {
				@Index(name = "idx_product_images_product_id", columnList = "product_id")
		}
)
public class ProductImage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_images_product"))
	private Product product;
	
	@NotBlank
	@Column(nullable = false, length = 2048)
	private String url;
	
	@Column(nullable = false)
	private Integer sortOrder = 0;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
		if (this.sortOrder == null) this.sortOrder = 0;
	}
	
	public Long getId() {
		return id;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public void setProduct(Product product) {
		this.product = product;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Integer getSortOrder() {
		return sortOrder;
	}
	
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
}