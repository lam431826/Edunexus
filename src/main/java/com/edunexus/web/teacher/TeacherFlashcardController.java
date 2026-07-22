package com.edunexus.web.teacher;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.ClassFlashcard;
import com.edunexus.domain.ClassFlashcardDeck;
import com.edunexus.domain.User;
import com.edunexus.dto.ClassFlashcardDeckForm;
import com.edunexus.dto.ClassFlashcardRowForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ClassFlashcardService;
import com.edunexus.service.ClassService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/classes/{classId}/flashcards")
public class TeacherFlashcardController {

    private final ClassFlashcardService classFlashcardService;
    private final ClassService classService;
    private final CurrentUserProvider currentUserProvider;

    // ---- Deck list + create ----
    @GetMapping
    public String list(@PathVariable Long classId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("decks", classFlashcardService.getDecksByClass(classId));
        model.addAttribute("deckForm", new ClassFlashcardDeckForm());
        return "teacher/flashcard-list";
    }

    @PostMapping
    public String create(@PathVariable Long classId, @Valid @ModelAttribute("deckForm") ClassFlashcardDeckForm form,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        User teacher = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("decks", classFlashcardService.getDecksByClass(classId));
            return "teacher/flashcard-list";
        }
        ClassFlashcardDeck deck = classFlashcardService.createDeck(classEntity, teacher, form);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã tạo bộ thẻ.");
        return "redirect:/teacher/classes/" + classId + "/flashcards/decks/" + deck.getId();
    }

    // ---- Deck detail: card CRUD ----
    @GetMapping("/decks/{deckId}")
    public String deckDetail(@PathVariable Long classId, @PathVariable Long deckId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        ClassFlashcardDeck deck = ownedDeck(classId, deckId);
        List<ClassFlashcard> cards = classFlashcardService.getCards(deck);

        ClassFlashcardDeckForm form = new ClassFlashcardDeckForm();
        form.setName(deck.getName());
        form.setDescription(deck.getDescription());
        List<ClassFlashcardRowForm> rows = new ArrayList<>();
        for (ClassFlashcard c : cards) {
            ClassFlashcardRowForm row = new ClassFlashcardRowForm();
            row.setId(c.getId());
            row.setFrontText(c.getFrontText());
            row.setBackText(c.getBackText());
            rows.add(row);
        }
        form.setCards(rows);

        model.addAttribute("classEntity", classEntity);
        model.addAttribute("deck", deck);
        model.addAttribute("deckForm", form);
        return "teacher/flashcard-deck-detail";
    }

    @PostMapping("/decks/{deckId}")
    public String saveDeck(@PathVariable Long classId, @PathVariable Long deckId,
                            @Valid @ModelAttribute("deckForm") ClassFlashcardDeckForm form,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        ClassFlashcardDeck deck = ownedDeck(classId, deckId);
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("deck", deck);
            return "teacher/flashcard-deck-detail";
        }
        classFlashcardService.updateDeckAndCards(deckId, form);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã lưu bộ thẻ.");
        return "redirect:/teacher/classes/" + classId + "/flashcards/decks/" + deckId;
    }

    @PostMapping("/decks/{deckId}/delete")
    public String deleteDeck(@PathVariable Long classId, @PathVariable Long deckId, RedirectAttributes redirectAttributes) {
        ownedClass(classId);
        ClassFlashcardDeck deck = ownedDeck(classId, deckId);
        classFlashcardService.deleteDeck(deck);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã xóa bộ thẻ.");
        return "redirect:/teacher/classes/" + classId + "/flashcards";
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/delete")
    public String deleteCard(@PathVariable Long classId, @PathVariable Long deckId, @PathVariable Long cardId,
                              RedirectAttributes redirectAttributes) {
        ownedClass(classId);
        ClassFlashcardDeck deck = ownedDeck(classId, deckId);
        ClassFlashcard card = classFlashcardService.getCard(cardId);
        if (card.getDeck() == null || !card.getDeck().getId().equals(deck.getId())) {
            throw new EntityNotFoundException("Flashcard not found: " + cardId);
        }
        classFlashcardService.deleteCard(card);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã xóa thẻ.");
        return "redirect:/teacher/classes/" + classId + "/flashcards/decks/" + deckId;
    }

    private ClassEntity ownedClass(Long classId) {
        return classService.getOwnedClass(classId, currentUserProvider.getCurrentUser());
    }

    private ClassFlashcardDeck ownedDeck(Long classId, Long deckId) {
        ClassFlashcardDeck deck = classFlashcardService.getDeck(deckId);
        if (deck.getClassEntity() == null || !deck.getClassEntity().getId().equals(classId)) {
            throw new EntityNotFoundException("Class flashcard deck not found: " + deckId);
        }
        return deck;
    }
}
