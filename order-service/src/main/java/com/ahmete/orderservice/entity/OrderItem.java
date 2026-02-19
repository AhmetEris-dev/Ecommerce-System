package com.ahmete.orderservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items",
		indexes = {
				@Index(name = "idx_order_items_company_id", columnList = "companyId"),
				@Index(name = "idx_order_items_order_id", columnList = "order_id")
		})
public class OrderItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
	
	@Column(nullable = false)
	private Long productId;
	
	@Column(nullable = false)
	private String productName;
	
	@Column(nullable = false)
	private Long companyId;
	
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal unitPrice;
	
	@Column(nullable = false)
	private Integer quantity;
	
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal lineTotal;
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
	
	// getters/setters
	
	public Long getId() { return id; }
	
	public Order getOrder() { return order; }
	
	public void setOrder(Order order) { this.order = order; }
	
	public Long getProductId() { return productId; }
	
	public void setProductId(Long productId) { this.productId = productId; }
	
	public String getProductName() { return productName; }
	
	public void setProductName(String productName) { this.productName = productName; }
	
	public Long getCompanyId() { return companyId; }
	
	public void setCompanyId(Long companyId) { this.companyId = companyId; }
	
	public BigDecimal getUnitPrice() { return unitPrice; }
	
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
	
	public Integer getQuantity() { return quantity; }
	
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	
	public BigDecimal getLineTotal() { return lineTotal; }
	
	public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
	
	public Instant getCreatedAt() { return createdAt; }
}