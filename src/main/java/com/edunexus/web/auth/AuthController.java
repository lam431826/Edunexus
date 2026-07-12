package com.edunexus.web.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Model model,
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String logout,
                             Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            boolean isSme = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SME"));
            return "redirect:" + (isSme ? "/sme/courses" : "/student/dashboard");
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất thành công.");
        }
        return "auth/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
