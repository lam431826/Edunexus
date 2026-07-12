package com.edunexus.service;

import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.FlashcardProgress;
import com.edunexus.domain.Module;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ContentSource;
import com.edunexus.domain.enums.ContentStatus;
import com.edunexus.domain.enums.MasteryLevel;
import com.edunexus.dto.FlashcardDeckForm;
import com.edunexus.dto.FlashcardRowForm;
import com.edunexus.repository.FlashcardDeckRepository;
import com.edunexus.repository.FlashcardProgressRepository;
import com.edunexus.repository.FlashcardRepository;
import com.edunexus.service.ai.DraftFlashcard;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardDeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;

    public Flashcard getCard(Long id) {
        return flashcardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found: " + id));
    }

    public FlashcardDeck getDeck(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard deck not found: " + id));
    }

    public List<FlashcardDeck> getDecksByCourse(Long courseId) {
        return deckRepository.findByModule_CourseId(courseId);
    }

    public List<Flashcard> getCards(FlashcardDeck deck) {
        return flashcardRepository.findByDeckOrderByOrderIndexAsc(deck);
    }

    @Transactional
    public FlashcardDeck createOrUpdateDeck(Long deckId, Module module, FlashcardDeckForm form) {
        FlashcardDeck deck = deckId == null ? new FlashcardDeck() : getDeck(deckId);
        deck.setModule(module);
        deck.setName(form.getName());
        deck.setDescription(form.getDescription());
        deck = deckRepository.save(deck);

        for (FlashcardRowForm row : form.getCards()) {
            if (row.getFrontText() == null || row.getFrontText().isBlank()) {
                continue;
            }
            Flashcard card = row.getId() != null
                    ? flashcardRepository.findById(row.getId()).orElse(new Flashcard())
                    : new Flashcard();
            card.setDeck(deck);
            card.setFrontText(row.getFrontText());
            card.setBackText(row.getBackText());
            if (card.getId() == null) {
                card.setOrderIndex(getCards(deck).size());
                card.setSource(ContentSource.MANUAL);
                card.setStatus(ContentStatus.APPROVED);
            }
            flashcardRepository.save(card);
        }
        return deck;
    }

    @Transactional
    public List<Flashcard> saveAiDrafts(FlashcardDeck deck, List<DraftFlashcard> drafts) {
        int startIndex = getCards(deck).size();
        return drafts.stream().map(d -> flashcardRepository.save(Flashcard.builder()
                .deck(deck)
                .frontText(d.frontText())
                .backText(d.backText())
                .orderIndex(startIndex + drafts.indexOf(d))
                .source(ContentSource.AI)
                .status(ContentStatus.PENDING_REVIEW)
                .build())).toList();
    }

    @Transactional
    public void approve(Flashcard card) {
        card.setStatus(ContentStatus.APPROVED);
        flashcardRepository.save(card);
    }

    @Transactional
    public void recordReview(User student, Flashcard card, MasteryLevel level) {
        FlashcardProgress progress = progressRepository.findByStudentAndFlashcard(student, card)
                .orElseGet(() -> FlashcardProgress.builder().student(student).flashcard(card).build());
        progress.setMasteryLevel(level);
        int hoursUntilNextReview = switch (level) {
            case KNOWN -> 24 * 7;
            case LEARNING -> 24;
            case NEW -> 1;
        };
        progress.setNextReviewAt(LocalDateTime.now().plusHours(hoursUntilNextReview));
        progressRepository.save(progress);
    }

    public MasteryLevel getMastery(User student, Flashcard card) {
        return progressRepository.findByStudentAndFlashcard(student, card)
                .map(FlashcardProgress::getMasteryLevel)
                .orElse(MasteryLevel.NEW);
    }
}
