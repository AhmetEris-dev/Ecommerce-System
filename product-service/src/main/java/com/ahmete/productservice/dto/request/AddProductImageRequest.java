package com.ahmete.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProductImageRequest(
		@NotBlank
		@Size(max = 2048)
		String url,
		
		@Min(0)
		Integer sortOrder
) {}