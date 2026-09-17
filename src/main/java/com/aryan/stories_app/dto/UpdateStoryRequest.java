package com.abhishek.stories_app.dto;

import com.abhishek.stories_app.model.StoryStatus;
import jakarta.validation.constraints.Size;

public record UpdateStoryRequest(
		@Size(max = 500) String title,
		@Size(max = 100_000) String content,
		StoryStatus status,
		@Size(max = 2048) String bgimg) {}
