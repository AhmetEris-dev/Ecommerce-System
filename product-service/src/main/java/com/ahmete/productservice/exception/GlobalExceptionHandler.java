package com.ahmete.productservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
	}
	
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
	}
	
	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
		               .map(this::formatFieldError)
		               .collect(Collectors.joining("; "));
		if (msg.isBlank()) msg = "Validation failed";
		return build(HttpStatus.BAD_REQUEST, msg, req);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
		String msg = ex.getConstraintViolations().stream()
		               .map(v -> v.getPropertyPath() + ": " + v.getMessage())
		               .collect(Collectors.joining("; "));
		if (msg.isBlank()) msg = "Validation failed";
		return build(HttpStatus.BAD_REQUEST, msg, req);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
		// İç detayı sızdırma. Log istiyorsan burada logla.
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req);
	}
	
	private String formatFieldError(FieldError fe) {
		String field = fe.getField();
		String msg = fe.getDefaultMessage();
		Object rejected = fe.getRejectedValue();
		if (rejected == null) return field + ": " + msg;
		return field + ": " + msg + " (rejected=" + rejected + ")";
	}
	
	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				req.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}
}