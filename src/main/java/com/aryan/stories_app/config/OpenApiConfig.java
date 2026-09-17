package com.abhishek.stories_app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI storiesOpenAPI() {
		final String scheme = "bearerAuth";
		return new OpenAPI()
				.info(
						new Info()
								.title("Stories API")
								.description(
										"JWT-secured blog/stories platform. Register, login, then"
												+ " click Authorize and use: Bearer {token}")
								.version("1.0.0"))
				.addSecurityItem(new SecurityRequirement().addList(scheme))
				.components(
						new Components()
								.addSecuritySchemes(
										scheme,
										new SecurityScheme()
												.name(scheme)
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")));
	}
}
