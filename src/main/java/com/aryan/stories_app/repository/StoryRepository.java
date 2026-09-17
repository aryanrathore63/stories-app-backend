package com.abhishek.stories_app.repository;

import com.abhishek.stories_app.model.Story;
import com.abhishek.stories_app.model.StoryStatus;
import com.abhishek.stories_app.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryRepository extends JpaRepository<Story, Long> {

	@EntityGraph(attributePaths = "author")
	@Query("SELECT s FROM Story s ORDER BY s.updatedAt DESC")
	List<Story> findAllForAdminModeration();

	@EntityGraph(attributePaths = "author")
	@Query("SELECT s FROM Story s WHERE s.id = :id")
	Optional<Story> findDetailById(@Param("id") Long id);

	@EntityGraph(attributePaths = "author")
	Page<Story> findByStatus(StoryStatus status, Pageable pageable);

	@EntityGraph(attributePaths = "author")
	Page<Story> findByStatusAndTitleContainingIgnoreCase(
			StoryStatus status, String title, Pageable pageable);

	@EntityGraph(attributePaths = "author")
	Page<Story> findByAuthorAndStatus(User author, StoryStatus status, Pageable pageable);

	@EntityGraph(attributePaths = "author")
	Page<Story> findByAuthor(User author, Pageable pageable);

	long countByStatus(StoryStatus status);

	@EntityGraph(attributePaths = "author")
	@Query(
			"""
			SELECT s FROM Story s
			WHERE s.status = :status
			ORDER BY SIZE(s.likes) DESC, s.updatedAt DESC
			""")
	Page<Story> findTrending(@Param("status") StoryStatus status, Pageable pageable);
}
