package com.abhishek.stories_app.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@Size(max = 120) String name,
		@Size(min = 3, max = 100) String username) {}
