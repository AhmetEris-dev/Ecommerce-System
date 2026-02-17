package com.ahmete.authservice.constants;

public final class RestApis {
	
	private RestApis() {}
	
	public static final String AUTH = "/auth";
	
	public static final class Auth {
		private Auth() {}
		public static final String ROOT = AUTH;
		public static final String LOGIN = "/login";
		public static final String REFRESH = "/refresh";
		public static final String LOGOUT = "/logout";
	}
}