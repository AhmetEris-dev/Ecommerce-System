package com.ahmete.productservice.service;

import com.ahmete.productservice.dto.request.AddProductImageRequest;
import com.ahmete.productservice.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {
	
	List<ProductImageResponse> listImages(Long productId);
	
	ProductImageResponse addImage(Long productId, AddProductImageRequest request);
	
	void removeImage(Long productId, Long imageId);
}