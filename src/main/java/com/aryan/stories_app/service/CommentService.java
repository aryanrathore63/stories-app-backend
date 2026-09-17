package com.abhishek.stories_app.service;

import com.abhishek.stories_app.dto.CommentRequest;
import com.abhishek.stories_app.dto.CommentResponse;
import com.abhishek.stories_app.dto.PaginatedCommentsResponse;
import com.abhishek.stories_app.exception.BadRequestException;
import com.abhishek.stories_app.exception.ResourceNotFoundException;
import com.abhishek.stories_app.exception.UnauthorizedException;
import com.abhishek.stories_app.mapper.StoryMapper;
import com.abhishek.stories_app.model.Comment;
import com.abhishek.stories_app.model.Role;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.StoryStatus;
import com.abhishek.stories_app.model.User;
import com.abhishek.stories_app.repository.CommentRepository;
import com.abhishek.stories_app.repository.StoryRepository;
import com.abhishek.stories_app.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final StoryRepository storyRepository;
	private final SecurityUtils securityUtils;

	@Transactional(readOnly = true)
	public PaginatedCommentsResponse listForStory(Long storyId, int page, int pageSize) {
		Story story = loadStoryForPublicComments(storyId);
		var pg = PageRequest.of(ApiLimits.page(page) - 1, ApiLimits.pageSize(pageSize, ApiLimits.MAX_COMMENT_PAGE_SIZE));
		Page<Comment> result = commentRepository.findByStoryOrderByCreatedAtAsc(story, pg);
		var items = result.getContent().stream().map(StoryMapper::toComment).toList();
		return new PaginatedCommentsResponse(
				items,
				result.getTotalElements(),
				page,
				pageSize,
				result.getTotalPages());
	}

	@Transactional
	public CommentResponse add(Long storyId, CommentRequest req) {
		User user = securityUtils.currentUser();
		Story story = loadStoryForPublicComments(storyId);
		if (story.getStatus() != StoryStatus.PUBLISHED) {
			throw new BadRequestException("Comments are only allowed on published stories");
		}
		Comment c =
				Comment.builder()
						.content(req.content().trim())
						.user(user)
						.story(story)
						.build();
		c = commentRepository.save(c);
		return StoryMapper.toComment(c);
	}

	@Transactional
	public void delete(Long commentId) {
		User user = securityUtils.currentUser();
		Comment c =
				commentRepository
						.findById(commentId)
						.orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
		boolean owner = c.getUser().getId().equals(user.getId());
		boolean admin = user.getRole() == Role.ADMIN;
		if (!owner && !admin) {
			throw new UnauthorizedException("You cannot delete this comment");
		}
		commentRepository.delete(c);
	}

	@Transactional
	public void adminDelete(Long commentId) {
		Comment c =
				commentRepository
						.findById(commentId)
						.orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
		commentRepository.delete(c);
	}

	private Story loadStoryForPublicComments(Long storyId) {
		Story story =
				storyRepository
						.findDetailById(storyId)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		if (story.getStatus() != StoryStatus.PUBLISHED) {
			User current = securityUtils.currentUserOrNull();
			boolean allowed =
					current != null
							&& (story.getAuthor().getId().equals(current.getId())
									|| current.getRole() == Role.ADMIN);
			if (!allowed) {
				throw new ResourceNotFoundException("Story not found");
			}
		}
		return story;
	}
}
