package com.abhishek.stories_app.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.proxy")
public record ProxyProperties(boolean trustForwardedHeaders, String trustedProxies) {

	public Set<String> trustedProxyIps() {
		if (trustedProxies == null || trustedProxies.isBlank()) {
			return Set.of();
		}
		return Arrays.stream(trustedProxies.split(","))
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}
}

