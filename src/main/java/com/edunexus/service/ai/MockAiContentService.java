package com.edunexus.service.ai;

import com.edunexus.domain.RubricCriterion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Rule-based stand-in for a real LLM/YouTube provider. No external network calls are made — this
 * lets the whole SME authoring / AI-staging flow (SCR-07, SCR-08, SCR-15, SCR-20) and the essay
 * preliminary-grading flow (Assignment Result) run without any API key. Active by default
 * ({@code app.ai.provider=mock}); set {@code app.ai.provider=anthropic} to switch to
 * {@link AnthropicAiContentService} once a real API key is supplied - the app remains fully usable
 * without AI support either way (NFR: manual authoring/grading must always work).
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiContentService implements AiContentService {

    private static final Pattern YOUTUBE_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{6,}.*$");

    @Override
    public String generateLessonDraft(String sourceHint) {
        String topic = blank(sourceHint) ? "chủ đề bài học" : sourceHint.trim();
        return """
                ## Tổng quan
                Bài học này giới thiệu về **%s**, tập trung vào các khái niệm cốt lõi mà người học cần nắm vững.

                ## Nội dung chính
                1. Định nghĩa và vai trò của %s trong thực tế.
                2. Các thành phần/bước chính liên quan đến %s.
                3. Ví dụ minh họa và lỗi thường gặp.

                ## Tóm tắt
                Nội dung trên là bản nháp do AI tạo ra từ nguồn bạn cung cấp. Vui lòng xem lại, chỉnh sửa
                và phê duyệt trước khi xuất bản cho học viên.
                """.formatted(topic, topic, topic);
    }

    @Override
    public String extractYoutubeTranscript(String youtubeUrl) {
        if (blank(youtubeUrl) || !YOUTUBE_PATTERN.matcher(youtubeUrl.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Video summary is unavailable because the video or transcript cannot be accessed.");
        }
        return """
                [AI Transcript Summary]
                Video liên kết tại %s đã được xử lý. Bản tóm tắt tự động (giả lập) trình bày lại nội dung
                chính theo trình tự thời gian của video, sẵn sàng để bạn đưa vào AI Lesson Staging và
                chỉnh sửa thêm trước khi công bố.
                """.formatted(youtubeUrl.trim());
    }

    @Override
    public List<DraftQuestion> generateQuestions(String topicHint, int count) {
        String topic = blank(topicHint) ? "nội dung bài học" : topicHint.trim();
        var difficulties = com.edunexus.domain.enums.Difficulty.values();
        List<DraftQuestion> result = new ArrayList<>();
        for (int i = 1; i <= Math.max(count, 1); i++) {
            List<String> options = List.of(
                    "Đáp án đúng liên quan tới " + topic,
                    "Phương án gây nhiễu 1",
                    "Phương án gây nhiễu 2",
                    "Phương án gây nhiễu 3"
            );
            result.add(new DraftQuestion(
                    "Câu hỏi %d (AI nháp) về %s là gì?".formatted(i, topic),
                    difficulties[i % difficulties.length],
                    options,
                    0,
                    "Giải thích: đáp án đầu tiên phản ánh đúng khái niệm cốt lõi của " + topic + "."
            ));
        }
        return result;
    }

    @Override
    public List<DraftFlashcard> generateFlashcards(String topicHint, int count) {
        String topic = blank(topicHint) ? "khái niệm" : topicHint.trim();
        List<DraftFlashcard> result = new ArrayList<>();
        for (int i = 1; i <= Math.max(count, 1); i++) {
            result.add(new DraftFlashcard(
                    "Thuật ngữ %d - %s".formatted(i, topic),
                    "Định nghĩa (AI nháp) cho thuật ngữ %d liên quan tới %s.".formatted(i, topic)
            ));
        }
        return result;
    }

    @Override
    public EssayGradeResult gradeEssayPreliminary(String submissionText, List<RubricCriterion> criteria, int maxScore) {
        int length = submissionText == null ? 0 : submissionText.trim().length();
        // Heuristic: longer, non-trivial answers score higher, capped at each criterion's share of maxScore.
        double qualityRatio = Math.min(1.0, length / 600.0) * 0.6 + 0.3;

        List<CriterionScore> scores = new ArrayList<>();
        int total = 0;
        for (RubricCriterion criterion : criteria) {
            int criterionMax = (int) Math.round(maxScore * (criterion.getWeightPercent() / 100.0));
            int score = (int) Math.round(criterionMax * qualityRatio);
            total += score;
            scores.add(new CriterionScore(criterion.getId(), score,
                    "AI nhận thấy bài làm " + (qualityRatio > 0.7 ? "đáp ứng tốt" : "cần bổ sung thêm")
                            + " tiêu chí \"" + criterion.getName() + "\"."));
        }
        String feedback = length < 100
                ? "Bài làm khá ngắn, hãy bổ sung thêm luận điểm và ví dụ để đạt điểm cao hơn."
                : "Bài làm thể hiện được các ý chính. Đây là điểm sơ bộ từ AI, chờ xác nhận cuối cùng.";
        return new EssayGradeResult(total, feedback, scores);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
