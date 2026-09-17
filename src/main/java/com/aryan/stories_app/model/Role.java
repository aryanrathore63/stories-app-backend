package com.abhishek.stories_app.model;

public enum Role {
	USER,
	ADMIN;

	public String asAuthority() {
		return "ROLE_" + name();
	}
}
