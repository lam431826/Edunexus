package com.edunexus.repository;

import com.edunexus.domain.ClassFlashcard;
import com.edunexus.domain.ClassFlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassFlashcardRepository extends JpaRepository<ClassFlashcard, Long> {
    List<ClassFlashcard> findByDeckOrderByOrderIndexAsc(ClassFlashcardDeck deck);
}
