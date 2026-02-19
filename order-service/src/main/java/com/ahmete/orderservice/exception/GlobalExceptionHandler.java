package com.ahmete.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ApiError> handleConflict(InsufficientStockException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
	}
	
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
	public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest req) {
		String msg = "Validation error";
		if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
			msg = manv.getBindingResult().getFieldError().getField() + ": " + manv.getBindingResult().getFieldError().getDefaultMessage();
		} else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
			msg = be.getBindingResult().getFieldError().getField() + ": " + be.getBindingResult().getFieldError().getDefaultMessage();
		} else if (ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
			msg = cve.getConstraintViolations().iterator().next().getMessage();
		}
		return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI());
	}
	
	private ResponseEntity<ApiError> build(HttpStatus status, String message, String path) {
		ApiError body = new ApiError(
				Instant.now().toString(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path
		);
		return ResponseEntity.status(status).body(body);
	}
	
	public record ApiError(
			String timestamp,
			int status,
			String error,
			String message,
			String path
	) {}
}