package com.edunexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Kept separate from SecurityConfig: SecurityConfig depends on CustomOAuth2UserService, which
 * depends on PasswordEncoder - if PasswordEncoder were a @Bean method on SecurityConfig itself,
 * Spring would need a fully-constructed SecurityConfig to produce it while also needing it to
 * construct SecurityConfig, a circular dependency. A standalone config class breaks the cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
