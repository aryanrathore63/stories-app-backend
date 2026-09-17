package com.abhishek.stories_app.service;

import com.abhishek.stories_app.dto.StoryResponse;
import com.abhishek.stories_app.dto.UpdateProfileRequest;
import com.abhishek.stories_app.dto.UserResponse;
import com.abhishek.stories_app.exception.BadRequestException;
import com.abhishek.stories_app.mapper.StoryMapper;
import com.abhishek.stories_app.mapper.UserMapper;
import com.abhishek.stories_app.model.User;
import com.abhishek.stories_app.model.Bookmark;
import com.abhishek.stories_app.repository.BookmarkRepository;
import com.abhishek.stories_app.repository.CommentRepository;
import com.abhishek.stories_app.repository.LikeRepository;
import com.abhishek.stories_app.repository.UserRepository;
import com.abhishek.stories_app.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final BookmarkRepository bookmarkRepository;
	private final LikeRepository likeRepository;
	private final CommentRepository commentRepository;
	private final SecurityUtils securityUtils;

	@Transactional(readOnly = true)
	public UserResponse getMe() {
		return UserMapper.toResponse(securityUtils.currentUser());
	}

	@Transactional
	public UserResponse updateMe(UpdateProfileRequest req) {
		if (req.name() == null && req.username() == null) {
			throw new BadRequestException("Nothing to update");
		}
		User user = securityUtils.currentUser();
		if (req.name() != null && !req.name().isBlank()) {
			user.setName(req.name().trim());
		}
		if (req.username() != null && !req.username().isBlank()) {
			String u = req.username().trim();
			if (!u.equalsIgnoreCase(user.getUsername())
					&& userRepository.existsByUsernameIgnoreCase(u)) {
				throw new BadRequestException("Username already taken");
			}
			user.setUsername(u);
		}
		user = userRepository.save(user);
		return UserMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public List<StoryResponse> myBookmarks() {
		User user = securityUtils.currentUser();
		return bookmarkRepository.findForUserWithStoryAndAuthor(user).stream()
				.map(Bookmark::getStory)
				.map(
						s ->
								StoryMapper.listItem(
										s,
										likeRepository.countByStory(s),
										likeRepository.existsByUserAndStory(user, s),
										commentRepository.countByStory(s)))
				.toList();
	}
}
