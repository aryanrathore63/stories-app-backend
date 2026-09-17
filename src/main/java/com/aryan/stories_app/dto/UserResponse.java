package com.abhishek.stories_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
		String id,
		String email,
		String name,
		String role,
		String username) {}
