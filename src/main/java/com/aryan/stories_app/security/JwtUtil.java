package com.abhishek.stories_app.security;

import com.abhishek.stories_app.config.JwtProperties;
import com.abhishek.stories_app.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	private final JwtProperties props;
	private final SecretKey key;

	public JwtUtil(JwtProperties props) {
		this.props = props;
		if (props.secret() == null || props.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
		}
		this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(Long userId, String email, Role role) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + props.expirationMs());
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("role", role.name())
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (ExpiredJwtException
				| UnsupportedJwtException
				| MalformedJwtException
				| SignatureException
				| IllegalArgumentException e) {
			return false;
		}
	}

	public Long extractUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}
}
