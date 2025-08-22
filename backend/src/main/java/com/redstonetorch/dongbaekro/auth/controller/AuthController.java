package com.redstonetorch.dongbaekro.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redstonetorch.dongbaekro.auth.dto.request.LoginRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.LogoutRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.SignUpRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.TokenRefreshRequest;
import com.redstonetorch.dongbaekro.auth.dto.response.LoginResponse;
import com.redstonetorch.dongbaekro.auth.dto.response.SignUpResponse;
import com.redstonetorch.dongbaekro.auth.dto.response.TokenRefreshResponse;
import com.redstonetorch.dongbaekro.auth.service.AuthService;
import com.redstonetorch.dongbaekro.common.dto.response.ApiResponse;
import com.redstonetorch.dongbaekro.common.dto.response.SimpleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
		SignUpResponse response = authService.signUp(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@RequestBody TokenRefreshRequest request) {
		TokenRefreshResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<SimpleResponse>> logout(@RequestBody LogoutRequest request) {
		SimpleResponse response = authService.logout(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
