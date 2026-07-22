package com.edunexus.web.teacher;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.dto.NotifyForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ClassService;
import com.edunexus.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/classes/{classId}/notify")
public class TeacherNotificationController {

    private final NotificationService notificationService;
    private final ClassService classService;
    private final com.edunexus.repository.EnrollmentRepository enrollmentRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String form(@PathVariable Long classId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("roster", enrollmentRepository.findByClassEntity_Id(classId));
        model.addAttribute("notifications", notificationService.getByClass(classId));
        model.addAttribute("notifyForm", new NotifyForm());
        return "teacher/notify";
    }

    @PostMapping
    public String send(@PathVariable Long classId, @Valid @ModelAttribute("notifyForm") NotifyForm form,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        User teacher = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("roster", enrollmentRepository.findByClassEntity_Id(classId));
            model.addAttribute("notifications", notificationService.getByClass(classId));
            return "teacher/notify";
        }

        User recipient = null;
        if (form.getStudentId() != null) {
            Enrollment enrollment = enrollmentRepository.findByClassEntity_Id(classId).stream()
                    .filter(e -> e.getStudent().getId().equals(form.getStudentId()))
                    .findFirst()
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not enrolled in this class: " + form.getStudentId()));
            recipient = enrollment.getStudent();
        }

        notificationService.send(classEntity, teacher, recipient, form.getMessage());
        redirectAttributes.addFlashAttribute("infoMessage", recipient == null
                ? "Đã gửi thông báo tới cả lớp."
                : "Đã gửi thông báo tới " + recipient.getName() + ".");
        return "redirect:/teacher/classes/" + classId + "/notify";
    }

    private ClassEntity ownedClass(Long classId) {
        return classService.getOwnedClass(classId, currentUserProvider.getCurrentUser());
    }
}
