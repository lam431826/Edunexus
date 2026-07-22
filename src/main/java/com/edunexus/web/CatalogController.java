package com.edunexus.web;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.Question;
import com.edunexus.domain.enums.ClassStatus;
import com.edunexus.domain.enums.ContentStatus;
import com.edunexus.domain.enums.CourseStatus;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.FlashcardDeckRepository;
import com.edunexus.repository.FlashcardRepository;
import com.edunexus.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Public course catalog (UC-GST-01..04) - no authentication required. Guest preview is capped at
 * 10 sample questions and 5 sample flashcards per GBR-15; beyond that a Student must register.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class CatalogController {

    private static final int GUEST_PREVIEW_MAX_QUESTIONS = 10;
    private static final int GUEST_PREVIEW_MAX_FLASHCARDS = 5;

    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final QuestionRepository questionRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final FlashcardRepository flashcardRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseRepository.findByStatus(CourseStatus.PUBLISHED));
        model.addAttribute("classes", classRepository.findByStatus(ClassStatus.PUBLISHED));
        return "catalog/list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));

        List<Question> questions = questionRepository.findByModule_CourseIdAndStatus(id, ContentStatus.APPROVED);
        List<Question> sampleQuestions = questions.size() > GUEST_PREVIEW_MAX_QUESTIONS
                ? questions.subList(0, GUEST_PREVIEW_MAX_QUESTIONS) : questions;

        List<FlashcardDeck> decks = flashcardDeckRepository.findByModule_CourseId(id);
        List<Flashcard> sampleFlashcards = new ArrayList<>();
        for (FlashcardDeck deck : decks) {
            if (sampleFlashcards.size() >= GUEST_PREVIEW_MAX_FLASHCARDS) {
                break;
            }
            for (Flashcard card : flashcardRepository.findByDeckOrderByOrderIndexAsc(deck)) {
                if (sampleFlashcards.size() >= GUEST_PREVIEW_MAX_FLASHCARDS) {
                    break;
                }
                sampleFlashcards.add(card);
            }
        }

        model.addAttribute("course", course);
        model.addAttribute("sampleQuestions", sampleQuestions);
        model.addAttribute("sampleFlashcards", sampleFlashcards);
        model.addAttribute("totalQuestionCount", questions.size());
        model.addAttribute("previewLimited", questions.size() > GUEST_PREVIEW_MAX_QUESTIONS);
        model.addAttribute("classes", classRepository.findBySourceCourse_Id(id));
        return "catalog/course-detail";
    }

    @GetMapping("/classes/{id}")
    public String classDetail(@PathVariable Long id, Model model) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + id));
        model.addAttribute("classEntity", classEntity);
        return "catalog/class-detail";
    }
}
