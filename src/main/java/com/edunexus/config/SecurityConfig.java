package com.edunexus.config;

import com.edunexus.security.RoleBasedAuthSuccessHandler;
import com.edunexus.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * Google OAuth2 requires a real client-id/secret (see application.yml) that only the operator
     * can provide. When absent, oauth2Login is skipped entirely so the app still starts and works
     * fully via email/password - Spring Boot's OAuth2 autoconfiguration throws at startup if a
     * registration is declared with a blank client-id, so this must stay conditional.
     */
    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Bean
    public RoleBasedAuthSuccessHandler roleBasedAuthSuccessHandler() {
        return new RoleBasedAuthSuccessHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     RoleBasedAuthSuccessHandler roleBasedAuthSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/catalog/**").permitAll()
                        .requestMatchers("/payment/vnpay/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cm/**").hasRole("COURSE_MANAGER")
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/sme/**").hasRole("SME")
                        .requestMatchers("/checkout/**").hasRole("STUDENT")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/vnpay/ipn"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedAuthSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .rememberMe(remember -> remember.key("edunexus-remember-me").rememberMeParameter("remember-me"));

        if (!googleClientId.isBlank()) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(roleBasedAuthSuccessHandler)
                    .failureUrl("/login?error")
            );
        }

        return http.build();
    }
}
