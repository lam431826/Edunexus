package com.edunexus.web.admin;

import com.edunexus.domain.User;
import com.edunexus.domain.enums.Role;
import com.edunexus.dto.AdminUserForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.AdminUserService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", adminUserService.getAll());
        model.addAttribute("adminUserForm", new AdminUserForm());
        model.addAttribute("assignableRoles",
                new Role[] { Role.SME, Role.TEACHER, Role.COURSE_MANAGER, Role.ADMIN });
        return "admin/users";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute AdminUserForm adminUserForm, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin hợp lệ.");
            return "redirect:/admin/users";
        }
        try {
            AdminUserService.CreatedAccount created = adminUserService.create(adminUserForm);
            activityLogService.log(admin, "USER_CREATED", "User", created.user().getId(), "SUCCESS",
                    "Tạo tài khoản " + created.user().getRole() + ": " + created.user().getEmail());
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Tài khoản đã tạo cho " + created.user().getEmail()
                            + ". Mật khẩu tạm thời: " + created.rawPassword());
        } catch (IllegalArgumentException ex) {
            activityLogService.log(admin, "USER_CREATED", "User", null, "FAILED", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        adminUserService.disable(id);
        activityLogService.log(admin, "USER_DISABLED", "User", id, "SUCCESS", "Vô hiệu hóa tài khoản #" + id);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã vô hiệu hóa tài khoản.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        adminUserService.enable(id);
        activityLogService.log(admin, "USER_ENABLED", "User", id, "SUCCESS", "Kích hoạt lại tài khoản #" + id);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã kích hoạt lại tài khoản.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        String rawPassword = adminUserService.resetPassword(id);
        activityLogService.log(admin, "USER_PASSWORD_RESET", "User", id, "SUCCESS", "Đặt lại mật khẩu tài khoản #" + id);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã đặt lại mật khẩu. Mật khẩu tạm thời: " + rawPassword);
        return "redirect:/admin/users";
    }
}
