package com.abhishek.stories_app.controller;

import com.abhishek.stories_app.dto.BookmarkActionResponse;
import com.abhishek.stories_app.dto.CreateStoryRequest;
import com.abhishek.stories_app.dto.DraftSaveRequest;
import com.abhishek.stories_app.dto.NarrateRequest;
import com.abhishek.stories_app.dto.NarrateResponse;
import com.abhishek.stories_app.dto.PaginatedCommentsResponse;
import com.abhishek.stories_app.dto.PaginatedStoriesResponse;
import com.abhishek.stories_app.dto.StoryResponse;
import com.abhishek.stories_app.dto.UpdateStoryRequest;
import com.abhishek.stories_app.model.StoryStatus;
import com.abhishek.stories_app.service.CommentService;
import com.abhishek.stories_app.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
@Tag(name = "Stories")
public class StoryController {

	private final StoryService storyService;
	private final CommentService commentService;

	@GetMapping("/me")
	@Operation(summary = "Stories authored by the current user")
	public PaginatedStoriesResponse myStories(
			@RequestParam(required = false) StoryStatus status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "50") int pageSize) {
		return storyService.myStories(status, page, pageSize);
	}

	@GetMapping("/trending")
	@Operation(summary = "Published stories ordered by likes (then recency)")
	public PaginatedStoriesResponse trending(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return storyService.trending(page, pageSize);
	}

	@GetMapping
	@Operation(summary = "Published stories with optional title search")
	public PaginatedStoriesResponse listPublished(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize,
			@RequestParam(required = false) String search) {
		return storyService.listPublished(page, pageSize, search);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Story by id (increments views when published)")
	public StoryResponse get(
			@PathVariable String id,
			@RequestParam(required = false) Integer commentPage,
			@RequestParam(required = false) Integer commentPageSize) {
		return storyService.getById(id, commentPage, commentPageSize);
	}

	@GetMapping("/{id}/comments")
	@Operation(summary = "Paginated comments for a story")
	public PaginatedCommentsResponse listComments(
			@PathVariable Long id,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int pageSize) {
		return commentService.listForStory(id, page, pageSize);
	}

	@PostMapping("/{id}/narrate")
	@Operation(summary = "Narrate a published story using VoiceRSS")
	public NarrateResponse narrate(
			@PathVariable String id, @RequestBody(required = false) NarrateRequest req) {
		return storyService.narrate(id, req);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a story (defaults to DRAFT)")
	public StoryResponse create(@Valid @RequestBody CreateStoryRequest req) {
		return storyService.create(req);
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Update a story (author or admin)")
	public StoryResponse patch(
			@PathVariable String id, @Valid @RequestBody UpdateStoryRequest req) {
		return storyService.update(id, req);
	}

	@PatchMapping("/{id}/draft")
	@Operation(summary = "Auto-save draft (draft status only)")
	public StoryResponse saveDraft(
			@PathVariable String id, @Valid @RequestBody DraftSaveRequest req) {
		return storyService.saveDraft(id, req);
	}

	@PostMapping("/{id}/publish")
	@Operation(summary = "Publish a story")
	public StoryResponse publish(@PathVariable String id) {
		return storyService.publish(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete story (author or admin)")
	public void delete(@PathVariable String id) {
		storyService.delete(id);
	}

	@PostMapping("/{id}/like")
	@Operation(summary = "Like a published story")
	public StoryResponse like(@PathVariable String id) {
		return storyService.like(id);
	}

	@DeleteMapping("/{id}/like")
	@Operation(summary = "Remove like")
	public StoryResponse unlike(@PathVariable String id) {
		return storyService.unlike(id);
	}

	@PostMapping("/{id}/bookmark")
	@Operation(summary = "Bookmark a published story")
	public BookmarkActionResponse bookmark(@PathVariable String id) {
		storyService.bookmark(id);
		return new BookmarkActionResponse(true);
	}

	@DeleteMapping("/{id}/bookmark")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove bookmark")
	public void unbookmark(@PathVariable String id) {
		storyService.unbookmark(id);
	}
}
