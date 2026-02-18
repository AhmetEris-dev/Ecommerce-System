package com.ahmete.userservice.controller;

import com.ahmete.userservice.constants.RestApis;
import com.ahmete.userservice.dto.request.VerifyUserRequest;
import com.ahmete.userservice.dto.response.VerifyUserResponse;
import com.ahmete.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RestApis.InternalUsers.ROOT)
public class InternalUserController {
	
	private final UserService userService;
	
	public InternalUserController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping(RestApis.InternalUsers.VERIFY)
	public VerifyUserResponse verify(@Valid @RequestBody VerifyUserRequest request) {
		return userService.verifyForAuth(request);
	}
}