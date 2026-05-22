package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.UserRole;
import java.util.HashSet;
import java.util.Set;

public class Seller extends User {
    private final String sellerGrade;
    private final Set<String> ownedPropertyIds = new HashSet<>();

    public Seller(String userId, String name, String email, String passwordHash, String sellerGrade) {
        super(userId, name, email, passwordHash, UserRole.SELLER);
        this.sellerGrade = sellerGrade;
    }

    public void addOwnedProperty(String propertyId) {
        ownedPropertyIds.add(propertyId);
    }

    public boolean ownsProperty(String propertyId) {
        return ownedPropertyIds.contains(propertyId);
    }

    public String getSellerGrade() { return sellerGrade; }
}


