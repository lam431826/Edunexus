package com.edunexus.security;

import com.edunexus.domain.User;
import com.edunexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AppPrincipal appPrincipal)) {
            throw new IllegalStateException("Unsupported authentication principal: " + principal.getClass());
        }
        return userRepository.findById(appPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
