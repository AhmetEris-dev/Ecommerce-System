package com.ahmete.orderservice.service.projection;

import com.ahmete.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public interface SellerOrderItemViewProjection {
	Long getOrderId();
	Long getBuyerId();
	OrderStatus getOrderStatus();
	
	Long getProductId();
	String getProductName();
	BigDecimal getUnitPrice();
	Integer getQuantity();
	BigDecimal getLineTotal();
	Instant getCreatedAt();
}