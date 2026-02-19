package com.ahmete.productservice.repository;

import com.ahmete.productservice.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	
	List<ProductImage> findAllByProductIdOrderBySortOrderAscIdAsc(Long productId);
	
	Optional<ProductImage> findByIdAndProductId(Long id, Long productId);
}