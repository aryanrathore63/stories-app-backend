package com.abhishek.stories_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DraftSaveRequest(
		@NotBlank @Size(max = 500) String title,
		@Size(max = 100_000) String content,
		@Size(max = 2048) String bgimg) {}
