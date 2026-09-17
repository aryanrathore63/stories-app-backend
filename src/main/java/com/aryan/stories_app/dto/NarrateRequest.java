package com.abhishek.stories_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * VoiceRSS request options (all optional; backend applies defaults).
 */
public record NarrateRequest(
		@Size(max = 10) String hl,
		@Size(max = 32) String voice,
		@Min(-10) @Max(10) Integer rate,
		@Size(max = 10) String codec) {}

