package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.CredentialStatus;
import java.time.LocalDateTime;

public class AgentCredential {
    private final String credentialId;
    private final String agentId;
    private final String licenseNumber;
    private final String officeRegistrationNumber;
    private LocalDateTime verifiedAt;
    private CredentialStatus status;

    public AgentCredential(String credentialId, String agentId, String licenseNumber, String officeRegistrationNumber) {
        this.credentialId = credentialId;
        this.agentId = agentId;
        this.licenseNumber = licenseNumber;
        this.officeRegistrationNumber = officeRegistrationNumber;
        this.status = CredentialStatus.PENDING;
    }

    public void verify() {
        status = CredentialStatus.VERIFIED;
        verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        status = CredentialStatus.REJECTED;
    }

    public boolean isVerified() {
        return status == CredentialStatus.VERIFIED;
    }

    public String getCredentialId() { return credentialId; }
    public String getAgentId() { return agentId; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getOfficeRegistrationNumber() { return officeRegistrationNumber; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public CredentialStatus getStatus() { return status; }
}

