package com.abhishek.stories_app.dto;

import java.util.List;

public record PaginatedCommentsResponse(
		List<CommentResponse> items,
		long total,
		int page,
		int pageSize,
		int totalPages) {}
