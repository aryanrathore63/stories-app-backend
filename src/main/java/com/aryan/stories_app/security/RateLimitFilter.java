package com.abhishek.stories_app.security;

import com.abhishek.stories_app.config.RateLimitProperties;
import com.abhishek.stories_app.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final RateLimitService rateLimitService;
	private final ClientIpResolver clientIpResolver;

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		if (!rateLimitService.isEnabled()) {
			return true;
		}
		return HttpMethod.OPTIONS.matches(request.getMethod());
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain)
			throws ServletException, IOException {
		String method = request.getMethod();
		String path = request.getRequestURI();
		String clientKey = clientIpResolver.resolve(request);

		for (RateLimitProperties.Rule rule : rateLimitService.matchingRules(method, path)) {
			if (!rateLimitService.tryConsume(rule, clientKey)) {
				writeTooManyRequests(response, rule);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private void writeTooManyRequests(
			HttpServletResponse response, RateLimitProperties.Rule rule)
			throws IOException {
		long retryAfter = rateLimitService.retryAfterSeconds(rule);
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setHeader("Retry-After", String.valueOf(retryAfter));
		var body =
				ErrorResponse.of(
						"Too many requests. Please try again later.", 429);
		response.getWriter().write(JSON.writeValueAsString(body));
	}
}
