package com.abhishek.stories_app.dto;

import java.util.List;

public record PaginatedStoriesResponse(
		List<StoryResponse> items,
		long total,
		int page,
		int pageSize,
		int totalPages) {}
