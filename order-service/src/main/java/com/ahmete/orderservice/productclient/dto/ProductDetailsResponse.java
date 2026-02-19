package com.ahmete.orderservice.productclient.dto;

import java.math.BigDecimal;

public record ProductDetailsResponse(
		Long id,
		String name,
		BigDecimal price,
		Integer stock,
		String status,
		Long companyId
) {}