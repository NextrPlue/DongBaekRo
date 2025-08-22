package com.redstonetorch.dongbaekro.auth.dto.request;

import com.redstonetorch.dongbaekro.common.annotation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank
	@Email
	String email,

	@NotBlank
	@ValidPassword
	String password
) {
}
