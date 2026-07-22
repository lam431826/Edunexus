package com.edunexus.security;

import com.edunexus.domain.User;

/**
 * Common identity accessor implemented by both the form-login principal (AppUserPrincipal) and the
 * Google OAuth2 principal (security.oauth2.AppOAuth2User), so CurrentUserProvider/
 * GlobalModelAttributes can resolve the logged-in User regardless of which login method was used.
 */
public interface AppPrincipal {
    Long getId();

    User getUser();
}
