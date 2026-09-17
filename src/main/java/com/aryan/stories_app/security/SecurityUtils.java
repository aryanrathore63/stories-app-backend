package com.abhishek.stories_app.security;

import com.abhishek.stories_app.exception.UnauthorizedException;
import com.abhishek.stories_app.model.User;
import com.abhishek.stories_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

	private final UserRepository userRepository;

	public User currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
			throw new UnauthorizedException("Authentication required");
		}
		return userRepository
				.findById(principal.getId())
				.orElseThrow(() -> new UnauthorizedException("Authentication required"));
	}

	@Transactional(readOnly = true)
	public User currentUserOrNull() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
			return null;
		}
		return userRepository.findById(principal.getId()).orElse(null);
	}

	public boolean isAdmin(User user) {
		return user.getRole() == com.abhishek.stories_app.model.Role.ADMIN;
	}
}
