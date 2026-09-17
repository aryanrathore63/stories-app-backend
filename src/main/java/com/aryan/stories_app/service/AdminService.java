package com.abhishek.stories_app.service;

import com.abhishek.stories_app.dto.AdminCommentRowResponse;
import com.abhishek.stories_app.dto.AdminStatsResponse;
import com.abhishek.stories_app.dto.AdminStoryRowResponse;
import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.mapper.UserMapper;
import com.abhishek.stories_app.model.Comment;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.repository.CommentRepository;
import com.abhishek.stories_app.repository.StoryRepository;
import com.abhishek.stories_app.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final StoryRepository storyRepository;
	private final CommentRepository commentRepository;
	private final StoryService storyService;
	private final CommentService commentService;

	@Transactional(readOnly = true)
	public List<UserResponse> listUsers() {
		return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AdminStatsResponse stats() {
		long users = userRepository.count();
		long posts = storyRepository.count();
		long comments = commentRepository.count();
		return new AdminStatsResponse(users, posts, comments);
	}

	private static final int STORY_PREVIEW_MAX = 120;

	private static String previewText(String content, int max) {
		if (content == null || content.isBlank()) {
			return "";
		}
		String t = content.strip().replaceAll("\\s+", " ");
		if (t.length() <= max) {
			return t;
		}
		return t.substring(0, max) + "…";
	}

	@Transactional(readOnly = true)
	public List<AdminStoryRowResponse> listStoriesForModeration() {
		return storyRepository.findAllForAdminModeration().stream()
				.map(
						s ->
								new AdminStoryRowResponse(
										String.valueOf(s.getId()),
										s.getTitle(),
										previewText(s.getContent(), STORY_PREVIEW_MAX),
										s.getAuthor().getName()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminCommentRowResponse> listCommentsForModeration() {
		return commentRepository.findAllForAdminModeration().stream()
				.map(AdminService::toAdminCommentRow)
				.toList();
	}

	private static AdminCommentRowResponse toAdminCommentRow(Comment c) {
		Story story = c.getStory();
		return new AdminCommentRowResponse(
				String.valueOf(c.getId()),
				c.getContent(),
				c.getUser().getName(),
				String.valueOf(story.getId()),
				story.getTitle());
	}

	@Transactional
	public void deleteStory(String id) {
		storyService.adminDeleteStory(id);
	}

	@Transactional
	public void deleteComment(Long id) {
		commentService.adminDelete(id);
	}
}
