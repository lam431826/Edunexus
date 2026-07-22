package com.edunexus.security;

import com.edunexus.domain.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/** Redirects a freshly authenticated user to their role's home screen (SCR-01 navigation flow). */
public class RoleBasedAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Map<Role, String> HOME_BY_ROLE = new EnumMap<>(Role.class);

    static {
        HOME_BY_ROLE.put(Role.ADMIN, "/admin/dashboard");
        HOME_BY_ROLE.put(Role.COURSE_MANAGER, "/cm/dashboard");
        HOME_BY_ROLE.put(Role.TEACHER, "/teacher/dashboard");
        HOME_BY_ROLE.put(Role.SME, "/sme/courses");
        HOME_BY_ROLE.put(Role.STUDENT, "/student/dashboard");
    }

    /** Shared by AuthController's "already authenticated" redirect so the mapping lives in one place. */
    public static String resolveHomeUrl(Collection<? extends GrantedAuthority> authorities) {
        for (Role role : Role.values()) {
            boolean has = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_" + role.name()));
            if (has) {
                return HOME_BY_ROLE.get(role);
            }
        }
        return "/login";
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String target = resolveHomeUrl(authentication.getAuthorities());
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
