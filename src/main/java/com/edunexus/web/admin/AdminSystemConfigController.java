package com.edunexus.web.admin;

import com.edunexus.domain.SystemConfigEntry;
import com.edunexus.domain.User;
import com.edunexus.repository.SystemConfigRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/system-config")
public class AdminSystemConfigController {

    private final SystemConfigRepository systemConfigRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Transactional
    public String list(Model model) {
        seedDefaultsIfEmpty();
        model.addAttribute("entries", systemConfigRepository.findAll());
        return "admin/system-config";
    }

    @PostMapping
    public String save(@RequestParam String configKey,
                        @RequestParam(required = false) String configValue,
                        @RequestParam(required = false) String description,
                        RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        SystemConfigEntry entry = systemConfigRepository.findByConfigKey(configKey)
                .orElseGet(() -> SystemConfigEntry.builder().configKey(configKey).build());
        entry.setConfigValue(configValue);
        if (description != null && !description.isBlank()) {
            entry.setDescription(description);
        }
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setUpdatedBy(admin);
        systemConfigRepository.save(entry);

        activityLogService.log(admin, "SYSTEM_CONFIG_UPDATED", "SystemConfigEntry", entry.getId(), "SUCCESS",
                configKey + " = " + configValue);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã cập nhật cấu hình: " + configKey);
        return "redirect:/admin/system-config";
    }

    private void seedDefaultsIfEmpty() {
        if (systemConfigRepository.count() > 0) {
            return;
        }
        List<SystemConfigEntry> defaults = List.of(
                SystemConfigEntry.builder()
                        .configKey("guest.preview.max.questions")
                        .configValue("10")
                        .description("Số câu hỏi tối đa khách vãng lai được xem thử")
                        .build(),
                SystemConfigEntry.builder()
                        .configKey("guest.preview.max.flashcards")
                        .configValue("5")
                        .description("Số thẻ ghi nhớ tối đa khách vãng lai được xem thử")
                        .build()
        );
        systemConfigRepository.saveAll(defaults);
    }
}
