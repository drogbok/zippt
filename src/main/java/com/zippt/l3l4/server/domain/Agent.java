package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.CredentialStatus;
import com.zippt.l3l4.common.enums.UserRole;
import java.math.BigDecimal;

public class Agent extends User {
    private final String officeName;
    private final String serviceRegion;
    private BigDecimal reputationScore;
    private CredentialStatus credentialStatus;

    public Agent(String userId, String name, String email, String passwordHash,
                 String officeName, String serviceRegion) {
        super(userId, name, email, passwordHash, UserRole.AGENT);
        this.officeName = officeName;
        this.serviceRegion = serviceRegion;
        this.reputationScore = BigDecimal.ZERO;
        this.credentialStatus = CredentialStatus.PENDING;
    }

    public boolean isCredentialVerified() {
        return credentialStatus == CredentialStatus.VERIFIED;
    }

    public void verifyCredential() {
        credentialStatus = CredentialStatus.VERIFIED;
    }

    public void updateReputation(BigDecimal score) {
        reputationScore = score;
    }

    public String getOfficeName() { return officeName; }
    public String getServiceRegion() { return serviceRegion; }
    public BigDecimal getReputationScore() { return reputationScore; }
    public CredentialStatus getCredentialStatus() { return credentialStatus; }
}

