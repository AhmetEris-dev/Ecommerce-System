package com.ahmete.userservice.config;

import com.ahmete.userservice.domain.Role;
import com.ahmete.userservice.domain.UserStatus;
import com.ahmete.userservice.entity.User;
import com.ahmete.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Optional dev helper:
 * Creates an initial ADMIN user if none exists (email: admin@local.dev / password: Admin123!)
 * Remove if you don't want seeding.
 */
@Configuration
public class DataSeedConfig {
	
	@Bean
	CommandLineRunner seedAdmin(UserRepository userRepository, BCryptPasswordEncoder encoder) {
		return args -> {
			String adminEmail = "admin@local.dev";
			if (userRepository.existsByEmailIgnoreCase(adminEmail)) return;
			
			User u = new User();
			u.setEmail(adminEmail);
			u.setPasswordHash(encoder.encode("Admin123!"));
			u.setFirstName("System");
			u.setLastName("Admin");
			u.setRole(Role.ADMIN);
			u.setStatus(UserStatus.ACTIVE);
			u.setCompany(null);
			
			userRepository.save(u);
		};
	}
}