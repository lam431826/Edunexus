package com.edunexus.security.oauth2;

import com.edunexus.domain.User;
import com.edunexus.domain.enums.AuthProvider;
import com.edunexus.domain.enums.Role;
import com.edunexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * GBR-01: a Google sign-in whose email already exists links to that account instead of creating a
 * duplicate. GBR-02: internal roles (SME/Teacher/Course Manager/Admin) can never be provisioned via
 * OAuth2 - a brand-new Google sign-in is always created as STUDENT.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account did not return an email address.");
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> userRepository.save(User.builder()
                .name(name != null && !name.isBlank() ? name : email)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.STUDENT)
                .authProvider(AuthProvider.GOOGLE)
                .enabled(true)
                .build()));

        if (!user.isEnabled()) {
            throw new OAuth2AuthenticationException("Your account has been disabled. Please contact the administrator.");
        }

        return new AppOAuth2User(user, oAuth2User.getAttributes());
    }
}
