package com.ahmete.orderservice.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productName,
		Long companyId,
		BigDecimal unitPrice,
		Integer quantity,
		BigDecimal lineTotal
) {}