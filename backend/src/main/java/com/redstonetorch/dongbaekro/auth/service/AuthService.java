package com.redstonetorch.dongbaekro.auth.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redstonetorch.dongbaekro.auth.dto.request.LoginRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.LogoutRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.SignUpRequest;
import com.redstonetorch.dongbaekro.auth.dto.request.TokenRefreshRequest;
import com.redstonetorch.dongbaekro.auth.dto.response.LoginResponse;
import com.redstonetorch.dongbaekro.auth.dto.response.SignUpResponse;
import com.redstonetorch.dongbaekro.auth.dto.response.TokenRefreshResponse;
import com.redstonetorch.dongbaekro.auth.entity.RefreshToken;
import com.redstonetorch.dongbaekro.auth.entity.User;
import com.redstonetorch.dongbaekro.auth.jwt.JwtTokenProvider;
import com.redstonetorch.dongbaekro.auth.repository.RefreshTokenRepository;
import com.redstonetorch.dongbaekro.auth.repository.UserRepository;
import com.redstonetorch.dongbaekro.common.dto.response.SimpleResponse;
import com.redstonetorch.dongbaekro.common.exception.CustomException;
import com.redstonetorch.dongbaekro.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final int MAX_ACTIVE_TOKENS = 5; // 최대 5개 동시 로그인 허용

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
		}

		User user = request.toEntity(passwordEncoder);
		userRepository.save(user);

		return new SignUpResponse(user);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_EMAIL));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
		}

		String accessToken = jwtTokenProvider.createToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user);

		List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevoked(user, false);

		if (activeTokens.size() >= MAX_ACTIVE_TOKENS) {
			activeTokens.stream()
				.min(Comparator.comparing(RefreshToken::getCreatedAt))
				.ifPresent(RefreshToken::revokeRefreshToken);
		}
		
		refreshTokenRepository.save(RefreshToken.builder().user(user).refreshToken(refreshToken).build());

		return new LoginResponse(accessToken, refreshToken);
	}

	@Transactional
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		RefreshToken refreshTokenEntity = refreshTokenRepository.findByRefreshTokenAndRevoked(request.refreshToken(),
				false)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

		if (!jwtTokenProvider.validateToken(request.refreshToken())) {
			refreshTokenEntity.revokeRefreshToken();
			throw new CustomException(ErrorCode.AUTH_EXPIRED_REFRESH_TOKEN);
		}

		User user = refreshTokenEntity.getUser();

		String newAccessToken = jwtTokenProvider.createToken(user);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

		refreshTokenEntity.updateRefreshToken(newRefreshToken);

		return new TokenRefreshResponse(newAccessToken, newRefreshToken);
	}

	@Transactional
	public SimpleResponse logout(LogoutRequest request) {
		RefreshToken refreshToken = refreshTokenRepository.findByRefreshTokenAndRevoked(request.refreshToken(), false)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

		refreshToken.revokeRefreshToken();

		return new SimpleResponse("로그아웃에 성공하였습니다.");
	}
}
