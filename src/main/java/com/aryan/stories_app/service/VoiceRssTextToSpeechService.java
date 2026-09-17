package com.abhishek.stories_app.service;

import com.abhishek.stories_app.dto.NarrateRequest;
import com.abhishek.stories_app.dto.NarrateResponse;
import com.abhishek.stories_app.exception.BadRequestException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoiceRssTextToSpeechService {

	private final HttpClient httpClient;

	@Value("${VOICERSS_KEY:}")
	private String apiKey;

	public VoiceRssTextToSpeechService() {
		this.httpClient = HttpClient.newHttpClient();
	}

	public NarrateResponse narrate(String text, NarrateRequest req) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new BadRequestException("VoiceRSS is not configured");
		}

		String hl = req == null ? null : req.hl();
		if (hl == null || hl.isBlank()) hl = "en-us";

		String voice = req == null ? null : req.voice();

		Integer rate = req == null ? null : req.rate();
		if (rate == null) rate = 0;

		String codec = req == null ? null : req.codec();
		if (codec == null || codec.isBlank()) codec = "MP3";

		String cleaned = cleanupText(text);
		if (cleaned.isBlank()) {
			throw new BadRequestException("Text is empty");
		}
		// VoiceRSS supports up to 100KB. Base64 audio can get very large, so keep it bounded.
		if (cleaned.length() > 20_000) {
			cleaned = cleaned.substring(0, 20_000);
		}

		String query =
				"key="
						+ urlEncode(apiKey)
						+ "&hl="
						+ urlEncode(hl)
						+ (voice == null || voice.isBlank() ? "" : "&v=" + urlEncode(voice))
						+ "&src="
						+ urlEncode(cleaned)
						+ "&r="
						+ rate
						+ "&c="
						+ urlEncode(codec)
						+ "&b64=true";

		URI uri = URI.create("https://api.voicerss.org/?" + query);
		HttpRequest httpRequest = HttpRequest.newBuilder(uri).GET().build();
		HttpResponse<String> response;
		try {
			response =
					httpClient.send(
							httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new BadRequestException("Text-to-speech request failed");
		}

		String body = response.body() == null ? "" : response.body().trim();
		if (body.startsWith("ERROR:")) {
			throw new BadRequestException(body);
		}

		// Usually VoiceRSS returns base64 string directly. Some wrappers return JSON; handle both.
		String audioBase64 = extractAudioBase64(body);
		if (audioBase64.isBlank()) {
			throw new BadRequestException("Text-to-speech failed");
		}

		// We force MP3, so return an appropriate content type for data URLs.
		return new NarrateResponse(audioBase64, "audio/mpeg");
	}

	private String extractAudioBase64(String body) {
		if (!body.startsWith("{")) {
			return body;
		}
		String audio = extractJsonString(body, "audio");
		if (!audio.isBlank()) return audio;
		String speech = extractJsonString(body, "speech");
		if (!speech.isBlank()) return speech;
		return extractJsonString(body, "content");
	}

	private String extractJsonString(String json, String key) {
		String token = "\"" + key + "\"";
		int keyPos = json.indexOf(token);
		if (keyPos < 0) return "";
		int colonPos = json.indexOf(':', keyPos + token.length());
		if (colonPos < 0) return "";
		int startQuote = json.indexOf('"', colonPos + 1);
		if (startQuote < 0) return "";
		int endQuote = startQuote + 1;
		while (endQuote < json.length()) {
			char ch = json.charAt(endQuote);
			if (ch == '"' && json.charAt(endQuote - 1) != '\\') {
				break;
			}
			endQuote++;
		}
		if (endQuote >= json.length()) return "";
		return json.substring(startQuote + 1, endQuote);
	}

	private String urlEncode(String v) {
		return URLEncoder.encode(v, StandardCharsets.UTF_8);
	}

	private String cleanupText(String input) {
		if (input == null) return "";
		String s = input;
		// Basic markdown cleanup (keeps readable text).
		s = s.replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1"); // links: [text](url) -> text
		s = s.replaceAll("[*_`>#-]", " "); // markdown markers
		s = s.replaceAll("\\s+", " ").trim();
		return s;
	}
}

