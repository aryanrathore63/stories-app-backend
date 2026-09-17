package com.abhishek.stories_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-with-at-least-32-bytes")
@ActiveProfiles("dev")
class StoriesAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
