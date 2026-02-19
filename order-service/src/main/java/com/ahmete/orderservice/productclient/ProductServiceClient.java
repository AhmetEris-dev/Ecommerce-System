package com.ahmete.orderservice.productclient;

import com.ahmete.orderservice.exception.ForbiddenException;
import com.ahmete.orderservice.exception.InsufficientStockException;
import com.ahmete.orderservice.exception.NotFoundException;
import com.ahmete.orderservice.productclient.dto.DecreaseStockRequest;
import com.ahmete.orderservice.productclient.dto.ProductDetailsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {
	
	private final RestClient restClient;
	
	public ProductServiceClient(RestClient productServiceRestClient) {
		this.restClient = productServiceRestClient;
	}
	
	public ProductDetailsResponse getProductById(Long id) {
		try {
			return restClient.get()
			                 .uri("/products/{id}", id)
			                 .retrieve()
			                 .body(ProductDetailsResponse.class);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new NotFoundException("Product not found: " + id);
			}
			throw e;
		}
	}
	
	public void decreaseStock(Long productId, int quantity) {
		DecreaseStockRequest req = new DecreaseStockRequest(quantity);
		
		try {
			restClient.post()
			          .uri("/internal/products/{id}/decrease-stock", productId)
			          .body(req)
			          .retrieve()
			          .toBodilessEntity();
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.CONFLICT) {
				throw new InsufficientStockException("Insufficient stock for productId=" + productId);
			}
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new NotFoundException("Product not found: " + productId);
			}
			if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
				throw new ForbiddenException("Product not ACTIVE: productId=" + productId);
			}
			throw e;
		}
	}
}