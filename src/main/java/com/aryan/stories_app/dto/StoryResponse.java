package com.abhishek.stories_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoryResponse(
		String id,
		String title,
		String content,
		String excerpt,
		String authorId,
		StoryAuthorResponse author,
		String status,
		Long likesCount,
		Boolean likedByMe,
		Long commentCount,
		Long viewCount,
		String createdAt,
		String updatedAt,
		String bgimg,
		List<CommentResponse> comments,
		Long commentsTotal,
		Integer commentsPage,
		Integer commentsPageSize) {

	public StoryResponse withoutComments() {
		return new StoryResponse(
				id,
				title,
				content,
				excerpt,
				authorId,
				author,
				status,
				likesCount,
				likedByMe,
				commentCount,
				viewCount,
				createdAt,
				updatedAt,
				bgimg,
				null,
				null,
				null,
				null);
	}
}
