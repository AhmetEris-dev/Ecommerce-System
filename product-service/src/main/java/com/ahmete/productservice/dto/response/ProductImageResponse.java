package com.ahmete.productservice.dto.response;

import java.time.Instant;

public record ProductImageResponse(
		Long id,
		String url,
		Integer sortOrder,
		Instant createdAt
) {}