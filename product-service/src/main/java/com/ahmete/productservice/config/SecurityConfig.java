package com.ahmete.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						// Swagger & OpenAPI serbest
						.requestMatchers(
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/swagger-ui.html"
						).permitAll()
						// Şimdilik her şeye izin ver
						.anyRequest().permitAll()
				)
				// Login ekranı / basic auth çıkmasın
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.build();
	}
}