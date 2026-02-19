package com.ahmete.orderservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long buyerId;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status = OrderStatus.NEW;
	
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalAmount;
	
	@Column(nullable = false)
	private String currency = "TRY";
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@Column(nullable = false)
	private Instant updatedAt;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();
	
	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) this.status = OrderStatus.NEW;
		if (this.currency == null) this.currency = "TRY";
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
	
	public void addItem(OrderItem item) {
		item.setOrder(this);
		this.items.add(item);
	}
	
	// getters/setters
	
	public Long getId() { return id; }
	
	public Long getBuyerId() { return buyerId; }
	
	public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
	
	public OrderStatus getStatus() { return status; }
	
	public void setStatus(OrderStatus status) { this.status = status; }
	
	public BigDecimal getTotalAmount() { return totalAmount; }
	
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	
	public String getCurrency() { return currency; }
	
	public void setCurrency(String currency) { this.currency = currency; }
	
	public Instant getCreatedAt() { return createdAt; }
	
	public Instant getUpdatedAt() { return updatedAt; }
	
	public List<OrderItem> getItems() { return items; }
	
	public void setItems(List<OrderItem> items) { this.items = items; }
}