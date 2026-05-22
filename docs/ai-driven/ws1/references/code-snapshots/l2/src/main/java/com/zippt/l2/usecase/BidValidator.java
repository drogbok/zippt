package com.zippt.l2.usecase;

import com.zippt.l2.model.BidForm;
import com.zippt.l2.exception.BidValidationException;

/**
 * [L2] Validator : 입찰서 유효성 검증 (Description 7단계).
 * Alternative A2 에 대응.
 */
public final class BidValidator {

    private BidValidator() { /* static utility */ }

    public static void validate(BidForm f) throws BidValidationException {
        if (f == null) {
            throw new BidValidationException("form is null");
        }
        if (f.getCommissionRate() < 0.0 || f.getCommissionRate() > 5.0) {
            throw new BidValidationException("commissionRate out of range");
        }
        if (f.getMarketingStrategy() == null || f.getMarketingStrategy().isBlank()) {
            throw new BidValidationException("marketingStrategy is blank");
        }
        if (f.getExpectedSalePeriod() <= 0) {
            throw new BidValidationException("expectedSalePeriod must be positive");
        }
        if (f.getServiceTerms() == null || f.getServiceTerms().isBlank()) {
            throw new BidValidationException("serviceTerms is blank");
        }
    }
}
