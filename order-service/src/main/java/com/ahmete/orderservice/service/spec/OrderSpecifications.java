package com.ahmete.orderservice.service.spec;

import com.ahmete.orderservice.entity.Order;
import com.ahmete.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class OrderSpecifications {
	
	private OrderSpecifications() {}
	
	public static Specification<Order> buyerIdEquals(Long buyerId) {
		if (buyerId == null) return null;
		return (root, query, cb) -> cb.equal(root.get("buyerId"), buyerId);
	}
	
	public static Specification<Order> statusEquals(OrderStatus status) {
		if (status == null) return null;
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}
	
	public static Specification<Order> createdAtFrom(Instant from) {
		if (from == null) return null;
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
	}
	
	public static Specification<Order> createdAtTo(Instant to) {
		if (to == null) return null;
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
	}
}