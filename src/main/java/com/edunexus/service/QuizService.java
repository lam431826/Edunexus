package com.edunexus.service;

import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.QuestionOption;
import com.edunexus.domain.QuizAnswer;
import com.edunexus.domain.QuizAttempt;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ContentStatus;
import com.edunexus.domain.enums.QuizMode;
import com.edunexus.domain.enums.QuizStatus;
import com.edunexus.dto.NewQuizForm;
import com.edunexus.dto.QuizOptionView;
import com.edunexus.dto.QuizQuestionView;
import com.edunexus.repository.QuestionOptionRepository;
import com.edunexus.repository.QuestionRepository;
import com.edunexus.repository.QuizAttemptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public QuizAttempt getById(Long id) {
        return quizAttemptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz attempt not found: " + id));
    }

    public List<QuizAttempt> getHistory(User student) {
        return quizAttemptRepository.findByStudentOrderByStartedAtDesc(student);
    }

    /** Practice-test scores are self-assessment only (GBR-10) — never written to any gradebook entity. */
    @Transactional
    public QuizAttempt startQuiz(User student, Module module, NewQuizForm form) {
        List<Question> pool = new ArrayList<>(questionRepository.findByModuleAndStatus(module, ContentStatus.APPROVED));
        Collections.shuffle(pool);
        int count = Math.min(form.getQuestionCount(), pool.size());
        List<Question> selected = pool.subList(0, count);

        QuizAttempt attempt = QuizAttempt.builder()
                .student(student)
                .module(module)
                .mode(form.getMode())
                .questionCount(count)
                .status(QuizStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            answers.add(QuizAnswer.builder()
                    .attempt(attempt)
                    .question(selected.get(i))
                    .orderIndex(i)
                    .flagged(false)
                    .build());
        }
        attempt.setAnswers(answers);
        return quizAttemptRepository.save(attempt);
    }

    @Transactional
    public void selectAnswer(QuizAttempt attempt, Long questionId, Long optionId, boolean flagged) {
        QuizAnswer answer = attempt.getAnswers().stream()
                .filter(a -> a.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Question not part of this attempt: " + questionId));
        if (optionId != null) {
            QuestionOption option = questionOptionRepository.findById(optionId)
                    .orElseThrow(() -> new EntityNotFoundException("Option not found: " + optionId));
            answer.setSelectedOption(option);
        }
        answer.setFlagged(flagged);
        quizAttemptRepository.save(attempt);
    }

    /** Never exposes {@code QuestionOption.correct} to the client while a quiz is in progress. */
    public List<QuizQuestionView> toQuestionViews(QuizAttempt attempt) {
        return attempt.getAnswers().stream()
                .map(a -> new QuizQuestionView(
                        a.getQuestion().getId(),
                        a.getQuestion().getText(),
                        a.getQuestion().getOptions().stream()
                                .map(o -> new QuizOptionView(o.getId(), o.getText()))
                                .toList(),
                        a.getSelectedOption() != null ? a.getSelectedOption().getId() : null,
                        a.isFlagged()))
                .toList();
    }

    @Transactional
    public QuizAttempt submit(QuizAttempt attempt) {
        int correct = 0;
        for (QuizAnswer answer : attempt.getAnswers()) {
            boolean isCorrect = answer.getSelectedOption() != null && answer.getSelectedOption().isCorrect();
            answer.setCorrect(isCorrect);
            if (isCorrect) {
                correct++;
            }
        }
        attempt.setCorrectCount(correct);
        attempt.setScore(attempt.getQuestionCount() == 0 ? 0
                : Math.round(correct * 100f / attempt.getQuestionCount()));
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setDurationSeconds((int) Duration.between(attempt.getStartedAt(), attempt.getSubmittedAt()).getSeconds());
        attempt.setStatus(QuizStatus.SUBMITTED);
        return quizAttemptRepository.save(attempt);
    }
}
