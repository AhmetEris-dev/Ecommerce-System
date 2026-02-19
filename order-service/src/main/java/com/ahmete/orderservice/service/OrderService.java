package com.ahmete.orderservice.service;

import com.ahmete.orderservice.dto.request.CreateOrderItemRequest;
import com.ahmete.orderservice.dto.request.CreateOrderRequest;
import com.ahmete.orderservice.dto.response.OrderResponse;
import com.ahmete.orderservice.dto.response.OrderSummaryResponse;
import com.ahmete.orderservice.dto.response.SellerOrderItemViewResponse;
import com.ahmete.orderservice.entity.Order;
import com.ahmete.orderservice.entity.OrderItem;
import com.ahmete.orderservice.entity.OrderStatus;
import com.ahmete.orderservice.exception.BadRequestException;
import com.ahmete.orderservice.exception.ForbiddenException;
import com.ahmete.orderservice.exception.NotFoundException;
import com.ahmete.orderservice.productclient.ProductServiceClient;
import com.ahmete.orderservice.productclient.dto.ProductDetailsResponse;
import com.ahmete.orderservice.repository.OrderItemRepository;
import com.ahmete.orderservice.repository.OrderRepository;
import com.ahmete.orderservice.security.SecurityUtils;
import com.ahmete.orderservice.service.projection.SellerOrderItemViewProjection;
import com.ahmete.orderservice.service.spec.OrderSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
	
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductServiceClient productClient;
	
	public OrderService(OrderRepository orderRepository,
	                    OrderItemRepository orderItemRepository,
	                    ProductServiceClient productClient) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productClient = productClient;
	}
	
	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		if (!SecurityUtils.isBuyer()) {
			throw new ForbiddenException("Only BUYER can create orders");
		}
		
		Map<Long, Integer> merged = mergeItems(request.items());
		if (merged.isEmpty()) throw new BadRequestException("Order items cannot be empty");
		
		// 1) Fetch + validate products
		Map<Long, ProductDetailsResponse> products = new LinkedHashMap<>();
		for (Map.Entry<Long, Integer> e : merged.entrySet()) {
			Long productId = e.getKey();
			Integer qty = e.getValue();
			
			ProductDetailsResponse p = productClient.getProductById(productId);
			
			if (p.status() == null || !"ACTIVE".equalsIgnoreCase(p.status())) {
				throw new ForbiddenException("Product not ACTIVE: productId=" + productId);
			}
			if (p.stock() == null || p.stock() < qty) {
				throw new com.ahmete.orderservice.exception.InsufficientStockException(
						"Insufficient stock for productId=" + productId
				);
			}
			if (p.price() == null || p.price().compareTo(BigDecimal.ZERO) <= 0) {
				throw new BadRequestException("Invalid product price: productId=" + productId);
			}
			if (p.companyId() == null) {
				throw new BadRequestException("Product companyId missing: productId=" + productId);
			}
			
			products.put(productId, p);
		}
		
		// 2) Best-effort stock decrement BEFORE persisting order
		// NOTE: If some decrements succeed and a later one fails, order DB will not persist, but earlier decrements remain (no compensation in scope).
		for (Map.Entry<Long, Integer> e : merged.entrySet()) {
			productClient.decreaseStock(e.getKey(), e.getValue());
		}
		
		// 3) Build entities + totals
		Order order = new Order();
		order.setBuyerId(SecurityUtils.currentUserId());
		order.setStatus(OrderStatus.NEW);
		order.setCurrency("TRY");
		
		BigDecimal total = BigDecimal.ZERO;
		
		for (Map.Entry<Long, Integer> e : merged.entrySet()) {
			Long productId = e.getKey();
			Integer qty = e.getValue();
			ProductDetailsResponse p = products.get(productId);
			
			BigDecimal lineTotal = p.price().multiply(BigDecimal.valueOf(qty));
			
			OrderItem item = new OrderItem();
			item.setProductId(p.id());
			item.setProductName(p.name());
			item.setCompanyId(p.companyId());
			item.setUnitPrice(p.price());
			item.setQuantity(qty);
			item.setLineTotal(lineTotal);
			
			order.addItem(item);
			
			total = total.add(lineTotal);
		}
		
		order.setTotalAmount(total);
		
		Order saved = orderRepository.save(order);
		// ensure items loaded
		saved.getItems().size();
		
		return OrderMapper.toResponse(saved);
	}
	
	public Page<OrderSummaryResponse> buyerListMyOrders(Pageable pageable) {
		if (!SecurityUtils.isBuyer()) throw new ForbiddenException("Only BUYER can access /orders/me");
		
		Long buyerId = SecurityUtils.currentUserId();
		
		Specification<Order> spec = OrderSpecifications.buyerIdEquals(buyerId);
		return orderRepository.findAll(spec, pageable).map(OrderMapper::toSummary);
	}
	
	public OrderResponse buyerGetMyOrder(Long orderId) {
		if (!SecurityUtils.isBuyer()) throw new ForbiddenException("Only BUYER can access /orders/me/{orderId}");
		
		Order order = orderRepository.findById(orderId)
		                             .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
		
		if (!order.getBuyerId().equals(SecurityUtils.currentUserId())) {
			throw new ForbiddenException("You can only view your own orders");
		}
		
		order.getItems().size();
		return OrderMapper.toResponse(order);
	}
	
	public Page<SellerOrderItemViewResponse> sellerListCompanyItems(Pageable pageable) {
		if (!SecurityUtils.isSeller()) throw new ForbiddenException("Only SELLER can access /orders/seller");
		
		Long companyId = SecurityUtils.currentCompanyId();
		if (companyId == null) throw new ForbiddenException("SELLER token must include companyId");
		
		Page<SellerOrderItemViewProjection> page = orderItemRepository.findSellerItems(companyId, pageable);
		return page.map(p -> new SellerOrderItemViewResponse(
				p.getOrderId(),
				p.getBuyerId(),
				p.getOrderStatus(),
				p.getProductId(),
				p.getProductName(),
				p.getUnitPrice(),
				p.getQuantity(),
				p.getLineTotal(),
				p.getCreatedAt()
		));
	}
	
	public Page<OrderSummaryResponse> adminListOrders(
			OrderStatus status,
			Long buyerId,
			Instant createdAtFrom,
			Instant createdAtTo,
			Pageable pageable
	) {
		if (!SecurityUtils.isAdmin()) throw new ForbiddenException("Only ADMIN can access /orders");
		
		Specification<Order> spec = Specification.where(OrderSpecifications.statusEquals(status))
		                                         .and(OrderSpecifications.buyerIdEquals(buyerId))
		                                         .and(OrderSpecifications.createdAtFrom(createdAtFrom))
		                                         .and(OrderSpecifications.createdAtTo(createdAtTo));
		
		return orderRepository.findAll(spec, pageable).map(OrderMapper::toSummary);
	}
	
	public OrderResponse adminGetOrder(Long orderId) {
		if (!SecurityUtils.isAdmin()) throw new ForbiddenException("Only ADMIN can access /orders/{orderId}");
		
		Order order = orderRepository.findById(orderId)
		                             .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
		
		order.getItems().size();
		return OrderMapper.toResponse(order);
	}
	
	@Transactional
	public OrderResponse cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
		                             .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
		
		boolean isBuyer = SecurityUtils.isBuyer();
		boolean isAdmin = SecurityUtils.isAdmin();
		
		if (!isBuyer && !isAdmin) {
			throw new ForbiddenException("Only BUYER or ADMIN can cancel");
		}
		
		if (isBuyer) {
			if (!order.getBuyerId().equals(SecurityUtils.currentUserId())) {
				throw new ForbiddenException("You can only cancel your own order");
			}
			if (order.getStatus() != OrderStatus.NEW) {
				throw new ForbiddenException("BUYER can cancel only if status NEW");
			}
		}
		
		if (isAdmin) {
			if (order.getStatus() == OrderStatus.DELIVERED) {
				throw new ForbiddenException("ADMIN cannot cancel DELIVERED orders");
			}
		}
		
		order.setStatus(OrderStatus.CANCELLED);
		Order saved = orderRepository.save(order);
		saved.getItems().size();
		return OrderMapper.toResponse(saved);
	}
	
	private Map<Long, Integer> mergeItems(List<CreateOrderItemRequest> items) {
		Map<Long, Integer> merged = new LinkedHashMap<>();
		for (CreateOrderItemRequest i : items) {
			if (i.productId() == null) throw new BadRequestException("productId is required");
			if (i.quantity() == null || i.quantity() < 1) throw new BadRequestException("quantity must be >= 1");
			
			merged.merge(i.productId(), i.quantity(), Integer::sum);
		}
		return merged;
	}
}