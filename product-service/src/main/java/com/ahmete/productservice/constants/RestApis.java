package com.ahmete.productservice.constants;

public final class RestApis {
	
	private RestApis() {}
	
	public static final String PRODUCTS = "/products";
	
	public static final class Product {
		private Product() {}
		
		public static final String ROOT = PRODUCTS;
		public static final String ID = "/{id}";
		public static final String STATUS = "/{id}/status";
		
		// Images (optional feature implemented)
		public static final String IMAGES = "/{id}/images";
		public static final String IMAGE_ID = "/{id}/images/{imageId}";
	}
}