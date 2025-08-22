package com.redstonetorch.dongbaekro.auth.dto.response;

import com.redstonetorch.dongbaekro.auth.entity.User;

public record SignUpResponse(
	String name,
	String email,
	String phone
) {
	public SignUpResponse(User user) {
		this(user.getName(), user.getEmail(), user.getPhone());
	}
}
