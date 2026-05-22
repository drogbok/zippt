package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.enums.UserRole;
import com.zippt.l3l4sa.server.domain.Agent;
import com.zippt.l3l4sa.server.domain.Buyer;
import com.zippt.l3l4sa.server.domain.Seller;
import com.zippt.l3l4sa.server.domain.User;

public class AuthenticationManager {
    private final DataStore store;

    public AuthenticationManager(DataStore store) {
        this.store = store;
    }

    public User validateUser(String userId, UserRole requiredRole) {
        User user = store.findUser(userId);
        if (user == null || !user.isAuthenticated() || !user.hasRole(requiredRole)) {
            throw new IllegalStateException("Authentication failed for " + userId + " as " + requiredRole);
        }
        return user;
    }

    public Seller validateSeller(String sellerId) {
        return (Seller) validateUser(sellerId, UserRole.SELLER);
    }

    public Agent validateAgent(String agentId) {
        return (Agent) validateUser(agentId, UserRole.AGENT);
    }

    public Buyer validateBuyer(String buyerId) {
        return (Buyer) validateUser(buyerId, UserRole.BUYER);
    }
}


