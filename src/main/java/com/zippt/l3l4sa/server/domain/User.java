package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.AuthStatus;
import com.zippt.l3l4sa.common.enums.UserRole;

public class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private AuthStatus authStatus;

    public User(String userId, String name, String email, String passwordHash, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.authStatus = AuthStatus.LOGGED_OUT;
    }

    public boolean isAuthenticated() {
        return authStatus == AuthStatus.LOGGED_IN;
    }

    public boolean hasRole(UserRole requiredRole) {
        return role == requiredRole;
    }

    public void login() {
        authStatus = AuthStatus.LOGGED_IN;
    }

    public void expireSession() {
        authStatus = AuthStatus.EXPIRED;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public AuthStatus getAuthStatus() { return authStatus; }
}


