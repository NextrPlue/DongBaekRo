package com.redstonetorch.dongbaekro.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
	@NotBlank
	String refreshToken
) {
}