package com.redstonetorch.dongbaekro.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// Global
	GLOBAL_VALIDATION_FAILED("GLOBAL_0001", "입력 값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_ACCESS_DENIED("GLOBAL_0002", "해당 리소스에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	GLOBAL_INVALID_REQUEST_FORMAT("GLOBAL_0003", "요청 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_UNSUPPORTED_MEDIA_TYPE("GLOBAL_0004", "지원되지 않는 미디어 타입입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
	GLOBAL_METHOD_NOT_ALLOWED("GLOBAL_0005", "지원되지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
	GLOBAL_MISSING_PARAMETER("GLOBAL_0006", "필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_MISSING_HEADER("GLOBAL_0007", "필수 요청 헤더가 누락되었습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_INTERNAL_SERVER_ERROR("GLOBAL_0008", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// Auth - Authentication & Authorization (401)
	AUTH_INVALID_EMAIL("AUTH_1001", "잘못된 이메일입니다.", HttpStatus.UNAUTHORIZED),
	AUTH_INVALID_PASSWORD("AUTH_1002", "잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),
	AUTH_INVALID_API_KEY("AUTH_1003", "유효하지 않은 API Key입니다.", HttpStatus.UNAUTHORIZED),
	AUTH_INVALID_REFRESH_TOKEN("AUTH_1004", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
	AUTH_EXPIRED_REFRESH_TOKEN("AUTH_1005", "만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),

	// Auth - Bad Request (400)
	AUTH_EMAIL_ALREADY_EXISTS("AUTH_1006", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
	AUTH_CURRENT_PASSWORD_MISMATCH("AUTH_1007", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

	// Auth - Not Found (404)
	AUTH_USER_NOT_FOUND("AUTH_1008", "해당하는 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

	private final String code;
	private final String message;
	private final HttpStatus status;
}