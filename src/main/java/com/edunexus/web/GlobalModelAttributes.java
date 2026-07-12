package com.edunexus.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the signed-in user's display name and current request path available to every Thymeleaf
 * template without per-controller wiring. (Thymeleaf 3.1+ no longer exposes #request by default,
 * so the active-nav-link highlighting in the layout fragments reads this attribute instead.)
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUserName")
    public String currentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof com.edunexus.security.AppUserPrincipal principal)) {
            return "";
        }
        return principal.getUser().getName();
    }

    @ModelAttribute("requestUri")
    public String requestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
