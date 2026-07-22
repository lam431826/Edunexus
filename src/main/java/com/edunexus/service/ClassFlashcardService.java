package com.edunexus.service;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.ClassFlashcard;
import com.edunexus.domain.ClassFlashcardDeck;
import com.edunexus.domain.User;
import com.edunexus.dto.ClassFlashcardDeckForm;
import com.edunexus.dto.ClassFlashcardRowForm;
import com.edunexus.repository.ClassFlashcardDeckRepository;
import com.edunexus.repository.ClassFlashcardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Teacher-authored class-specific flashcard deck/card CRUD - a parallel entity to
 * FlashcardDeck/Flashcard, never mixed with SME course content. Mirrors FlashcardService.
 */
@Service
@RequiredArgsConstructor
public class ClassFlashcardService {

    private final ClassFlashcardDeckRepository deckRepository;
    private final ClassFlashcardRepository cardRepository;

    public ClassFlashcardDeck getDeck(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class flashcard deck not found: " + id));
    }

    public ClassFlashcard getCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class flashcard not found: " + id));
    }

    public List<ClassFlashcardDeck> getDecksByClass(Long classId) {
        return deckRepository.findByClassEntity_Id(classId);
    }

    public List<ClassFlashcard> getCards(ClassFlashcardDeck deck) {
        return cardRepository.findByDeckOrderByOrderIndexAsc(deck);
    }

    @Transactional
    public ClassFlashcardDeck createDeck(ClassEntity classEntity, User createdBy, ClassFlashcardDeckForm form) {
        ClassFlashcardDeck deck = ClassFlashcardDeck.builder()
                .classEntity(classEntity)
                .name(form.getName())
                .description(form.getDescription())
                .createdBy(createdBy)
                .build();
        return deckRepository.save(deck);
    }

    @Transactional
    public ClassFlashcardDeck updateDeckAndCards(Long deckId, ClassFlashcardDeckForm form) {
        ClassFlashcardDeck deck = getDeck(deckId);
        deck.setName(form.getName());
        deck.setDescription(form.getDescription());
        deck = deckRepository.save(deck);

        for (ClassFlashcardRowForm row : form.getCards()) {
            if (row.getFrontText() == null || row.getFrontText().isBlank()) {
                continue;
            }
            ClassFlashcard card = row.getId() != null
                    ? cardRepository.findById(row.getId()).orElse(new ClassFlashcard())
                    : new ClassFlashcard();
            card.setDeck(deck);
            card.setFrontText(row.getFrontText());
            card.setBackText(row.getBackText());
            if (card.getId() == null) {
                card.setOrderIndex(getCards(deck).size());
            }
            cardRepository.save(card);
        }
        return deck;
    }

    @Transactional
    public void deleteDeck(ClassFlashcardDeck deck) {
        for (ClassFlashcard card : getCards(deck)) {
            cardRepository.delete(card);
        }
        deckRepository.delete(deck);
    }

    @Transactional
    public void deleteCard(ClassFlashcard card) {
        cardRepository.delete(card);
    }
}
