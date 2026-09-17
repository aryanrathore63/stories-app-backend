package com.abhishek.stories_app.repository;

import com.abhishek.stories_app.model.Bookmark;
import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	boolean existsByUserAndStory(User user, Story story);

	Optional<Bookmark> findByUserAndStory(User user, Story story);

	void deleteByUserAndStory(User user, Story story);

	@Query(
			"""
			SELECT b FROM Bookmark b
			JOIN FETCH b.story s
			JOIN FETCH s.author
			WHERE b.user = :user
			ORDER BY b.createdAt DESC
			""")
	List<Bookmark> findForUserWithStoryAndAuthor(@Param("user") User user);
}
