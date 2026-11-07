package com.shopycat.security;

import com.shopycat.model.AppUser;
import com.shopycat.repo.AppUserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Looks up the logged-in account, or returns null for visitors. */
@Component
public class CurrentUser {

    private final AppUserRepository users;

    public CurrentUser(AppUserRepository users) {
        this.users = users;
    }

    public AppUser getOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return users.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    public AppUser require() {
        AppUser user = getOrNull();
        if (user == null) {
            throw new IllegalStateException("Login required");
        }
        return user;
    }
}
