package com.ahmete.orderservice.repository;

import com.ahmete.orderservice.entity.OrderItem;
import com.ahmete.orderservice.service.projection.SellerOrderItemViewProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	
	@Query("""
           select
             oi.order.id as orderId,
             oi.order.buyerId as buyerId,
             oi.order.status as orderStatus,
             oi.productId as productId,
             oi.productName as productName,
             oi.unitPrice as unitPrice,
             oi.quantity as quantity,
             oi.lineTotal as lineTotal,
             oi.createdAt as createdAt
           from OrderItem oi
           where oi.companyId = :companyId
           """)
	Page<SellerOrderItemViewProjection> findSellerItems(Long companyId, Pageable pageable);
}