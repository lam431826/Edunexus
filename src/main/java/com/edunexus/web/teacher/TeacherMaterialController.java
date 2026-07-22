package com.edunexus.web.teacher;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.ClassMaterial;
import com.edunexus.domain.User;
import com.edunexus.dto.ClassMaterialForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ClassMaterialService;
import com.edunexus.service.ClassService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/classes/{classId}/materials")
public class TeacherMaterialController {

    private final ClassMaterialService classMaterialService;
    private final ClassService classService;
    private final CurrentUserProvider currentUserProvider;

    // ---- Class Material list + inline create form ----
    @GetMapping
    public String list(@PathVariable Long classId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("materials", classMaterialService.getByClass(classId));
        model.addAttribute("materialForm", new ClassMaterialForm());
        return "teacher/material-list";
    }

    @PostMapping
    public String create(@PathVariable Long classId, @Valid @ModelAttribute("materialForm") ClassMaterialForm form,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        User teacher = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("materials", classMaterialService.getByClass(classId));
            return "teacher/material-list";
        }
        classMaterialService.create(classEntity, teacher, form);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã thêm tài liệu.");
        return "redirect:/teacher/classes/" + classId + "/materials";
    }

    // ---- Edit ----
    @GetMapping("/{materialId}/edit")
    public String editForm(@PathVariable Long classId, @PathVariable Long materialId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        ClassMaterial material = ownedMaterial(classId, materialId);
        ClassMaterialForm form = new ClassMaterialForm();
        form.setTitle(material.getTitle());
        form.setBodyMarkdown(material.getBodyMarkdown());
        form.setVideoUrl(material.getVideoUrl());
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("material", material);
        model.addAttribute("materialForm", form);
        return "teacher/material-edit";
    }

    @PostMapping("/{materialId}")
    public String update(@PathVariable Long classId, @PathVariable Long materialId,
                          @Valid @ModelAttribute("materialForm") ClassMaterialForm form,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ownedClass(classId);
        ClassMaterial material = ownedMaterial(classId, materialId);
        if (result.hasErrors()) {
            model.addAttribute("material", material);
            return "teacher/material-edit";
        }
        classMaterialService.update(material, form);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã cập nhật tài liệu.");
        return "redirect:/teacher/classes/" + classId + "/materials";
    }

    @PostMapping("/{materialId}/delete")
    public String delete(@PathVariable Long classId, @PathVariable Long materialId, RedirectAttributes redirectAttributes) {
        ownedClass(classId);
        ClassMaterial material = ownedMaterial(classId, materialId);
        classMaterialService.delete(material);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã xóa tài liệu.");
        return "redirect:/teacher/classes/" + classId + "/materials";
    }

    private ClassEntity ownedClass(Long classId) {
        return classService.getOwnedClass(classId, currentUserProvider.getCurrentUser());
    }

    private ClassMaterial ownedMaterial(Long classId, Long materialId) {
        ClassMaterial material = classMaterialService.getById(materialId);
        if (material.getClassEntity() == null || !material.getClassEntity().getId().equals(classId)) {
            throw new EntityNotFoundException("Class material not found: " + materialId);
        }
        return material;
    }
}
