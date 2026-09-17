package com.abhishek.stories_app.controller;

import com.abhishek.stories_app.dto.AuthResponse;
import com.abhishek.stories_app.dto.LoginRequest;
import com.abhishek.stories_app.dto.RegisterRequest;
import com.abhishek.stories_app.dto.TokenRefreshResponse;
import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.exception.UnauthorizedException;
import com.abhishek.stories_app.security.RefreshTokenCookieService;
import com.abhishek.stories_app.service.AuthService;
import com.abhishek.stories_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

	private final AuthService authService;
	private final UserService userService;
	private final RefreshTokenCookieService refreshTokenCookieService;

	@PostMapping("/register")
	@Operation(summary = "Register a new user (ROLE_USER)")
	public AuthResponse register(
			@Valid @RequestBody RegisterRequest req, HttpServletResponse response) {
		var session = authService.register(req);
		setRefreshCookie(response, session.refreshTokenPlain());
		return authService.toAuthResponse(session);
	}

	@PostMapping("/login")
	@Operation(summary = "Login with email and password; returns short-lived access token")
	public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
		var session = authService.login(req);
		setRefreshCookie(response, session.refreshTokenPlain());
		return authService.toAuthResponse(session);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Rotate refresh cookie and issue a new access token")
	public TokenRefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken =
				refreshTokenCookieService
						.readRefreshToken(request)
						.orElseThrow(() -> new UnauthorizedException("Refresh token required"));
		var session = authService.refreshWithSession(refreshToken);
		setRefreshCookie(response, session.refreshTokenPlain());
		return new TokenRefreshResponse(session.accessToken());
	}

	@PostMapping("/logout")
	@Operation(summary = "Revoke refresh token and clear cookie")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		refreshTokenCookieService.readRefreshToken(request).ifPresent(authService::logout);
		refreshTokenCookieService.clearRefreshCookie(response);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@Operation(summary = "Current user profile")
	public UserResponse me() {
		return userService.getMe();
	}

	private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
		refreshTokenCookieService.setRefreshCookie(
				response, refreshToken, refreshTokenCookieService.cookieMaxAge());
	}
}
