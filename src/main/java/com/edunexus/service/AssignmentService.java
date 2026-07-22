package com.edunexus.service;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Module;
import com.edunexus.domain.RubricCriterion;
import com.edunexus.dto.AssignmentForm;
import com.edunexus.dto.RubricCriterionForm;
import com.edunexus.repository.AssignmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));
    }

    public List<Assignment> getByOwner(Long ownerId) {
        return assignmentRepository.findByModule_Course_Owner_Id(ownerId);
    }

    public List<Assignment> getByCourse(Long courseId) {
        return assignmentRepository.findByModule_CourseId(courseId);
    }

    @Transactional
    public Assignment createOrUpdate(Long assignmentId, Module module, AssignmentForm form) {
        Assignment assignment = buildAssignment(assignmentId, form);
        assignment.setModule(module);
        assignment.setClassScope(null);
        return assignmentRepository.save(assignment);
    }

    /** Teacher-created class-specific assignment (UC-TEA-04) - scoped to a Class, not the SME's Module. */
    @Transactional
    public Assignment createOrUpdateForClass(Long assignmentId, ClassEntity classEntity, AssignmentForm form) {
        Assignment assignment = buildAssignment(assignmentId, form);
        assignment.setModule(null);
        assignment.setClassScope(classEntity);
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getByClassScope(Long classId) {
        return assignmentRepository.findByClassScope_Id(classId);
    }

    private Assignment buildAssignment(Long assignmentId, AssignmentForm form) {
        int totalWeight = form.getRubricCriteria().stream().mapToInt(RubricCriterionForm::getWeightPercent).sum();
        if (!form.getRubricCriteria().isEmpty() && totalWeight != 100) {
            throw new IllegalArgumentException("Combined rubric weight must equal 100% (currently " + totalWeight + "%).");
        }

        Assignment assignment = assignmentId == null ? new Assignment() : getById(assignmentId);
        assignment.setTitle(form.getTitle());
        assignment.setPromptMarkdown(form.getPromptMarkdown());
        assignment.setDueDate(form.getDueDate());
        assignment.setMaxScore(form.getMaxScore());

        assignment.getRubricCriteria().clear();
        for (RubricCriterionForm rf : form.getRubricCriteria()) {
            if (rf.getName() == null || rf.getName().isBlank()) {
                continue;
            }
            assignment.getRubricCriteria().add(RubricCriterion.builder()
                    .assignment(assignment)
                    .name(rf.getName())
                    .weightPercent(rf.getWeightPercent())
                    .descriptor(rf.getDescriptor())
                    .build());
        }
        return assignment;
    }
}
