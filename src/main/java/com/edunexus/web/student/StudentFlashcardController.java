package com.edunexus.web.student;

import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.MasteryLevel;
import com.edunexus.dto.FlashcardView;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/flashcards")
public class StudentFlashcardController {

    private final FlashcardService flashcardService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-16 Flashcard Library ----
    @GetMapping
    public String library(Model model) {
        User student = currentUserProvider.getCurrentUser();
        List<Enrollment> enrollments = enrollmentService.getEnrollments(student);
        Map<FlashcardDeck, int[]> deckStats = new LinkedHashMap<>(); // [mastered, total]
        List<FlashcardDeck> decks = new ArrayList<>();
        for (Enrollment e : enrollments) {
            List<FlashcardDeck> courseDecks = flashcardService.getDecksByCourse(e.getCourse().getId());
            decks.addAll(courseDecks);
            for (FlashcardDeck deck : courseDecks) {
                List<Flashcard> cards = flashcardService.getCards(deck);
                long mastered = cards.stream()
                        .filter(c -> flashcardService.getMastery(student, c) == MasteryLevel.KNOWN)
                        .count();
                deckStats.put(deck, new int[]{(int) mastered, cards.size()});
            }
        }
        model.addAttribute("decks", decks);
        model.addAttribute("deckStats", deckStats);
        return "student/flashcard-library";
    }

    // ---- SCR-17 Flashcard Practice ----
    @GetMapping("/decks/{id}/practice")
    public String practice(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        FlashcardDeck deck = flashcardService.getDeck(id);
        enrollmentService.assertEnrolled(student, deck.getModule().getCourse().getId());
        List<Flashcard> cards = flashcardService.getCards(deck);
        List<FlashcardView> cardViews = cards.stream()
                .map(c -> new FlashcardView(c.getId(), c.getFrontText(), c.getBackText()))
                .toList();
        model.addAttribute("deck", deck);
        model.addAttribute("cards", cardViews);
        return "student/flashcard-practice";
    }

    @PostMapping("/cards/{cardId}/review")
    @ResponseBody
    public String review(@PathVariable Long cardId, @RequestParam String level) {
        User student = currentUserProvider.getCurrentUser();
        Flashcard card = flashcardService.getCard(cardId);
        flashcardService.recordReview(student, card, MasteryLevel.valueOf(level));
        return "ok";
    }
}
