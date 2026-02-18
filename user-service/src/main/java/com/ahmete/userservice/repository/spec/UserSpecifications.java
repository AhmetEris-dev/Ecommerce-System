package com.ahmete.userservice.repository.spec;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;
import com.ahmete.userservice.entity.User;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {
	
	private UserSpecifications() {}
	
	public static Specification<User> role(Role role) {
		return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
	}
	
	public static Specification<User> status(UserStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}
	
	public static Specification<User> companyId(Long companyId) {
		return (root, query, cb) -> companyId == null ? cb.conjunction() : cb.equal(root.get("company").get("id"), companyId);
	}
	
	public static Specification<User> q(String q) {
		return (root, query, cb) -> {
			if (q == null || q.isBlank()) return cb.conjunction();
			String like = "%" + q.trim().toLowerCase() + "%";
			Expression<String> email = cb.lower(root.get("email"));
			Expression<String> firstName = cb.lower(root.get("firstName"));
			Expression<String> lastName = cb.lower(root.get("lastName"));
			return cb.or(
					cb.like(email, like),
					cb.like(firstName, like),
					cb.like(lastName, like)
			);
		};
	}
}