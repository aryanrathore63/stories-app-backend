package com.abhishek.stories_app.service;

final class ApiLimits {

	static final int MAX_STORY_PAGE_SIZE = 50;
	static final int MAX_COMMENT_PAGE_SIZE = 100;

	private ApiLimits() {}

	static int page(int requested) {
		return Math.max(1, requested);
	}

	static int pageSize(int requested, int maximum) {
		return Math.min(maximum, Math.max(1, requested));
	}
}

