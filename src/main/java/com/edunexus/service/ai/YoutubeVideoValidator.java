package com.edunexus.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Real YouTube Data API v3 lookup (API-02). Requires youtube.api-key (a Google Cloud API key with
 * the YouTube Data API v3 enabled) - the operator must supply this, see application.yml.
 *
 * Caveat: an API key only grants existence + caption-track *metadata* (language/kind), not the
 * verbatim transcript text - downloading actual caption text requires OAuth2 with a channel-owner
 * scope. So {@link AnthropicAiContentService} feeds the video's title/description into the LLM to
 * generate a summary rather than pulling a real transcript.
 */
@Component
public class YoutubeVideoValidator {

    private static final Pattern ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{6,})");

    private final RestClient restClient = RestClient.create();

    @Value("${youtube.api-key:}")
    private String apiKey;

    @Value("${youtube.api-base-url}")
    private String baseUrl;

    public record VideoInfo(String videoId, String title, String description, boolean hasCaptions) {
    }

    /** @throws IllegalArgumentException if the URL isn't a YouTube link, the video doesn't exist, or no API key is configured. */
    public VideoInfo validate(String youtubeUrl) {
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "YouTube validation is not configured (missing youtube.api-key).");
        }
        String videoId = extractVideoId(youtubeUrl);
        if (videoId == null) {
            throw new IllegalArgumentException(
                    "Video summary is unavailable because the video or transcript cannot be accessed.");
        }

        JsonNode videoResponse = restClient.get()
                .uri(baseUrl + "/videos?part=snippet,status&id={id}&key={key}", videoId, apiKey)
                .retrieve()
                .body(JsonNode.class);
        JsonNode items = videoResponse != null ? videoResponse.path("items") : null;
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Video summary is unavailable because the video or transcript cannot be accessed.");
        }
        JsonNode snippet = items.get(0).path("snippet");
        String title = snippet.path("title").asText("");
        String description = snippet.path("description").asText("");

        boolean hasCaptions = false;
        try {
            JsonNode captionResponse = restClient.get()
                    .uri(baseUrl + "/captions?part=snippet&videoId={id}&key={key}", videoId, apiKey)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode captionItems = captionResponse != null ? captionResponse.path("items") : null;
            hasCaptions = captionItems != null && captionItems.isArray() && !captionItems.isEmpty();
        } catch (RuntimeException ignored) {
            // Caption metadata is best-effort only; video validity above is what actually gates the feature.
        }

        return new VideoInfo(videoId, title, description, hasCaptions);
    }

    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(youtubeUrl.trim());
        return matcher.find() ? matcher.group(1) : null;
    }
}
