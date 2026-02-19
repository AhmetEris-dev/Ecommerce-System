package com.ahmete.productservice.service;

import com.ahmete.productservice.entity.Product;
import com.ahmete.productservice.entity.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {
	
	private ProductSpecifications() {}
	
	public static Specification<Product> companyIdEquals(Long companyId) {
		return (root, query, cb) -> companyId == null ? cb.conjunction() : cb.equal(root.get("companyId"), companyId);
	}
	
	public static Specification<Product> statusEquals(ProductStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}
	
	public static Specification<Product> qContainsNameOrSku(String q) {
		return (root, query, cb) -> {
			if (q == null || q.isBlank()) return cb.conjunction();
			String like = "%" + q.trim().toLowerCase() + "%";
			return cb.or(
					cb.like(cb.lower(root.get("name")), like),
					cb.like(cb.lower(root.get("sku")), like)
			);
		};
	}
}