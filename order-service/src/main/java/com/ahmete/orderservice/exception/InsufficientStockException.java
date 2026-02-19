package com.ahmete.orderservice.exception;

public class InsufficientStockException extends RuntimeException {
	public InsufficientStockException(String message) {
		super(message);
	}
}