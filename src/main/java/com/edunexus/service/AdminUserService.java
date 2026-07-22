package com.edunexus.service;

import com.edunexus.domain.User;
import com.edunexus.domain.enums.Role;
import com.edunexus.dto.AdminUserForm;
import com.edunexus.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * This service is the ONLY code path allowed to create SME / TEACHER / COURSE_MANAGER / ADMIN
 * accounts (GBR-02) - students self-register elsewhere. Also enforces GBR-01 (unique email).
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    /** Result of a create/reset action: the persisted user plus the one-time plaintext password. */
    public record CreatedAccount(User user, String rawPassword) {
    }

    @Transactional
    public CreatedAccount create(AdminUserForm form) {
        if (form.getRole() == Role.STUDENT) {
            throw new IllegalArgumentException("Học viên tự đăng ký tài khoản, không tạo tại đây.");
        }
        userRepository.findByEmailIgnoreCase(form.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email đã được sử dụng: " + form.getEmail());
        });

        String rawPassword = generatePassword();
        User user = User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(form.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);
        return new CreatedAccount(user, rawPassword);
    }

    @Transactional
    public void disable(Long id) {
        User user = getById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enable(Long id) {
        User user = getById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public String resetPassword(Long id) {
        User user = getById(id);
        String rawPassword = generatePassword();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return rawPassword;
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
