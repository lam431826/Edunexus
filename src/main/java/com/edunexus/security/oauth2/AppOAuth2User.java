package com.edunexus.security.oauth2;

import com.edunexus.domain.User;
import com.edunexus.security.AppPrincipal;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Wraps a persisted User (already resolved/linked by email, GBR-01) as the OAuth2 security principal. */
@Getter
public class AppOAuth2User implements OAuth2User, AppPrincipal {

    private final User user;
    private final Map<String, Object> attributes;

    public AppOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
