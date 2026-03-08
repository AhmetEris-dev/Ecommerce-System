package com.ahmete.orderservice.productclient.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Kept for backward compatibility; currently unused in new stock API.
public record DecreaseStockRequest(
		@NotNull @Min(1) Integer quantity
) {}