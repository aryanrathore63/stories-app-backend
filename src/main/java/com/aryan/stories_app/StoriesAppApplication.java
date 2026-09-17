package com.abhishek.stories_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StoriesAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoriesAppApplication.class, args);
	}

}
