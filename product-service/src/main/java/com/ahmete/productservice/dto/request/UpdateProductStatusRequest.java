package com.ahmete.productservice.dto.request;

import com.ahmete.productservice.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
		@NotNull
		ProductStatus status
) {}