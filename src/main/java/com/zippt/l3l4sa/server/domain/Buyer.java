package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.UserRole;
import java.math.BigDecimal;

public class Buyer extends User {
    private final String preferredRegion;
    private final BigDecimal preferredPriceMin;
    private final BigDecimal preferredPriceMax;

    public Buyer(String userId, String name, String email, String passwordHash,
                 String preferredRegion, BigDecimal preferredPriceMin, BigDecimal preferredPriceMax) {
        super(userId, name, email, passwordHash, UserRole.BUYER);
        this.preferredRegion = preferredRegion;
        this.preferredPriceMin = preferredPriceMin;
        this.preferredPriceMax = preferredPriceMax;
    }

    public String getPreferredRegion() { return preferredRegion; }
    public BigDecimal getPreferredPriceMin() { return preferredPriceMin; }
    public BigDecimal getPreferredPriceMax() { return preferredPriceMax; }
}


