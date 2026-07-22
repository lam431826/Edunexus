package com.edunexus.service;

import com.edunexus.domain.User;
import com.edunexus.domain.enums.AuthProvider;
import com.edunexus.domain.enums.Role;
import com.edunexus.dto.RegisterForm;
import com.edunexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guest self-registration (UC-GST-05). Always creates a STUDENT account - GBR-02 reserves
 * SME/Teacher/Course Manager/Admin provisioning to the Admin user-management screen only.
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        if (userRepository.findByEmailIgnoreCase(form.getEmail()).isPresent()) {
            throw new IllegalStateException(
                    "This email is already registered. Please sign in or use another email.");
        }
        User user = User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .role(Role.STUDENT)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }
}
