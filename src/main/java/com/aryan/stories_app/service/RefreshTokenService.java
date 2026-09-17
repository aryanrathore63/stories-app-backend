package com.abhishek.stories_app.service;

import com.abhishek.stories_app.config.RefreshTokenProperties;
import com.abhishek.stories_app.exception.UnauthorizedException;
import com.abhishek.stories_app.model.RefreshToken;
import com.abhishek.stories_app.model.User;
import com.abhishek.stories_app.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenProperties props;

	public record RotatedRefreshToken(String plainToken, User user) {}

	@Transactional
	public String createRefreshToken(User user) {
		String plain = generatePlainToken();
		refreshTokenRepository.save(buildEntity(user, plain));
		return plain;
	}

	@Transactional
	public RotatedRefreshToken rotateRefreshToken(String plainToken) {
		RefreshToken existing = findActiveToken(plainToken);
		if (existing.getExpiresAt().isBefore(Instant.now())) {
			existing.setRevoked(true);
			throw new UnauthorizedException("Refresh token expired");
		}
		existing.setRevoked(true);
		String nextPlain = generatePlainToken();
		refreshTokenRepository.save(buildEntity(existing.getUser(), nextPlain));
		return new RotatedRefreshToken(nextPlain, existing.getUser());
	}

	@Transactional
	public void revokeRefreshToken(String plainToken) {
		refreshTokenRepository
				.findByTokenHashAndRevokedFalse(hash(plainToken))
				.ifPresent(
						token -> {
							token.setRevoked(true);
						});
	}

	private RefreshToken findActiveToken(String plainToken) {
		return refreshTokenRepository
				.findByTokenHashAndRevokedFalse(hash(plainToken))
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
	}

	private RefreshToken buildEntity(User user, String plainToken) {
		return RefreshToken.builder()
				.user(user)
				.tokenHash(hash(plainToken))
				.expiresAt(Instant.now().plusSeconds(Math.max(1, props.expirationDays()) * 86_400L))
				.build();
	}

	private static String generatePlainToken() {
		byte[] bytes = new byte[32];
		SECURE_RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}

	static String hash(String plainToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
