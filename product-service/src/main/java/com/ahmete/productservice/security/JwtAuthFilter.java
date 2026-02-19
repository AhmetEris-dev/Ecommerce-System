package com.ahmete.productservice.security;

import com.ahmete.productservice.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtProperties props;
	
	public JwtAuthFilter(JwtProperties props) {
		this.props = props;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = header.substring("Bearer ".length()).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}
		
		try {
			byte[] keyBytes = props.secret().getBytes(StandardCharsets.UTF_8);
			if (keyBytes.length < 32) {
				throw new JwtAuthenticationException("JWT secret must be at least 32 bytes");
			}
			
			Jws<Claims> jws = Jwts.parser()
			                      .verifyWith(Keys.hmacShaKeyFor(keyBytes))
			                      .requireIssuer(props.issuer())
			                      .build()
			                      .parseSignedClaims(token);
			
			Claims claims = jws.getPayload();
			
			String sub = claims.getSubject();
			if (sub == null || sub.isBlank()) {
				throw new JwtAuthenticationException("JWT subject (sub) is missing");
			}
			Long userId;
			try {
				userId = Long.parseLong(sub);
			} catch (NumberFormatException e) {
				throw new JwtAuthenticationException("JWT subject (sub) must be numeric userId", e);
			}
			
			String role = claims.get("role", String.class);
			if (role == null || role.isBlank()) {
				throw new JwtAuthenticationException("JWT claim 'role' is missing");
			}
			
			Long companyId = null;
			Object companyIdObj = claims.get("companyId");
			if (companyIdObj != null) {
				if (companyIdObj instanceof Number n) {
					companyId = n.longValue();
				} else if (companyIdObj instanceof String s && !s.isBlank()) {
					try {
						companyId = Long.parseLong(s);
					} catch (NumberFormatException e) {
						throw new JwtAuthenticationException("JWT claim 'companyId' must be numeric", e);
					}
				} else {
					throw new JwtAuthenticationException("JWT claim 'companyId' must be numeric");
				}
			}
			
			ApiPrincipal principal = new ApiPrincipal(userId, role, companyId);
			
			var auth = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role))
			);
			
			SecurityContextHolder.getContext().setAuthentication(auth);
			filterChain.doFilter(request, response);
			
		} catch (JwtException e) {
			// signature/exp/format issues
			SecurityContextHolder.clearContext();
			throw new JwtAuthenticationException("Invalid or expired JWT", e);
		}
	}
}