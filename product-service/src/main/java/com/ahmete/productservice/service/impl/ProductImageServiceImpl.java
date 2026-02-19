package com.ahmete.productservice.service.impl;

import com.ahmete.productservice.dto.request.AddProductImageRequest;
import com.ahmete.productservice.dto.response.ProductImageResponse;
import com.ahmete.productservice.entity.Product;
import com.ahmete.productservice.entity.ProductImage;
import com.ahmete.productservice.entity.ProductStatus;
import com.ahmete.productservice.exception.ForbiddenException;
import com.ahmete.productservice.exception.NotFoundException;
import com.ahmete.productservice.repository.ProductImageRepository;
import com.ahmete.productservice.repository.ProductRepository;
import com.ahmete.productservice.security.SecurityUtils;
import com.ahmete.productservice.service.ProductImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {
	
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	
	public ProductImageServiceImpl(ProductRepository productRepository, ProductImageRepository productImageRepository) {
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductImageResponse> listImages(Long productId) {
		boolean admin = SecurityUtils.isAdmin();
		
		// Visibility rules consistent with product read:
		// - Anonymous/BUYER/SELLER: only ACTIVE product images visible
		// - ADMIN: any
		Product product;
		if (admin) {
			product = productRepository.findById(productId)
			                           .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
		} else {
			product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
			                           .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
		}
		
		return product.getImages().stream()
		              .map(img -> new ProductImageResponse(img.getId(), img.getUrl(), img.getSortOrder(), img.getCreatedAt()))
		              .toList();
	}
	
	@Override
	public ProductImageResponse addImage(Long productId, AddProductImageRequest request) {
		ensureSellerOrAdmin();
		
		Product product = productRepository.findById(productId)
		                                   .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
		
		enforceOwnershipIfSeller(product);
		
		ProductImage img = new ProductImage();
		img.setUrl(request.url());
		img.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
		
		product.addImage(img); // cascades persist
		productRepository.save(product);
		
		return new ProductImageResponse(img.getId(), img.getUrl(), img.getSortOrder(), img.getCreatedAt());
	}
	
	@Override
	public void removeImage(Long productId, Long imageId) {
		ensureSellerOrAdmin();
		
		Product product = productRepository.findById(productId)
		                                   .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
		
		enforceOwnershipIfSeller(product);
		
		ProductImage img = productImageRepository.findByIdAndProductId(imageId, productId)
		                                         .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));
		
		product.removeImage(img);
		productRepository.save(product);
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
}