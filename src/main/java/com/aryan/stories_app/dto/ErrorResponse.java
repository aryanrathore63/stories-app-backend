package com.abhishek.stories_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ErrorResponse(
		String message,
		Object details,
		String error,
		int statusCode) {

	public static ErrorResponse of(String message, int status) {
		return ErrorResponse.builder().message(message).statusCode(status).build();
	}

	public static ErrorResponse ofValidation(List<String> messages, int status) {
		return ErrorResponse.builder()
				.message("Validation failed")
				.details(messages)
				.statusCode(status)
				.build();
	}
}
