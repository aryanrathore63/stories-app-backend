package com.abhishek.stories_app.controller;

import com.abhishek.stories_app.dto.StoryResponse;
import com.abhishek.stories_app.dto.UpdateProfileRequest;
import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	@Operation(summary = "Current user profile (alias)")
	public UserResponse me() {
		return userService.getMe();
	}

	@PatchMapping("/me")
	@Operation(summary = "Update display name and/or username")
	public UserResponse patchMe(@Valid @RequestBody UpdateProfileRequest req) {
		return userService.updateMe(req);
	}

	@GetMapping("/me/bookmarks")
	@Operation(summary = "Stories bookmarked by the current user")
	public List<StoryResponse> bookmarks() {
		return userService.myBookmarks();
	}
}
