package com.ahmete.productservice.service;

import com.ahmete.productservice.dto.request.CreateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductStatusRequest;
import com.ahmete.productservice.dto.response.ProductResponse;
import com.ahmete.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
	
	Page<ProductResponse> list(String q, Long companyId, ProductStatus status, Pageable pageable);
	
	ProductResponse getById(Long id);
	
	ProductResponse create(CreateProductRequest request);
	
	ProductResponse update(Long id, UpdateProductRequest request);
	
	ProductResponse updateStatus(Long id, UpdateProductStatusRequest request);
	
	void delete(Long id);
}