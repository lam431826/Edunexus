package com.edunexus.service;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.User;
import com.edunexus.dto.CourseGroupForm;
import com.edunexus.repository.CourseGroupRepository;
import com.edunexus.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseGroupService {

    private final CourseGroupRepository courseGroupRepository;
    private final UserRepository userRepository;

    public List<CourseGroup> getAll() {
        return courseGroupRepository.findAll();
    }

    public CourseGroup getById(Long id) {
        return courseGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found: " + id));
    }

    public List<CourseGroup> getManagedGroups(User manager) {
        return courseGroupRepository.findByManager(manager);
    }

    /** Enforces GBR-14: a Course Manager may only act within CourseGroups assigned to them. */
    public CourseGroup getManagedGroup(Long id, User manager) {
        CourseGroup group = getById(id);
        if (group.getManager() == null || !group.getManager().getId().equals(manager.getId())) {
            throw new AccessDeniedException("You do not manage this course group.");
        }
        return group;
    }

    public void assertManages(CourseGroup group, User manager) {
        if (group.getManager() == null || !group.getManager().getId().equals(manager.getId())) {
            throw new AccessDeniedException("You do not manage this course group.");
        }
    }

    @Transactional
    public CourseGroup create(CourseGroupForm form) {
        CourseGroup group = CourseGroup.builder()
                .name(form.getName())
                .description(form.getDescription())
                .manager(resolveManager(form.getManagerId()))
                .build();
        return courseGroupRepository.save(group);
    }

    @Transactional
    public CourseGroup update(Long id, CourseGroupForm form) {
        CourseGroup group = getById(id);
        group.setName(form.getName());
        group.setDescription(form.getDescription());
        group.setManager(resolveManager(form.getManagerId()));
        return courseGroupRepository.save(group);
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return userRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + managerId));
    }
}
