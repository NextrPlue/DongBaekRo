package com.redstonetorch.dongbaekro.auth.dto.response;

public record TokenRefreshResponse(
	String accessToken,
	String refreshToken
) {
}