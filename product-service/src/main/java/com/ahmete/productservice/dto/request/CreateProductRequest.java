package com.ahmete.productservice.dto.request;

import com.ahmete.productservice.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
		// ADMIN must provide companyId. SELLER must NOT rely on this (token companyId used).
		Long companyId,
		
		@NotBlank
		@Size(max = 200)
		String name,
		
		@Size(max = 2000)
		String description,
		
		@NotBlank
		@Size(max = 64)
		String sku,
		
		@NotNull
		@DecimalMin(value = "0.0", inclusive = true)
		BigDecimal price,
		
		@NotNull
		@Min(0)
		Integer stock,
		
		ProductStatus status
) {}