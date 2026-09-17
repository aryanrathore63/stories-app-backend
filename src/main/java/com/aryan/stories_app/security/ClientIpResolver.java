package com.abhishek.stories_app.security;

import jakarta.servlet.http.HttpServletRequest;
import com.abhishek.stories_app.config.ProxyProperties;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
final class ClientIpResolver {

	private final boolean trustForwardedHeaders;
	private final Set<String> trustedProxies;

	ClientIpResolver(ProxyProperties properties) {
		this.trustForwardedHeaders = properties.trustForwardedHeaders();
		this.trustedProxies = properties.trustedProxyIps();
	}

	String resolve(HttpServletRequest request) {
		String remoteAddress = request.getRemoteAddr();
		if (!trustForwardedHeaders || !trustedProxies.contains(remoteAddress)) {
			return remoteAddress;
		}
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			String first = forwarded.split(",")[0].trim();
			if (!first.isEmpty()) {
				return first;
			}
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return remoteAddress;
	}
}
