package com.abhishek.stories_app.controller;

import com.abhishek.stories_app.dto.AdminCommentRowResponse;
import com.abhishek.stories_app.dto.AdminStatsResponse;
import com.abhishek.stories_app.dto.AdminStoryRowResponse;
import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	private final AdminService adminService;

	@GetMapping("/users")
	@Operation(summary = "List all users")
	public List<UserResponse> users() {
		return adminService.listUsers();
	}

	@GetMapping("/stats")
	@Operation(summary = "Platform statistics")
	public AdminStatsResponse stats() {
		return adminService.stats();
	}

	@GetMapping("/stories")
	@Operation(summary = "List all stories for moderation")
	public List<AdminStoryRowResponse> moderationStories() {
		return adminService.listStoriesForModeration();
	}

	@GetMapping("/comments")
	@Operation(summary = "List all comments for moderation")
	public List<AdminCommentRowResponse> moderationComments() {
		return adminService.listCommentsForModeration();
	}

	@DeleteMapping("/stories/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete any story")
	public void deleteStory(@PathVariable String id) {
		adminService.deleteStory(id);
	}

	@DeleteMapping("/comments/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete any comment")
	public void deleteComment(@PathVariable Long id) {
		adminService.deleteComment(id);
	}
}
