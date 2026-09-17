package com.abhishek.stories_app.mapper;

import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.model.User;

public final class UserMapper {

	private UserMapper() {}

	public static UserResponse toResponse(User user) {
		return new UserResponse(
				String.valueOf(user.getId()),
				user.getEmail(),
				user.getName(),
				user.getRole().name(),
				user.getUsername());
	}
}
