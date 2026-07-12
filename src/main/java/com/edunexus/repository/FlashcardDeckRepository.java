package com.edunexus.repository;

import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, Long> {
    List<FlashcardDeck> findByModule(Module module);

    List<FlashcardDeck> findByModule_CourseId(Long courseId);
}
