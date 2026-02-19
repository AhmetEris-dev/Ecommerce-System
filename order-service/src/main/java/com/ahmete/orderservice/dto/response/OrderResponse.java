package com.ahmete.orderservice.dto.response;

import com.ahmete.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		Long buyerId,
		OrderStatus status,
		BigDecimal totalAmount,
		String currency,
		Instant createdAt,
		List<OrderItemResponse> items
) {}