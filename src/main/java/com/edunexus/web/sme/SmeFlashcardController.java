package com.edunexus.web.sme;

import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.Module;
import com.edunexus.dto.FlashcardDeckForm;
import com.edunexus.dto.FlashcardRowForm;
import com.edunexus.service.CourseService;
import com.edunexus.service.FlashcardService;
import com.edunexus.service.ai.AiContentService;
import com.edunexus.service.ai.DraftFlashcard;
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
@RequestMapping("/sme/flashcards")
public class SmeFlashcardController {

    private final FlashcardService flashcardService;
    private final CourseService courseService;
    private final AiContentService aiContentService;

    // ---- SCR-14 Flashcard Editor ----
    @GetMapping("/decks/{id}")
    public String editor(@PathVariable Long id, Model model) {
        FlashcardDeck deck = flashcardService.getDeck(id);
        List<Flashcard> cards = flashcardService.getCards(deck);
        FlashcardDeckForm form = new FlashcardDeckForm();
        form.setName(deck.getName());
        form.setDescription(deck.getDescription());
        List<FlashcardRowForm> rows = new ArrayList<>();
        for (Flashcard c : cards) {
            FlashcardRowForm row = new FlashcardRowForm();
            row.setId(c.getId());
            row.setFrontText(c.getFrontText());
            row.setBackText(c.getBackText());
            rows.add(row);
        }
        form.setCards(rows);
        model.addAttribute("deck", deck);
        model.addAttribute("deckForm", form);
        return "sme/flashcard-editor";
    }

    @PostMapping("/decks/{id}")
    public String save(@PathVariable Long id, @Valid @ModelAttribute("deckForm") FlashcardDeckForm form,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        FlashcardDeck deck = flashcardService.getDeck(id);
        if (result.hasErrors()) {
            model.addAttribute("deck", deck);
            return "sme/flashcard-editor";
        }
        flashcardService.createOrUpdateDeck(id, deck.getModule(), form);
        redirectAttributes.addFlashAttribute("infoMessage", "Deck saved.");
        return "redirect:/sme/courses/" + deck.getModule().getCourse().getId();
    }

    // ---- SCR-15 AI Flashcard Staging ----
    @GetMapping("/decks/{id}/ai-staging")
    public String aiStagingForm(@PathVariable Long id, Model model) {
        model.addAttribute("deck", flashcardService.getDeck(id));
        return "sme/flashcard-ai-staging";
    }

    @PostMapping("/decks/{id}/ai-staging/generate")
    public String generate(@PathVariable Long id, @RequestParam(defaultValue = "6") int count, Model model) {
        FlashcardDeck deck = flashcardService.getDeck(id);
        List<DraftFlashcard> drafts = aiContentService.generateFlashcards(deck.getName(), count);
        model.addAttribute("deck", deck);
        model.addAttribute("drafts", drafts);
        return "sme/flashcard-ai-staging";
    }

    @PostMapping("/decks/{id}/ai-staging/approve")
    public String approve(@PathVariable Long id,
                           @RequestParam List<String> draftFront,
                           @RequestParam List<String> draftBack,
                           @RequestParam(required = false) List<Integer> selected,
                           RedirectAttributes redirectAttributes) {
        FlashcardDeck deck = flashcardService.getDeck(id);
        List<DraftFlashcard> toSave = new ArrayList<>();
        for (int i = 0; i < draftFront.size(); i++) {
            if (selected != null && !selected.contains(i)) {
                continue;
            }
            toSave.add(new DraftFlashcard(draftFront.get(i), draftBack.get(i)));
        }
        flashcardService.saveAiDrafts(deck, toSave);
        redirectAttributes.addFlashAttribute("infoMessage", toSave.size() + " flashcard(s) added to the deck for review.");
        return "redirect:/sme/courses/" + deck.getModule().getCourse().getId();
    }
}
