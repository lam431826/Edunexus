package com.edunexus.repository;

import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardProgress;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {
    Optional<FlashcardProgress> findByStudentAndFlashcard(User student, Flashcard flashcard);

    List<FlashcardProgress> findByStudentAndFlashcard_Deck_Id(User student, Long deckId);
}
