package com.ahmete.authservice.userclient;

import com.ahmete.authservice.userclient.config.UserServiceProperties;
import com.ahmete.authservice.userclient.dto.UserVerifyRequest;
import com.ahmete.authservice.userclient.dto.UserVerifyResponse;
import com.ahmete.authservice.userclient.exception.UserServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {
	
	private final RestClient restClient;
	private final UserServiceProperties props;
	
	public UserServiceClient(RestClient restClient, UserServiceProperties props) {
		this.restClient = restClient;
		this.props = props;
	}
	
	public UserVerifyResponse verify(String email, String password) {
		UserVerifyRequest req = new UserVerifyRequest(email, password);
		
		try {
			return restClient
					.post()
					.uri(props.baseUrl() + "/internal/users/verify")
					.contentType(MediaType.APPLICATION_JSON)
					.body(req)
					.retrieve()
					.onStatus(
							status -> status.value() == 404,
							(request, response) -> { throw new UserServiceException(HttpStatus.NOT_FOUND, "Email not found"); }
					)
					.onStatus(
							status -> status.value() == 401,
							(request, response) -> { throw new UserServiceException(HttpStatus.UNAUTHORIZED, "Wrong password"); }
					)
					.onStatus(
							status -> status.value() == 403,
							(request, response) -> { throw new UserServiceException(HttpStatus.FORBIDDEN, "User status is PASSIVE"); }
					)
					.onStatus(
							status -> status.isError(),
							(request, response) -> { throw new UserServiceException(HttpStatus.BAD_GATEWAY, "User-service error"); }
					)
					.body(UserVerifyResponse.class);
		} catch (UserServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new UserServiceException(HttpStatus.BAD_GATEWAY, "Cannot reach user-service");
		}
	}
}