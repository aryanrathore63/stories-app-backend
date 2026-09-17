package com.abhishek.stories_app.repository;

import com.abhishek.stories_app.model.RefreshToken;
import com.abhishek.stories_app.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

	void deleteByUser(User user);
}
