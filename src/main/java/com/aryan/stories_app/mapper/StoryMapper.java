package com.abhishek.stories_app.mapper;

import com.abhishek.stories_app.dto.CommentResponse;
import com.abhishek.stories_app.dto.StoryAuthorResponse;
import com.abhishek.stories_app.dto.StoryResponse;
import com.abhishek.stories_app.model.Comment;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.User;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class StoryMapper {

	private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

	private StoryMapper() {}

	public static String formatInstant(Instant i) {
		return i == null ? null : ISO.format(i);
	}

	public static StoryAuthorResponse authorSummary(User author) {
		if (author == null) {
			return null;
		}
		return new StoryAuthorResponse(String.valueOf(author.getId()), author.getName());
	}

	public static CommentResponse toComment(Comment c) {
		User u = c.getUser();
		return new CommentResponse(
				String.valueOf(c.getId()),
				c.getContent(),
				String.valueOf(c.getStory().getId()),
				String.valueOf(u.getId()),
				new StoryAuthorResponse(String.valueOf(u.getId()), u.getName()),
				formatInstant(c.getCreatedAt()));
	}

	public static StoryResponse toStory(
			Story s,
			Long likesCount,
			Boolean likedByMe,
			Long commentCount,
			List<CommentResponse> comments,
			Long commentsTotal,
			Integer commentsPage,
			Integer commentsPageSize) {
		User a = s.getAuthor();
		return new StoryResponse(
				String.valueOf(s.getId()),
				s.getTitle(),
				s.getContent(),
				excerpt(s.getContent()),
				a != null ? String.valueOf(a.getId()) : null,
				authorSummary(a),
				s.getStatus().name(),
				likesCount,
				likedByMe,
				commentCount,
				s.getViewCount(),
				formatInstant(s.getCreatedAt()),
				formatInstant(s.getUpdatedAt()),
				s.getBgimg(),
				comments,
				commentsTotal,
				commentsPage,
				commentsPageSize);
	}

	public static StoryResponse listItem(Story s, Long likesCount, Boolean likedByMe, Long commentCount) {
		return toStory(s, likesCount, likedByMe, commentCount, null, null, null, null);
	}

	private static String excerpt(String content) {
		if (content == null) {
			return null;
		}
		String flat = content.replaceAll("\\s+", " ").trim();
		int max = 220;
		if (flat.length() <= max) {
			return flat;
		}
		return flat.substring(0, max).trim() + "…";
	}
}
