package com.abhishek.stories_app.repository;

import com.abhishek.stories_app.model.Like;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

	boolean existsByUserAndStory(User user, Story story);

	Optional<Like> findByUserAndStory(User user, Story story);

	long countByStory(Story story);

	void deleteByUserAndStory(User user, Story story);
}
