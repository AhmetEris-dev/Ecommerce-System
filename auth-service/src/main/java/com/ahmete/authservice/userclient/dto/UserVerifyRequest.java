package com.ahmete.authservice.userclient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserVerifyRequest(
		@NotBlank @Email String email,
		@NotBlank String password
) {
}