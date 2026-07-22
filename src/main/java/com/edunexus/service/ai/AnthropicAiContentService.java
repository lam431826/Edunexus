package com.edunexus.service.ai;

import com.edunexus.domain.RubricCriterion;
import com.edunexus.domain.enums.Difficulty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Real LLM-backed implementation (API-03/API-04) using the Anthropic Messages API, activated via
 * {@code app.ai.provider=anthropic}. Requires anthropic.api-key (see application.yml) - the
 * operator must supply a real key. Every {@link AiContentService} method keeps the exact same
 * contract as {@link MockAiContentService} so LessonService/QuestionService/FlashcardService/
 * AsyncGradingService need zero changes.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicAiContentService implements AiContentService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final YoutubeVideoValidator youtubeVideoValidator;

    @Value("${anthropic.api-key:}")
    private String apiKey;

    @Value("${anthropic.base-url}")
    private String baseUrl;

    @Value("${anthropic.model}")
    private String model;

    public AnthropicAiContentService(YoutubeVideoValidator youtubeVideoValidator) {
        this.youtubeVideoValidator = youtubeVideoValidator;
    }

    @Override
    public String generateLessonDraft(String sourceHint) {
        String topic = (sourceHint == null || sourceHint.isBlank()) ? "chủ đề bài học" : sourceHint.trim();
        return callForText("""
                Viết một bản nháp bài giảng (markdown, tiếng Việt) về chủ đề sau, gồm mục Tổng quan, \
                Nội dung chính (dạng danh sách có đánh số) và Tóm tắt. Chủ đề: %s""".formatted(topic));
    }

    @Override
    public String extractYoutubeTranscript(String youtubeUrl) {
        // API key alone only grants existence + caption metadata, not verbatim transcript text
        // (that needs OAuth2 channel-owner scope) - so the LLM summarizes from title/description.
        YoutubeVideoValidator.VideoInfo info = youtubeVideoValidator.validate(youtubeUrl);
        return callForText("""
                Dựa trên tiêu đề và mô tả của video YouTube dưới đây, hãy viết một bản tóm tắt nội dung \
                (markdown, tiếng Việt) hữu ích cho một bài giảng.
                Tiêu đề: %s
                Mô tả: %s""".formatted(info.title(), info.description()));
    }

    @Override
    public List<DraftQuestion> generateQuestions(String topicHint, int count) {
        String topic = (topicHint == null || topicHint.isBlank()) ? "nội dung bài học" : topicHint.trim();
        String prompt = """
                Tạo %d câu hỏi trắc nghiệm (tiếng Việt) về chủ đề "%s". Trả lời CHỈ bằng JSON hợp lệ, \
                không thêm chữ nào khác, theo đúng định dạng mảng:
                [{"text":"...","difficulty":"EASY|MEDIUM|HARD","options":["...","...","...","..."],"correctIndex":0,"explanation":"..."}]
                """.formatted(Math.max(count, 1), topic);
        JsonNode array = callForJson(prompt);
        List<DraftQuestion> result = new ArrayList<>();
        for (JsonNode node : array) {
            List<String> options = new ArrayList<>();
            node.path("options").forEach(o -> options.add(o.asText()));
            Difficulty difficulty;
            try {
                difficulty = Difficulty.valueOf(node.path("difficulty").asText("MEDIUM"));
            } catch (IllegalArgumentException ex) {
                difficulty = Difficulty.MEDIUM;
            }
            result.add(new DraftQuestion(node.path("text").asText(), difficulty, options,
                    node.path("correctIndex").asInt(0), node.path("explanation").asText("")));
        }
        return result;
    }

    @Override
    public List<DraftFlashcard> generateFlashcards(String topicHint, int count) {
        String topic = (topicHint == null || topicHint.isBlank()) ? "khái niệm" : topicHint.trim();
        String prompt = """
                Tạo %d thẻ ghi nhớ (flashcard, tiếng Việt) về chủ đề "%s". Trả lời CHỈ bằng JSON hợp lệ, \
                theo đúng định dạng mảng: [{"frontText":"thuật ngữ","backText":"định nghĩa"}]
                """.formatted(Math.max(count, 1), topic);
        JsonNode array = callForJson(prompt);
        List<DraftFlashcard> result = new ArrayList<>();
        for (JsonNode node : array) {
            result.add(new DraftFlashcard(node.path("frontText").asText(), node.path("backText").asText()));
        }
        return result;
    }

    @Override
    public EssayGradeResult gradeEssayPreliminary(String submissionText, List<RubricCriterion> criteria, int maxScore) {
        StringBuilder criteriaDesc = new StringBuilder();
        for (RubricCriterion c : criteria) {
            criteriaDesc.append("- id=%d, tên=\"%s\", trọng số=%d%%, mô tả=\"%s\"\n"
                    .formatted(c.getId(), c.getName(), c.getWeightPercent(), c.getDescriptor()));
        }
        String prompt = """
                Chấm sơ bộ bài luận sau đây theo rubric, tổng điểm tối đa là %d. Rubric:
                %s
                Bài làm của học viên:
                %s

                Trả lời CHỈ bằng JSON hợp lệ theo định dạng:
                {"criterionScores":[{"criterionId":<id>,"score":<int>,"remark":"..."}],"overallFeedback":"..."}
                """.formatted(maxScore, criteriaDesc, submissionText == null ? "" : submissionText);
        JsonNode root = callForJsonObject(prompt);
        List<CriterionScore> scores = new ArrayList<>();
        int total = 0;
        for (JsonNode node : root.path("criterionScores")) {
            int score = node.path("score").asInt(0);
            total += score;
            scores.add(new CriterionScore(node.path("criterionId").asLong(), score, node.path("remark").asText("")));
        }
        return new EssayGradeResult(total, root.path("overallFeedback").asText(""), scores);
    }

    private String callForText(String prompt) {
        JsonNode response = call(prompt);
        return response.path("content").path(0).path("text").asText("");
    }

    private JsonNode callForJson(String prompt) {
        String text = callForText(prompt);
        try {
            return objectMapper.readTree(extractJson(text));
        } catch (Exception ex) {
            throw new IllegalStateException("AI provider returned an unparseable response.", ex);
        }
    }

    private JsonNode callForJsonObject(String prompt) {
        return callForJson(prompt);
    }

    private String extractJson(String text) {
        int start = Math.min(
                indexOfOrMax(text, '['), indexOfOrMax(text, '{'));
        int endBracket = text.lastIndexOf(']');
        int endBrace = text.lastIndexOf('}');
        int end = Math.max(endBracket, endBrace);
        if (start >= text.length() || end < 0 || end < start) {
            return text;
        }
        return text.substring(start, end + 1);
    }

    private int indexOfOrMax(String text, char c) {
        int idx = text.indexOf(c);
        return idx < 0 ? Integer.MAX_VALUE : idx;
    }

    private JsonNode call(String prompt) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("AI provider is not configured (missing anthropic.api-key).");
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 2048,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        return restClient.post()
                .uri(baseUrl + "/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }
}
