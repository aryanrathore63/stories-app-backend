package com.abhishek.stories_app.security;

import com.abhishek.stories_app.config.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenCookieService {

	private final RefreshTokenProperties props;

	public Optional<String> readRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
				.filter(c -> props.cookieName().equals(c.getName()))
				.map(Cookie::getValue)
				.filter(v -> v != null && !v.isBlank())
				.findFirst();
	}

	public void setRefreshCookie(HttpServletResponse response, String token, Duration maxAge) {
		ResponseCookie cookie =
				ResponseCookie.from(props.cookieName(), token)
						.httpOnly(true)
						.secure(props.secure())
						.sameSite(props.sameSite())
						.path(props.cookiePath())
						.maxAge(maxAge)
						.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie =
				ResponseCookie.from(props.cookieName(), "")
						.httpOnly(true)
						.secure(props.secure())
						.sameSite(props.sameSite())
						.path(props.cookiePath())
						.maxAge(0)
						.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public Duration cookieMaxAge() {
		return Duration.ofDays(Math.max(1, props.expirationDays()));
	}
}
