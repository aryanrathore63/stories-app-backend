package com.abhishek.stories_app.security;

import com.abhishek.stories_app.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

@Service
public class RateLimitService {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private final RateLimitProperties properties;
	private final LinkedHashMap<String, BucketEntry> buckets = new LinkedHashMap<>(128, 0.75f, true);

	public RateLimitService(RateLimitProperties properties) {
		this.properties = properties;
	}

	public boolean isEnabled() {
		return properties.enabled() && !properties.rules().isEmpty();
	}

	public List<RateLimitProperties.Rule> matchingRules(String method, String path) {
		return properties.rules().stream()
				.filter(rule -> matches(rule, method, path))
				.toList();
	}

	public synchronized boolean tryConsume(RateLimitProperties.Rule rule, String clientKey) {
		long now = System.nanoTime();
		removeExpiredBuckets(now);
		String bucketKey = rule.name() + ":" + clientKey;
		BucketEntry entry = buckets.get(bucketKey);
		if (entry == null) {
			while (buckets.size() >= properties.maxBuckets()) {
				Iterator<String> iterator = buckets.keySet().iterator();
				if (!iterator.hasNext()) break;
				iterator.next();
				iterator.remove();
			}
			entry = new BucketEntry(newBucket(rule), now);
			buckets.put(bucketKey, entry);
		} else {
			entry.lastAccessNanos = now;
		}
		return entry.bucket.tryConsume(1);
	}

	public long retryAfterSeconds(RateLimitProperties.Rule rule) {
		return Math.max(1, rule.window().getSeconds());
	}

	private static boolean matches(RateLimitProperties.Rule rule, String method, String path) {
		if (!rule.methods().isEmpty()
				&& rule.methods().stream().noneMatch(m -> m.equalsIgnoreCase(method))) {
			return false;
		}
		return PATH_MATCHER.match(rule.pathPattern(), path);
	}

	private void removeExpiredBuckets(long now) {
		long ttlNanos = properties.bucketTtl().toNanos();
		Iterator<BucketEntry> iterator = buckets.values().iterator();
		while (iterator.hasNext()) {
			BucketEntry entry = iterator.next();
			if (now - entry.lastAccessNanos < ttlNanos) {
				break;
			}
			iterator.remove();
		}
	}

	synchronized int bucketCount() {
		return buckets.size();
	}

	private static Bucket newBucket(RateLimitProperties.Rule rule) {
		int capacity = Math.max(1, rule.capacity());
		int refill = Math.max(1, rule.refillAmount());
		Bandwidth limit =
				Bandwidth.builder()
						.capacity(capacity)
						.refillGreedy(refill, rule.window())
						.build();
		return Bucket.builder().addLimit(limit).build();
	}

	private static final class BucketEntry {
		private final Bucket bucket;
		private long lastAccessNanos;

		private BucketEntry(Bucket bucket, long lastAccessNanos) {
			this.bucket = bucket;
			this.lastAccessNanos = lastAccessNanos;
		}
	}
}
