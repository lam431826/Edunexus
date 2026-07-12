package com.edunexus.repository;

import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByDeckOrderByOrderIndexAsc(FlashcardDeck deck);
}
