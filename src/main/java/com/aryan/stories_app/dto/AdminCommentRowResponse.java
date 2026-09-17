package com.abhishek.stories_app.dto;

public record AdminCommentRowResponse(
		String id,
		String content,
		String authorName,
		String storyId,
		String storyTitle) {}
