package com.ahmete.productservice.controller;

import com.ahmete.productservice.constants.RestApis;
import com.ahmete.productservice.dto.request.AddProductImageRequest;
import com.ahmete.productservice.dto.response.ProductImageResponse;
import com.ahmete.productservice.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RestApis.Product.ROOT)
public class ProductImageController {
	
	private final ProductImageService productImageService;
	
	public ProductImageController(ProductImageService productImageService) {
		this.productImageService = productImageService;
	}
	
	// Public read (visibility rules enforced in service)
	@GetMapping(RestApis.Product.IMAGES)
	public List<ProductImageResponse> list(@PathVariable Long id) {
		return productImageService.listImages(id);
	}
	
	// Write: JWT required by SecurityConfig + service enforces SELLER/ADMIN + ownership
	@PostMapping(RestApis.Product.IMAGES)
	@ResponseStatus(HttpStatus.CREATED)
	public ProductImageResponse add(@PathVariable Long id, @Valid @RequestBody AddProductImageRequest request) {
		return productImageService.addImage(id, request);
	}
	
	@DeleteMapping(RestApis.Product.IMAGE_ID)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable Long id, @PathVariable Long imageId) {
		productImageService.removeImage(id, imageId);
	}
}