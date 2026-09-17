package com.abhishek.stories_app.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
		boolean enabled, int maxBuckets, Duration bucketTtl, List<Rule> rules) {

	public RateLimitProperties {
		if (rules == null) {
			rules = List.of();
		}
		maxBuckets = Math.max(100, maxBuckets);
		if (bucketTtl == null || bucketTtl.isNegative() || bucketTtl.isZero()) {
			bucketTtl = Duration.ofMinutes(15);
		}
	}

	public record Rule(
			String name,
			String pathPattern,
			List<String> methods,
			int capacity,
			int refillAmount,
			Duration window) {

		public Rule {
			if (methods == null) {
				methods = List.of();
			}
		}
	}
}
