package com.edunexus.web.sme;

import com.edunexus.domain.Course;
import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.User;
import com.edunexus.dto.QuestionForm;
import com.edunexus.dto.QuestionOptionForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.CourseService;
import com.edunexus.service.QuestionService;
import com.edunexus.service.ai.AiContentService;
import com.edunexus.service.ai.DraftQuestion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sme/questions")
public class SmeQuestionController {

    private final QuestionService questionService;
    private final CourseService courseService;
    private final AiContentService aiContentService;
    private final CurrentUserProvider currentUserProvider;

    private List<Module> myModules() {
        User sme = currentUserProvider.getCurrentUser();
        List<Module> modules = new ArrayList<>();
        for (Course c : courseService.findByOwner(sme)) {
            modules.addAll(courseService.getModules(c));
        }
        return modules;
    }

    // ---- SCR-18 Question Bank ----
    @GetMapping
    public String bank(Model model) {
        User sme = currentUserProvider.getCurrentUser();
        List<Question> all = new ArrayList<>();
        for (Course c : courseService.findByOwner(sme)) {
            all.addAll(questionService.getByCourse(c.getId()));
        }
        model.addAttribute("questions", all);
        return "sme/question-bank";
    }

    // ---- SCR-19 Question Detail ----
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("questionForm", blankFormWithFourOptions());
        model.addAttribute("modules", myModules());
        return "sme/question-detail";
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Question q = questionService.getById(id);
        QuestionForm form = new QuestionForm();
        form.setModuleId(q.getModule().getId());
        form.setText(q.getText());
        form.setDifficulty(q.getDifficulty());
        form.setExplanation(q.getExplanation());
        List<QuestionOptionForm> options = new ArrayList<>();
        for (var opt : q.getOptions()) {
            QuestionOptionForm of = new QuestionOptionForm();
            of.setText(opt.getText());
            of.setCorrect(opt.isCorrect());
            options.add(of);
        }
        form.setOptions(options);
        model.addAttribute("questionId", id);
        model.addAttribute("questionForm", form);
        model.addAttribute("modules", myModules());
        return "sme/question-detail";
    }

    @PostMapping({"/new", "/{id}"})
    public String save(@PathVariable(required = false) Long id, @Valid @ModelAttribute QuestionForm questionForm,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("modules", myModules());
            return "sme/question-detail";
        }
        try {
            Module module = courseService.getModule(questionForm.getModuleId());
            questionService.createOrUpdate(id, module, questionForm);
            redirectAttributes.addFlashAttribute("infoMessage", "Question saved.");
            return "redirect:/sme/questions";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("modules", myModules());
            model.addAttribute("errorMessage", ex.getMessage());
            return "sme/question-detail";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        questionService.delete(id);
        return "redirect:/sme/questions";
    }

    // ---- SCR-20 AI Question Staging ----
    @GetMapping("/ai-staging")
    public String aiStagingForm(Model model) {
        model.addAttribute("modules", myModules());
        return "sme/question-ai-staging";
    }

    @PostMapping("/ai-staging/generate")
    public String generate(@RequestParam Long moduleId, @RequestParam(defaultValue = "5") int count, Model model) {
        Module module = courseService.getModule(moduleId);
        List<DraftQuestion> drafts = aiContentService.generateQuestions(module.getTitle(), count);
        model.addAttribute("modules", myModules());
        model.addAttribute("selectedModuleId", moduleId);
        model.addAttribute("drafts", drafts);
        return "sme/question-ai-staging";
    }

    @PostMapping("/ai-staging/approve")
    public String approveDrafts(@RequestParam Long moduleId,
                                 @RequestParam List<String> draftText,
                                 @RequestParam List<String> draftDifficulty,
                                 @RequestParam List<String> draftOptions,
                                 @RequestParam List<Integer> draftCorrectIndex,
                                 @RequestParam List<String> draftExplanation,
                                 @RequestParam(required = false) List<Integer> selected,
                                 RedirectAttributes redirectAttributes) {
        Module module = courseService.getModule(moduleId);
        int saved = 0;
        for (int i = 0; i < draftText.size(); i++) {
            if (selected != null && !selected.contains(i)) {
                continue;
            }
            List<String> options = List.of(draftOptions.get(i).split("\\|"));
            DraftQuestion draft = new DraftQuestion(
                    draftText.get(i),
                    com.edunexus.domain.enums.Difficulty.valueOf(draftDifficulty.get(i)),
                    options,
                    draftCorrectIndex.get(i),
                    draftExplanation.get(i));
            questionService.saveAiDraft(module, draft);
            saved++;
        }
        redirectAttributes.addFlashAttribute("infoMessage", saved + " question(s) saved to the review queue.");
        return "redirect:/sme/questions";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.approve(questionService.getById(id));
        redirectAttributes.addFlashAttribute("infoMessage", "Question approved.");
        return "redirect:/sme/questions";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.reject(questionService.getById(id));
        redirectAttributes.addFlashAttribute("infoMessage", "Question rejected.");
        return "redirect:/sme/questions";
    }

    // ---- SCR-21 Question Import ----
    @GetMapping("/import")
    public String importForm(Model model) {
        model.addAttribute("modules", myModules());
        return "sme/question-import";
    }

    @PostMapping("/import")
    public String doImport(@RequestParam Long moduleId, @RequestParam MultipartFile file, Model model) {
        Module module = courseService.getModule(moduleId);
        var result = questionService.importFromExcel(module, file);
        model.addAttribute("modules", myModules());
        model.addAttribute("selectedModuleId", moduleId);
        model.addAttribute("result", result);
        return "sme/question-import";
    }

    private QuestionForm blankFormWithFourOptions() {
        QuestionForm form = new QuestionForm();
        List<QuestionOptionForm> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            options.add(new QuestionOptionForm());
        }
        form.setOptions(options);
        return form;
    }
}
