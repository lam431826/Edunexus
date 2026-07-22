package com.edunexus.repository;

import com.edunexus.domain.ClassFlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassFlashcardDeckRepository extends JpaRepository<ClassFlashcardDeck, Long> {
    List<ClassFlashcardDeck> findByClassEntity_Id(Long classId);
}
