package com.redstonetorch.dongbaekro.auth.dto.request;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.redstonetorch.dongbaekro.common.annotation.ValidPassword;
import com.redstonetorch.dongbaekro.common.annotation.ValidPhone;
import com.redstonetorch.dongbaekro.auth.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
	@NotBlank
	@Size(min = 2, max = 20)
	String name,

	@NotBlank
	@Email
	String email,

	@NotBlank
	@ValidPassword
	String password,

	@NotBlank
	@ValidPhone
	String phone
) {
	public User toEntity(PasswordEncoder passwordEncoder) {
		return User.builder()
			.name(name)
			.email(email)
			.password(passwordEncoder.encode(password))
			.phone(phone)
			.build();
	}
}
