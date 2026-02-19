package com.ahmete.orderservice.controller;

import com.ahmete.orderservice.constants.RestApis;
import com.ahmete.orderservice.dto.request.CreateOrderRequest;
import com.ahmete.orderservice.dto.response.OrderResponse;
import com.ahmete.orderservice.dto.response.OrderSummaryResponse;
import com.ahmete.orderservice.dto.response.SellerOrderItemViewResponse;
import com.ahmete.orderservice.entity.OrderStatus;
import com.ahmete.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(RestApis.Order.ROOT)
public class OrderController {
	
	private final OrderService orderService;
	
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	// 1) Create order (BUYER only)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
		return orderService.createOrder(request);
	}
	
	// 2) Buyer self read
	@GetMapping(RestApis.Order.ME)
	public Page<OrderSummaryResponse> myOrders(@PageableDefault(size = 10) Pageable pageable) {
		return orderService.buyerListMyOrders(pageable);
	}
	
	@GetMapping(RestApis.Order.ME_BY_ID)
	public OrderResponse myOrderById(@PathVariable Long orderId) {
		return orderService.buyerGetMyOrder(orderId);
	}
	
	// 3) Seller read
	@GetMapping(RestApis.Order.SELLER)
	public Page<SellerOrderItemViewResponse> sellerView(@PageableDefault(size = 10) Pageable pageable) {
		return orderService.sellerListCompanyItems(pageable);
	}
	
	// 4) Admin read with filters
	@GetMapping
	public Page<OrderSummaryResponse> adminList(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) Long buyerId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtTo,
			@PageableDefault(size = 10) Pageable pageable
	) {
		return orderService.adminListOrders(status, buyerId, createdAtFrom, createdAtTo, pageable);
	}
	
	@GetMapping(RestApis.Order.BY_ID)
	public OrderResponse adminGet(@PathVariable Long orderId) {
		return orderService.adminGetOrder(orderId);
	}
	
	// 5) Cancel
	@PostMapping(RestApis.Order.CANCEL)
	public OrderResponse cancel(@PathVariable Long orderId) {
		return orderService.cancelOrder(orderId);
	}
}