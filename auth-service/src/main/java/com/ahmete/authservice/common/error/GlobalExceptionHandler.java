package com.ahmete.authservice.common.error;

import com.ahmete.authservice.common.exception.ApiException;
import com.ahmete.authservice.userclient.exception.UserServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
		return build(ex.getStatus(), ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(UserServiceException.class)
	public ResponseEntity<ApiErrorResponse> handleUserService(UserServiceException ex, HttpServletRequest req) {
		return build(ex.getStatus(), ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
		               .map(this::formatFieldError)
		               .collect(Collectors.joining(", "));
		return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "Malformed JSON request body", req.getRequestURI());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI());
	}
	
	private String formatFieldError(FieldError fe) {
		return fe.getField() + ": " + fe.getDefaultMessage();
	}
	
	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path
		);
		return ResponseEntity.status(status).body(body);
	}
}