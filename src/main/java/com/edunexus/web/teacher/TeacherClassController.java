package com.edunexus.web.teacher;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.ClassFlashcardService;
import com.edunexus.service.ClassMaterialService;
import com.edunexus.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/classes")
public class TeacherClassController {

    private final ClassService classService;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassMaterialService classMaterialService;
    private final ClassFlashcardService classFlashcardService;
    private final AssignmentService assignmentService;
    private final CurrentUserProvider currentUserProvider;

    // ---- Teacher Class List ----
    @GetMapping
    public String list(Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        model.addAttribute("classes", classService.getByTeacher(teacher));
        return "teacher/class-list";
    }

    // ---- Teacher Class Detail: roster + materials + flashcard decks + assignments overview ----
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        ClassEntity classEntity = classService.getOwnedClass(id, teacher);

        List<Enrollment> roster = enrollmentRepository.findByClassEntity_Id(id);

        model.addAttribute("classEntity", classEntity);
        model.addAttribute("roster", roster);
        model.addAttribute("materials", classMaterialService.getByClass(id));
        model.addAttribute("decks", classFlashcardService.getDecksByClass(id));
        model.addAttribute("classAssignments", assignmentService.getByClassScope(id));
        model.addAttribute("courseAssignments", assignmentService.getByCourse(classEntity.getSourceCourse().getId()));
        return "teacher/class-detail";
    }
}
