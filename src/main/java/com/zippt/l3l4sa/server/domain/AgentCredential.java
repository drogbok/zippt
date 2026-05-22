package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.CredentialStatus;
import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
import java.time.LocalDateTime;

public class AgentCredential {
    private final String credentialId;
    private final String agentId;
    private final String licenseNumber;
    private final String officeRegistrationNumber;
    private LocalDateTime verifiedAt;
    private CredentialStatus status;

    public AgentCredential(String credentialId, String agentId, String licenseNumber, String officeRegistrationNumber) {
        DataDictionaryValidator.requireId(credentialId, "credential-");
        DataDictionaryValidator.requireId(agentId, "agent-");
        DataDictionaryValidator.requireRegistrationNumber(licenseNumber, ValidationErrorCode.LICENSE_NUMBER_INVALID);
        DataDictionaryValidator.requireRegistrationNumber(officeRegistrationNumber, ValidationErrorCode.OFFICE_REG_NO_INVALID);
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

    public void ensureBidEligible() {
        if (!isVerified() || verifiedAt == null) {
            throw new DomainValidationException(
                    ValidationErrorCode.CREDENTIAL_STATUS_INVALID,
                    "Agent credential must be VERIFIED before bidding."
            );
        }
    }

    public String getCredentialId() { return credentialId; }
    public String getAgentId() { return agentId; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getOfficeRegistrationNumber() { return officeRegistrationNumber; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public CredentialStatus getStatus() { return status; }
}


