package com.ahmete.orderservice.security;

import com.ahmete.orderservice.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
	
	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
	private static final List<String> SWAGGER_WHITELIST = List.of(
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/swagger-ui.html"
	);
	
	private final JwtProperties props;
	private final SecretKey key;
	
	public JwtAuthFilter(JwtProperties props) {
		this.props = props;
		byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalStateException("jwt.secret must be at least 32 bytes for HS256");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return SWAGGER_WHITELIST.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		
		String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth == null || !auth.startsWith("Bearer ")) {
			throw new UnauthorizedException("Missing Authorization header");
		}
		
		String token = auth.substring("Bearer ".length()).trim();
		try {
			Jws<Claims> jws = Jwts.parser()
			                      .verifyWith(key)
			                      .requireIssuer(props.issuer())
			                      .build()
			                      .parseSignedClaims(token);
			
			Claims claims = jws.getPayload();
			
			String sub = claims.getSubject();
			if (sub == null || sub.isBlank()) throw new UnauthorizedException("Invalid token subject");
			
			Long userId;
			try {
				userId = Long.parseLong(sub);
			} catch (NumberFormatException e) {
				throw new UnauthorizedException("Invalid token subject");
			}
			
			String role = claims.get("role", String.class);
			if (role == null || role.isBlank()) throw new UnauthorizedException("Missing role claim");
			
			Long companyId = null;
			Object companyIdObj = claims.get("companyId");
			if (companyIdObj != null) {
				if (companyIdObj instanceof Number n) {
					companyId = n.longValue();
				} else if (companyIdObj instanceof String s && !s.isBlank()) {
					companyId = Long.parseLong(s);
				}
			}
			
			ApiPrincipal principal = new ApiPrincipal(userId, role, companyId);
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
			);
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
			
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("Token expired");
		} catch (JwtException | IllegalArgumentException e) {
			throw new UnauthorizedException("Invalid token");
		}
	}
}