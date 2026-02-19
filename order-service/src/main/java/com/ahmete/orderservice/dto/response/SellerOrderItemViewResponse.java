package com.ahmete.orderservice.dto.response;

import com.ahmete.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SellerOrderItemViewResponse(
		Long orderId,
		Long buyerId,
		OrderStatus orderStatus,
		Long productId,
		String productName,
		BigDecimal unitPrice,
		Integer quantity,
		BigDecimal lineTotal,
		Instant createdAt
) {}