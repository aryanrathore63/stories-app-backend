package com.abhishek.stories_app.dto;

import com.abhishek.stories_app.model.StoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Content may be empty for new drafts (matches editor bootstrap). */
public record CreateStoryRequest(
		@NotBlank @Size(max = 500) String title,
		@Size(max = 100_000) String content,
		StoryStatus status,
		@Size(max = 2048) String bgimg) {}
