package com.edunexus.security;

import com.edunexus.domain.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/** Redirects a freshly authenticated user to their role's home screen (SCR-01 navigation flow). */
public class RoleBasedAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        boolean isSme = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + Role.SME.name()));

        String target = isSme ? "/sme/courses" : "/student/dashboard";
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
