package com.ahmete.userservice.config;

import com.ahmete.userservice.constants.RestApis;
import com.ahmete.userservice.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtProperties jwtProperties) throws Exception {
		JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtProperties);
		
		http
				.csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(registry -> registry
						// Swagger
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
						
						// Public register
						.requestMatchers(HttpMethod.POST, RestApis.Users.ROOT + RestApis.Users.REGISTER).permitAll()
						.requestMatchers(HttpMethod.POST, RestApis.Users.ROOT + RestApis.Users.REGISTER_SELLER).permitAll()
						
						// Internal auth-service verify
						.requestMatchers(HttpMethod.POST, RestApis.InternalUsers.ROOT + RestApis.InternalUsers.VERIFY).permitAll()
						
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.cors(Customizer.withDefaults());
		
		return http.build();
	}
}