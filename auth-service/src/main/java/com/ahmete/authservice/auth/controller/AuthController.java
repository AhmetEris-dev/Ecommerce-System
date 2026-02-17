package com.ahmete.authservice.auth.controller;

import com.ahmete.authservice.auth.dto.LoginRequest;
import com.ahmete.authservice.auth.dto.LogoutRequest;
import com.ahmete.authservice.auth.dto.RefreshRequest;
import com.ahmete.authservice.auth.dto.TokenResponse;
import com.ahmete.authservice.auth.service.AuthService;
import com.ahmete.authservice.constants.RestApis;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RestApis.Auth.ROOT)
public class AuthController {
	
	private final AuthService authService;
	
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping(RestApis.Auth.LOGIN)
	public TokenResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpReq) {
		String ip = clientIp(httpReq);
		String ua = httpReq.getHeader("User-Agent");
		return authService.login(req.email(), req.password(), ip, ua);
	}
	
	@PostMapping(RestApis.Auth.REFRESH)
	public TokenResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest httpReq) {
		String ip = clientIp(httpReq);
		String ua = httpReq.getHeader("User-Agent");
		return authService.refresh(req.refreshToken(), ip, ua);
	}
	
	@PostMapping(RestApis.Auth.LOGOUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody LogoutRequest req) {
		authService.logout(req.refreshToken());
	}
	
	private String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			int comma = xff.indexOf(',');
			return (comma > 0 ? xff.substring(0, comma) : xff).trim();
		}
		return request.getRemoteAddr();
	}
}