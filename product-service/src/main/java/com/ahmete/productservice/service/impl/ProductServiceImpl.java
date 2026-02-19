package com.ahmete.productservice.service.impl;

import com.ahmete.productservice.dto.request.CreateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductRequest;
import com.ahmete.productservice.dto.request.UpdateProductStatusRequest;
import com.ahmete.productservice.dto.response.ProductImageResponse;
import com.ahmete.productservice.dto.response.ProductResponse;
import com.ahmete.productservice.entity.Product;
import com.ahmete.productservice.entity.ProductImage;
import com.ahmete.productservice.entity.ProductStatus;
import com.ahmete.productservice.exception.ConflictException;
import com.ahmete.productservice.exception.ForbiddenException;
import com.ahmete.productservice.exception.NotFoundException;
import com.ahmete.productservice.repository.ProductRepository;
import com.ahmete.productservice.security.SecurityUtils;
import com.ahmete.productservice.service.ProductService;
import com.ahmete.productservice.service.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepository;
	
	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<ProductResponse> list(String q, Long companyId, ProductStatus status, Pageable pageable) {
		boolean admin = SecurityUtils.isAdmin();
		
		// status behavior:
		// - anonymous: ACTIVE only
		// - authenticated BUYER/SELLER: ACTIVE only
		// - ADMIN: can see all statuses and can filter
		ProductStatus effectiveStatus = admin ? status : ProductStatus.ACTIVE;
		
		Specification<Product> spec = Specification
				.where(ProductSpecifications.qContainsNameOrSku(q))
				.and(ProductSpecifications.companyIdEquals(companyId))
				.and(ProductSpecifications.statusEquals(effectiveStatus));
		
		return productRepository.findAll(spec, pageable).map(this::toResponse);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ProductResponse getById(Long id) {
		boolean admin = SecurityUtils.isAdmin();
		
		Product product;
		if (admin) {
			product = productRepository.findById(id)
			                           .orElseThrow(() -> new NotFoundException("Product not found: " + id));
		} else {
			product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
			                           .orElseThrow(() -> new NotFoundException("Product not found: " + id));
		}
		return toResponse(product);
	}
	
	@Override
	public ProductResponse create(CreateProductRequest request) {
		ensureSellerOrAdmin();
		
		if (productRepository.existsBySku(request.sku())) {
			throw new ConflictException("SKU already exists: " + request.sku());
		}
		
		Long companyId;
		if (SecurityUtils.isSeller()) {
			companyId = SecurityUtils.currentCompanyId();
			if (companyId == null) {
				throw new ForbiddenException("SELLER token must include companyId");
			}
		} else {
			// ADMIN
			if (request.companyId() == null) {
				throw new ForbiddenException("ADMIN must provide companyId in request");
			}
			companyId = request.companyId();
		}
		
		Product p = new Product();
		p.setCompanyId(companyId);
		p.setName(request.name());
		p.setDescription(request.description());
		p.setSku(request.sku());
		p.setPrice(request.price());
		p.setStock(request.stock());
		p.setStatus(request.status() != null ? request.status() : ProductStatus.ACTIVE);
		
		Product saved = productRepository.save(p);
		return toResponse(saved);
	}
	
	@Override
	public ProductResponse update(Long id, UpdateProductRequest request) {
		ensureSellerOrAdmin();
		
		Product product = productRepository.findById(id)
		                                   .orElseThrow(() -> new NotFoundException("Product not found: " + id));
		
		enforceOwnershipIfSeller(product);
		
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStock(request.stock());
		product.setStatus(request.status());
		
		return toResponse(product);
	}
	
	@Override
	public ProductResponse updateStatus(Long id, UpdateProductStatusRequest request) {
		ensureSellerOrAdmin();
		
		Product product = productRepository.findById(id)
		                                   .orElseThrow(() -> new NotFoundException("Product not found: " + id));
		
		enforceOwnershipIfSeller(product);
		
		product.setStatus(request.status());
		return toResponse(product);
	}
	
	@Override
	public void delete(Long id) {
		ensureSellerOrAdmin();
		
		Product product = productRepository.findById(id)
		                                   .orElseThrow(() -> new NotFoundException("Product not found: " + id));
		
		enforceOwnershipIfSeller(product);
		
		productRepository.delete(product);
	}
	
	private void ensureSellerOrAdmin() {
		if (!SecurityUtils.isSeller() && !SecurityUtils.isAdmin()) {
			throw new ForbiddenException("Only SELLER or ADMIN can perform this operation");
		}
	}
	
	private void enforceOwnershipIfSeller(Product product) {
		if (SecurityUtils.isAdmin()) return;
		
		Long tokenCompanyId = SecurityUtils.currentCompanyId();
		if (tokenCompanyId == null) {
			throw new ForbiddenException("SELLER token must include companyId");
		}
		if (!tokenCompanyId.equals(product.getCompanyId())) {
			throw new ForbiddenException("You can only manage products belonging to your company");
		}
	}
	
	private ProductResponse toResponse(Product p) {
		List<ProductImageResponse> images = p.getImages().stream()
		                                     .map(this::toImageResponse)
		                                     .toList();
		
		return new ProductResponse(
				p.getId(),
				p.getCompanyId(),
				p.getName(),
				p.getDescription(),
				p.getSku(),
				p.getPrice(),
				p.getStock(),
				p.getStatus(),
				p.getCreatedAt(),
				p.getUpdatedAt(),
				images
		);
	}
	
	private ProductImageResponse toImageResponse(ProductImage img) {
		return new ProductImageResponse(
				img.getId(),
				img.getUrl(),
				img.getSortOrder(),
				img.getCreatedAt()
		);
	}
}