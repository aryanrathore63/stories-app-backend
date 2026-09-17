package com.abhishek.stories_app.dto;

/**
 * Base64 audio payload for browser playback.
 */
public record NarrateResponse(
		String audioBase64,
		String contentType) {}

