package com.ahmete.orderservice.constants;

public final class RestApis {
	
	private RestApis() {}
	
	public static final String ORDERS = "/orders";
	
	public static final class Order {
		private Order() {}
		
		public static final String ROOT = ORDERS;
		
		public static final String ME = "/me";
		public static final String ME_BY_ID = "/me/{orderId}";
		
		public static final String SELLER = "/seller";
		
		public static final String BY_ID = "/{orderId}";
		public static final String CANCEL = "/{orderId}/cancel";
	}
}