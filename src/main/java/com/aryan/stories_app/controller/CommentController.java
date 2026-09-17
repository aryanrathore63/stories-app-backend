package com.abhishek.stories_app.controller;

import com.abhishek.stories_app.dto.CommentRequest;
import com.abhishek.stories_app.dto.CommentResponse;
import com.abhishek.stories_app.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
@Tag(name = "Comments (nested)")
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/{storyId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add a comment to a published story")
	public CommentResponse add(
			@PathVariable Long storyId, @Valid @RequestBody CommentRequest req) {
		return commentService.add(storyId, req);
	}
}
