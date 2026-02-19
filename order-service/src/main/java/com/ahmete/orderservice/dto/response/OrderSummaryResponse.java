package com.ahmete.orderservice.dto.response;

import com.ahmete.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
		Long id,
		OrderStatus status,
		BigDecimal totalAmount,
		Instant createdAt
) {}