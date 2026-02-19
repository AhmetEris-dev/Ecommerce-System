package com.ahmete.orderservice.service;

import com.ahmete.orderservice.dto.response.OrderItemResponse;
import com.ahmete.orderservice.dto.response.OrderResponse;
import com.ahmete.orderservice.dto.response.OrderSummaryResponse;
import com.ahmete.orderservice.entity.Order;
import com.ahmete.orderservice.entity.OrderItem;

import java.util.List;

public final class OrderMapper {
	
	private OrderMapper() {}
	
	public static OrderSummaryResponse toSummary(Order o) {
		return new OrderSummaryResponse(
				o.getId(),
				o.getStatus(),
				o.getTotalAmount(),
				o.getCreatedAt()
		);
	}
	
	public static OrderResponse toResponse(Order o) {
		List<OrderItemResponse> items = o.getItems().stream()
		                                 .map(OrderMapper::toItemResponse)
		                                 .toList();
		
		return new OrderResponse(
				o.getId(),
				o.getBuyerId(),
				o.getStatus(),
				o.getTotalAmount(),
				o.getCurrency(),
				o.getCreatedAt(),
				items
		);
	}
	
	public static OrderItemResponse toItemResponse(OrderItem i) {
		return new OrderItemResponse(
				i.getId(),
				i.getProductId(),
				i.getProductName(),
				i.getCompanyId(),
				i.getUnitPrice(),
				i.getQuantity(),
				i.getLineTotal()
		);
	}
}