package com.abhishek.stories_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentResponse(
		String id,
		String content,
		String storyId,
		String userId,
		StoryAuthorResponse user,
		String createdAt) {}
