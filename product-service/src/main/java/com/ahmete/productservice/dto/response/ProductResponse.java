package com.ahmete.productservice.dto.response;

import com.ahmete.productservice.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
		Long id,
		Long companyId,
		String name,
		String description,
		String sku,
		BigDecimal price,
		Integer stock,
		ProductStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<ProductImageResponse> images
) {}