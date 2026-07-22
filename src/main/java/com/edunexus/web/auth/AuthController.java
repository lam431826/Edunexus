package com.edunexus.web.auth;

import com.edunexus.dto.RegisterForm;
import com.edunexus.security.RoleBasedAuthSuccessHandler;
import com.edunexus.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @GetMapping("/login")
    public String loginPage(Model model,
                             @RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:" + RoleBasedAuthSuccessHandler.resolveHomeUrl(authentication.getAuthorities());
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất thành công.");
        }
        model.addAttribute("googleLoginEnabled", !googleClientId.isBlank());
        return "auth/login";
    }

    // ---- UC-GST-05 Register Account ----
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            registrationService.register(registerForm);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Đăng ký thành công. Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
