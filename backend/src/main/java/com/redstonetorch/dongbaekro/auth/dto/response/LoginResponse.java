package com.redstonetorch.dongbaekro.auth.dto.response;

public record LoginResponse(
	String accessToken,
	String refreshToken
) {
}
