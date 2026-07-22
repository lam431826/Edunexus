package com.edunexus.web.teacher;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.ClassService;
import com.edunexus.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/dashboard")
public class TeacherDashboardController {

    private final ClassService classService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String dashboard(Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        java.util.List<ClassEntity> classes = classService.getByTeacher(teacher);

        Map<Long, Integer> rosterSizeByClass = new LinkedHashMap<>();
        Map<Long, Long> pendingGradingByClass = new LinkedHashMap<>();

        for (ClassEntity classEntity : classes) {
            rosterSizeByClass.put(classEntity.getId(), enrollmentRepository.findByClassEntity_Id(classEntity.getId()).size());
            pendingGradingByClass.put(classEntity.getId(), (long) countPendingGrading(classEntity));
        }

        model.addAttribute("classes", classes);
        model.addAttribute("rosterSizeByClass", rosterSizeByClass);
        model.addAttribute("pendingGradingByClass", pendingGradingByClass);
        return "teacher/dashboard";
    }

    /** Submissions awaiting Teacher confirmation (GBR-09): AI_SCORED, across a class's own assignments and the source course's assignments. */
    private int countPendingGrading(ClassEntity classEntity) {
        int count = 0;
        for (Assignment a : assignmentService.getByClassScope(classEntity.getId())) {
            count += countAiScored(submissionService.getByAssignment(a));
        }
        for (Assignment a : assignmentService.getByCourse(classEntity.getSourceCourse().getId())) {
            count += countAiScored(submissionService.getByAssignment(a));
        }
        return count;
    }

    private int countAiScored(java.util.List<Submission> submissions) {
        return (int) submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.AI_SCORED).count();
    }
}
