package com.ahmete.productservice.controller;

import com.ahmete.productservice.constants.RestApis;
import com.ahmete.productservice.dto.request.CreateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductStatusRequest;
import com.ahmete.productservice.dto.response.ProductResponse;
import com.ahmete.productservice.entity.ProductStatus;
import com.ahmete.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RestApis.Product.ROOT)
public class ProductController {
	
	private final ProductService productService;
	
	public ProductController(ProductService productService) {
		this.productService = productService;
	}
	
	// Public read (JWT optional)
	@GetMapping
	public Page<ProductResponse> list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Long companyId,
			@RequestParam(required = false) ProductStatus status,
			Pageable pageable
	) {
		return productService.list(q, companyId, status, pageable);
	}
	
	@GetMapping(RestApis.Product.ID)
	public ProductResponse getById(@PathVariable Long id) {
		return productService.getById(id);
	}
	
	// Authenticated write (JWT required by SecurityConfig)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
		return productService.create(request);
	}
	
	@PutMapping(RestApis.Product.ID)
	public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
		return productService.update(id, request);
	}
	
	@PatchMapping(RestApis.Product.STATUS)
	public ProductResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateProductStatusRequest request) {
		return productService.updateStatus(id, request);
	}
	
	@DeleteMapping(RestApis.Product.ID)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		productService.delete(id);
	}
}