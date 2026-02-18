package com.ahmete.userservice.security;

import com.ahmete.userservice.config.JwtProperties;
import com.ahmete.userservice.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final String issuer;
	private final SecretKey key;
	
	public JwtAuthFilter(JwtProperties props) {
		this.issuer = props.issuer();
		
		byte[] secretBytes = Optional.ofNullable(props.secret()).orElse("").getBytes(StandardCharsets.UTF_8);
		// HS256 requires >= 256-bit key (32 bytes). Fail fast.
		if (secretBytes.length < 32) {
			throw new IllegalStateException("jwt.secret must be at least 32 bytes for HS256");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
	}
	
	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = header.substring("Bearer ".length()).trim();
		try {
			Jws<Claims> jws = Jwts.parser()
			                      .verifyWith(key)
			                      .requireIssuer(issuer)
			                      .build()
			                      .parseSignedClaims(token);
			
			Claims claims = jws.getPayload();
			
			String sub = claims.getSubject();
			if (sub == null || sub.isBlank()) {
				filterChain.doFilter(request, response);
				return;
			}
			
			Long userId = Long.parseLong(sub);
			
			String roleStr = claims.get("role", String.class);
			if (roleStr == null || roleStr.isBlank()) {
				filterChain.doFilter(request, response);
				return;
			}
			Role role = Role.valueOf(roleStr);
			
			Long companyId = claims.get("companyId", Long.class); // may be null
			
			ApiPrincipal principal = new ApiPrincipal(userId, role, companyId);
			
			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
			var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
			
			filterChain.doFilter(request, response);
		} catch (Exception ex) {
			// Invalid token -> act like unauthenticated (Security will enforce 401 where needed)
			SecurityContextHolder.clearContext();
			filterChain.doFilter(request, response);
		}
	}
}