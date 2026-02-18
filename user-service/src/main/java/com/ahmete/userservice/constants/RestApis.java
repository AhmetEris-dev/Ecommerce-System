package com.ahmete.userservice.constants;

public final class RestApis {
	
	private RestApis() {}
	
	public static final String USERS = "/users";
	public static final String COMPANIES = "/companies";
	public static final String INTERNAL = "/internal";
	
	public static final class Users {
		private Users() {}
		public static final String ROOT = USERS;
		public static final String REGISTER = "/register";
		public static final String REGISTER_SELLER = "/register-seller";
		public static final String ME = "/me";
		public static final String STATUS = "/{id}/status";
		public static final String ID = "/{id}";
	}
	
	public static final class Companies {
		private Companies() {}
		public static final String ROOT = COMPANIES;
		public static final String ID = "/{id}";
	}
	
	public static final class InternalUsers {
		private InternalUsers() {}
		public static final String ROOT = INTERNAL + "/users";
		public static final String VERIFY = "/verify";
	}
}