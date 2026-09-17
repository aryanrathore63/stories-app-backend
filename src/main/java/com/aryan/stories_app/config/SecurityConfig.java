package com.abhishek.stories_app.config;

import com.abhishek.stories_app.security.JwtAuthenticationFilter;
import com.abhishek.stories_app.security.RateLimitFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final RateLimitFilter rateLimitFilter;
	private final CorsProperties corsProperties;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(
						sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						auth ->
								auth.requestMatchers(
												"/auth/login",
												"/auth/register",
												"/auth/refresh",
												"/auth/logout")
										.permitAll()
										.requestMatchers(
												"/swagger-ui/**",
												"/swagger-ui.html",
												"/v3/api-docs/**")
										.permitAll()
										.requestMatchers("/h2-console", "/h2-console/**")
										.permitAll()
										.requestMatchers(HttpMethod.GET, "/stories/me")
										.authenticated()
										.requestMatchers(HttpMethod.GET, "/stories/trending")
										.permitAll()
										.requestMatchers(HttpMethod.GET, "/stories/*/comments")
										.permitAll()
										.requestMatchers(HttpMethod.GET, "/stories")
										.permitAll()
										.requestMatchers(HttpMethod.GET, "/stories/*")
										.permitAll()
										.requestMatchers(HttpMethod.POST, "/stories/*/narrate")
										.authenticated()
										.requestMatchers("/admin/**")
										.hasRole("ADMIN")
										.anyRequest()
										.authenticated())
				.addFilterBefore(
						rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(
						jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		List<String> origins =
				Arrays.stream(corsProperties.allowedOrigins().split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toList();
		config.setAllowedOriginPatterns(origins.isEmpty() ? List.of("http://localhost:3000") : origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
