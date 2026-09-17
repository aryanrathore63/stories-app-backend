package com.abhishek.stories_app.service;

import com.abhishek.stories_app.dto.CreateStoryRequest;
import com.abhishek.stories_app.dto.DraftSaveRequest;
import com.abhishek.stories_app.dto.NarrateRequest;
import com.abhishek.stories_app.dto.NarrateResponse;
import com.abhishek.stories_app.dto.PaginatedStoriesResponse;
import com.abhishek.stories_app.dto.StoryResponse;
import com.abhishek.stories_app.dto.UpdateStoryRequest;
import com.abhishek.stories_app.exception.BadRequestException;
import com.abhishek.stories_app.exception.ResourceNotFoundException;
import com.abhishek.stories_app.exception.UnauthorizedException;
import com.abhishek.stories_app.mapper.StoryMapper;
import com.abhishek.stories_app.model.Bookmark;
import com.abhishek.stories_app.model.Like;
import com.abhishek.stories_app.model.Role;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.StoryStatus;
import com.abhishek.stories_app.model.User;
import com.abhishek.stories_app.repository.BookmarkRepository;
import com.abhishek.stories_app.repository.CommentRepository;
import com.abhishek.stories_app.repository.LikeRepository;
import com.abhishek.stories_app.repository.StoryRepository;
import com.abhishek.stories_app.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoryService {

	private static final int DEFAULT_COMMENT_PREVIEW = 20;

	private final StoryRepository storyRepository;
	private final LikeRepository likeRepository;
	private final CommentRepository commentRepository;
	private final BookmarkRepository bookmarkRepository;
	private final SecurityUtils securityUtils;
	private final VoiceRssTextToSpeechService voiceRssTextToSpeechService;

	@Transactional(readOnly = true)
	public NarrateResponse narrate(String idRaw, NarrateRequest req) {
		long id = parseId(idRaw);
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		User viewer = securityUtils.currentUserOrNull();
		ensureReadable(story, viewer);
		return voiceRssTextToSpeechService.narrate(story.getContent(), req);
	}

	public static long parseId(String raw) {
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid story id");
		}
	}

	private static String normalizeBgimg(String raw) {
		if (raw == null) {
			return null;
		}
		String t = raw.trim();
		return t.isEmpty() ? null : t;
	}

	@Transactional
	public StoryResponse create(CreateStoryRequest req) {
		User author = securityUtils.currentUser();
		StoryStatus status = req.status() == null ? StoryStatus.DRAFT : req.status();
		String body = req.content() == null ? "" : req.content();
		Story story =
				Story.builder()
						.title(req.title().trim())
						.content(body)
						.status(status)
						.author(author)
						.viewCount(0)
						.bgimg(normalizeBgimg(req.bgimg()))
						.build();
		story = storyRepository.save(story);
		return enrich(story, author, null);
	}

	@Transactional
	public StoryResponse update(String idRaw, UpdateStoryRequest req) {
		User user = securityUtils.currentUser();
		Story story = loadOwnedOrAdmin(parseId(idRaw), user);
		if (req.title() != null && !req.title().isBlank()) {
			story.setTitle(req.title().trim());
		}
		if (req.content() != null) {
			story.setContent(req.content());
		}
		if (req.status() != null) {
			story.setStatus(req.status());
		}
		if (req.bgimg() != null) {
			story.setBgimg(normalizeBgimg(req.bgimg()));
		}
		story = storyRepository.save(story);
		return enrich(story, user, null);
	}

	@Transactional
	public void delete(String idRaw) {
		User user = securityUtils.currentUser();
		long id = parseId(idRaw);
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		boolean author = story.getAuthor().getId().equals(user.getId());
		boolean admin = user.getRole() == Role.ADMIN;
		if (!author && !admin) {
			throw new UnauthorizedException("You cannot delete this story");
		}
		storyRepository.delete(story);
	}

	@Transactional
	public StoryResponse publish(String idRaw) {
		User user = securityUtils.currentUser();
		Story story = loadOwned(parseId(idRaw), user);
		story.setStatus(StoryStatus.PUBLISHED);
		story = storyRepository.save(story);
		return enrich(story, user, null);
	}

	@Transactional
	public StoryResponse saveDraft(String idRaw, DraftSaveRequest req) {
		User user = securityUtils.currentUser();
		Story story = loadOwned(parseId(idRaw), user);
		if (story.getStatus() != StoryStatus.DRAFT) {
			throw new BadRequestException("Auto-save is only available for drafts");
		}
		story.setTitle(req.title().trim());
		story.setContent(req.content() == null ? "" : req.content());
		if (req.bgimg() != null) {
			story.setBgimg(normalizeBgimg(req.bgimg()));
		}
		story = storyRepository.save(story);
		return enrich(story, user, null);
	}

	@Transactional(readOnly = true)
	public PaginatedStoriesResponse listPublished(int page, int pageSize, String search) {
		var pg =
				PageRequest.of(
						ApiLimits.page(page) - 1,
						ApiLimits.pageSize(pageSize, ApiLimits.MAX_STORY_PAGE_SIZE),
						Sort.by(Sort.Direction.DESC, "updatedAt"));
		Page<Story> result;
		if (search != null && !search.isBlank()) {
			result =
					storyRepository.findByStatusAndTitleContainingIgnoreCase(
							StoryStatus.PUBLISHED, search.trim(), pg);
		} else {
			result = storyRepository.findByStatus(StoryStatus.PUBLISHED, pg);
		}
		User viewer = securityUtils.currentUserOrNull();
		var items =
				result.getContent().stream()
						.map(s -> enrichListItem(s, viewer))
						.map(StoryResponse::withoutComments)
						.toList();
		return new PaginatedStoriesResponse(
				items,
				result.getTotalElements(),
				page,
				pageSize,
				result.getTotalPages());
	}

	@Transactional(readOnly = true)
	public PaginatedStoriesResponse trending(int page, int pageSize) {
		var pg = PageRequest.of(ApiLimits.page(page) - 1, ApiLimits.pageSize(pageSize, ApiLimits.MAX_STORY_PAGE_SIZE));
		Page<Story> result = storyRepository.findTrending(StoryStatus.PUBLISHED, pg);
		User viewer = securityUtils.currentUserOrNull();
		var items =
				result.getContent().stream()
						.map(s -> enrichListItem(s, viewer))
						.map(StoryResponse::withoutComments)
						.toList();
		return new PaginatedStoriesResponse(
				items,
				result.getTotalElements(),
				page,
				pageSize,
				result.getTotalPages());
	}

	@Transactional
	public StoryResponse getById(String idRaw, Integer commentPage, Integer commentPageSize) {
		long id = parseId(idRaw);
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		User viewer = securityUtils.currentUserOrNull();
		ensureReadable(story, viewer);
		if (story.getStatus() == StoryStatus.PUBLISHED) {
			story.setViewCount(story.getViewCount() + 1);
			story = storyRepository.save(story);
		}
		int cp = commentPage == null ? 1 : commentPage;
		int cps = commentPageSize == null ? DEFAULT_COMMENT_PREVIEW : commentPageSize;
		return enrich(story, viewer, commentPreview(story, cp, cps));
	}

	@Transactional(readOnly = true)
	public PaginatedStoriesResponse myStories(
			StoryStatus statusFilter, int page, int pageSize) {
		User user = securityUtils.currentUser();
		var pg = PageRequest.of(ApiLimits.page(page) - 1, ApiLimits.pageSize(pageSize, ApiLimits.MAX_STORY_PAGE_SIZE));
		Page<Story> result;
		if (statusFilter != null) {
			result = storyRepository.findByAuthorAndStatus(user, statusFilter, pg);
		} else {
			result = storyRepository.findByAuthor(user, pg);
		}
		var items =
				result.getContent().stream()
						.map(s -> enrichListItem(s, user))
						.map(StoryResponse::withoutComments)
						.toList();
		return new PaginatedStoriesResponse(
				items,
				result.getTotalElements(),
				page,
				pageSize,
				result.getTotalPages());
	}

	@Transactional
	public StoryResponse like(String idRaw) {
		User user = securityUtils.currentUser();
		Story story = loadPublished(parseId(idRaw));
		if (likeRepository.existsByUserAndStory(user, story)) {
			throw new BadRequestException("Already liked");
		}
		try {
			likeRepository.save(Like.builder().user(user).story(story).build());
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Already liked");
		}
		return enrich(storyRepository.findDetailById(story.getId()).orElse(story), user, null);
	}

	@Transactional
	public StoryResponse unlike(String idRaw) {
		User user = securityUtils.currentUser();
		Story story = loadPublished(parseId(idRaw));
		likeRepository.deleteByUserAndStory(user, story);
		return enrich(storyRepository.findDetailById(story.getId()).orElse(story), user, null);
	}

	@Transactional
	public void bookmark(String idRaw) {
		User user = securityUtils.currentUser();
		Story story = loadPublished(parseId(idRaw));
		if (bookmarkRepository.existsByUserAndStory(user, story)) {
			return;
		}
		try {
			bookmarkRepository.save(Bookmark.builder().user(user).story(story).build());
		} catch (DataIntegrityViolationException ignored) {
			// idempotent
		}
	}

	@Transactional
	public void unbookmark(String idRaw) {
		User user = securityUtils.currentUser();
		Story story = loadPublished(parseId(idRaw));
		bookmarkRepository.deleteByUserAndStory(user, story);
	}

	@Transactional
	public void adminDeleteStory(String idRaw) {
		long id = parseId(idRaw);
		Story story =
				storyRepository
						.findById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		storyRepository.delete(story);
	}

	private CommentPreview commentPreview(Story story, int page, int pageSize) {
		var pg = PageRequest.of(ApiLimits.page(page) - 1, ApiLimits.pageSize(pageSize, ApiLimits.MAX_COMMENT_PAGE_SIZE));
		var slice = commentRepository.findByStoryOrderByCreatedAtAsc(story, pg);
		List<com.abhishek.stories_app.dto.CommentResponse> items =
				slice.getContent().stream().map(StoryMapper::toComment).toList();
		return new CommentPreview(
				items, slice.getTotalElements(), page, pageSize);
	}

	private StoryResponse enrich(Story story, User viewer, CommentPreview comments) {
		long likes = likeRepository.countByStory(story);
		Boolean liked =
				viewer != null ? likeRepository.existsByUserAndStory(viewer, story) : Boolean.FALSE;
		long commentCount = commentRepository.countByStory(story);
		if (comments == null) {
			return StoryMapper.toStory(
					story, likes, liked, commentCount, null, null, null, null);
		}
		return StoryMapper.toStory(
				story,
				likes,
				liked,
				commentCount,
				comments.items(),
				comments.total(),
				comments.page(),
				comments.pageSize());
	}

	private StoryResponse enrichListItem(Story story, User viewer) {
		long likes = likeRepository.countByStory(story);
		Boolean liked =
				viewer != null ? likeRepository.existsByUserAndStory(viewer, story) : Boolean.FALSE;
		long commentCount = commentRepository.countByStory(story);
		return StoryMapper.listItem(story, likes, liked, commentCount);
	}

	private void ensureReadable(Story story, User viewer) {
		if (story.getStatus() == StoryStatus.PUBLISHED) {
			return;
		}
		if (viewer == null) {
			throw new ResourceNotFoundException("Story not found");
		}
		boolean author = story.getAuthor().getId().equals(viewer.getId());
		boolean admin = viewer.getRole() == Role.ADMIN;
		if (!author && !admin) {
			throw new ResourceNotFoundException("Story not found");
		}
	}

	private Story loadOwned(long id, User user) {
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		if (!story.getAuthor().getId().equals(user.getId())) {
			throw new UnauthorizedException("You can only modify your own stories");
		}
		return story;
	}

	private Story loadOwnedOrAdmin(long id, User user) {
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		boolean author = story.getAuthor().getId().equals(user.getId());
		boolean admin = user.getRole() == Role.ADMIN;
		if (!author && !admin) {
			throw new UnauthorizedException("You can only modify your own stories");
		}
		return story;
	}

	private Story loadPublished(long id) {
		Story story =
				storyRepository
						.findDetailById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Story not found"));
		if (story.getStatus() != StoryStatus.PUBLISHED) {
			throw new BadRequestException("Story is not published");
		}
		return story;
	}

	private record CommentPreview(
			List<com.abhishek.stories_app.dto.CommentResponse> items,
			long total,
			int page,
			int pageSize) {}
}
