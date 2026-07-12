package com.edunexus.web.sme;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.Course;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.Lesson;
import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.User;
import com.edunexus.dto.CourseForm;
import com.edunexus.dto.ModuleForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.CourseService;
import com.edunexus.service.FlashcardService;
import com.edunexus.service.LessonService;
import com.edunexus.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sme")
public class SmeCourseController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final QuestionService questionService;
    private final FlashcardService flashcardService;
    private final AssignmentService assignmentService;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-04 Course List ----
    @GetMapping("/courses")
    public String courseList(Model model) {
        User sme = currentUserProvider.getCurrentUser();
        model.addAttribute("courses", courseService.findByOwner(sme));
        model.addAttribute("courseForm", new CourseForm());
        return "sme/course-list";
    }

    @PostMapping("/courses")
    public String createCourse(@Valid @ModelAttribute CourseForm courseForm, BindingResult result,
                                RedirectAttributes redirectAttributes) {
        User sme = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a course title.");
            return "redirect:/sme/courses";
        }
        Course course = courseService.createCourse(courseForm, sme);
        return "redirect:/sme/courses/" + course.getId();
    }

    // ---- SCR-05 Course Structure ----
    @GetMapping("/courses/{id}")
    public String courseStructure(@PathVariable Long id, Model model) {
        User sme = currentUserProvider.getCurrentUser();
        Course course = courseService.getOwnedCourse(id, sme);
        List<Module> modules = courseService.getModules(course);

        List<Lesson> lessons = lessonService.getByCourse(id);
        List<Question> questions = questionService.getByCourse(id);
        List<FlashcardDeck> decks = flashcardService.getDecksByCourse(id);
        List<Assignment> assignments = assignmentService.getByCourse(id);

        Map<Long, List<Lesson>> lessonsByModule = lessons.stream()
                .collect(Collectors.groupingBy(l -> l.getModule().getId()));
        Map<Long, List<FlashcardDeck>> decksByModule = decks.stream()
                .collect(Collectors.groupingBy(d -> d.getModule().getId()));
        Map<Long, List<Assignment>> assignmentsByModule = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getModule().getId()));
        Map<Long, Long> questionCountByModule = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getModule().getId(), Collectors.counting()));

        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("lessonsByModule", lessonsByModule);
        model.addAttribute("decksByModule", decksByModule);
        model.addAttribute("assignmentsByModule", assignmentsByModule);
        model.addAttribute("questionCountByModule", questionCountByModule);
        model.addAttribute("moduleForm", new ModuleForm());
        return "sme/course-structure";
    }

    @PostMapping("/courses/{id}/modules")
    public String addModule(@PathVariable Long id, @ModelAttribute ModuleForm moduleForm) {
        User sme = currentUserProvider.getCurrentUser();
        Course course = courseService.getOwnedCourse(id, sme);
        courseService.addModule(course, moduleForm);
        return "redirect:/sme/courses/" + id;
    }

    @PostMapping("/courses/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User sme = currentUserProvider.getCurrentUser();
        Course course = courseService.getOwnedCourse(id, sme);
        try {
            courseService.publish(course);
            redirectAttributes.addFlashAttribute("infoMessage", "Course content has been published successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sme/courses/" + id;
    }

    // ---- Quick-create actions from the Course Structure tree ----
    @PostMapping("/modules/{moduleId}/lessons/new")
    public String newLesson(@PathVariable Long moduleId) {
        Module module = courseService.getModule(moduleId);
        com.edunexus.dto.LessonForm form = new com.edunexus.dto.LessonForm();
        form.setTitle("Untitled lesson");
        Lesson lesson = lessonService.createLesson(module, form);
        return "redirect:/sme/lessons/" + lesson.getId();
    }

    @PostMapping("/modules/{moduleId}/flashcard-decks/new")
    public String newDeck(@PathVariable Long moduleId) {
        Module module = courseService.getModule(moduleId);
        com.edunexus.dto.FlashcardDeckForm form = new com.edunexus.dto.FlashcardDeckForm();
        form.setName("Untitled deck");
        FlashcardDeck deck = flashcardService.createOrUpdateDeck(null, module, form);
        return "redirect:/sme/flashcards/decks/" + deck.getId();
    }

    @PostMapping("/modules/{moduleId}/assignments/new")
    public String newAssignment(@PathVariable Long moduleId) {
        Module module = courseService.getModule(moduleId);
        com.edunexus.dto.AssignmentForm form = new com.edunexus.dto.AssignmentForm();
        form.setTitle("Untitled assignment");
        Assignment assignment = assignmentService.createOrUpdate(null, module, form);
        return "redirect:/sme/assignments/" + assignment.getId();
    }

    List<Module> sortedModules(Course course) {
        return courseService.getModules(course).stream()
                .sorted(Comparator.comparingInt(Module::getOrderIndex)).toList();
    }
}
