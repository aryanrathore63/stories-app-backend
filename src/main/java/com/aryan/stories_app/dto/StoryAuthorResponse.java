package com.abhishek.stories_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoryAuthorResponse(String id, String name) {}
