package com.abhishek.stories_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.refresh-token")
public record RefreshTokenProperties(
		int expirationDays,
		String cookieName,
		String cookiePath,
		boolean secure,
		String sameSite) {}
