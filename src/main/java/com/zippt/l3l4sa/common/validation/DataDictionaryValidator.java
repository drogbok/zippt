package com.zippt.l3l4sa.common.validation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public final class DataDictionaryValidator {
    private static final Pattern REGISTRATION_NUMBER = Pattern.compile("^[A-Z0-9-]{6,30}$");

    private DataDictionaryValidator() {
    }

    public static void requireId(String value, String prefix) {
        if (value == null || value.isBlank() || !value.startsWith(prefix)) {
            throw new DomainValidationException(
                    ValidationErrorCode.ID_INVALID,
                    "ID must start with " + prefix + " and not be blank."
            );
        }
    }

    public static String requireTextLength(String value, int min, int max, ValidationErrorCode code) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() < min || normalized.length() > max) {
            throw new DomainValidationException(code, "Text length must be between " + min + " and " + max + ".");
        }
        return normalized;
    }

    public static void requireDecimalRange(BigDecimal value, BigDecimal min, BigDecimal max,
                                           ValidationErrorCode code) {
        if (value == null || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new DomainValidationException(code, "Decimal value must be between " + min + " and " + max + ".");
        }
    }

    public static void requireScale(BigDecimal value, int maxScale, ValidationErrorCode code) {
        if (value != null && value.stripTrailingZeros().scale() > maxScale) {
            throw new DomainValidationException(code, "Decimal scale must be " + maxScale + " or less.");
        }
    }

    public static void requireFutureWithin(LocalDateTime value, Duration minAfterNow, Duration maxAfterNow,
                                           ValidationErrorCode code) {
        LocalDateTime now = LocalDateTime.now();
        if (value == null
                || value.isBefore(now.plus(minAfterNow))
                || value.isAfter(now.plus(maxAfterNow))) {
            throw new DomainValidationException(code, "datetime is outside the allowed future range.");
        }
    }

    public static void requireRegistrationNumber(String value, ValidationErrorCode code) {
        if (value == null || !REGISTRATION_NUMBER.matcher(value).matches()) {
            throw new DomainValidationException(code, "Registration number must be 6-30 chars of A-Z, 0-9, or hyphen.");
        }
    }

    public static void requireEqual(BigDecimal actual, BigDecimal expected, ValidationErrorCode code) {
        if (actual == null || actual.compareTo(expected) != 0) {
            throw new DomainValidationException(code, "Decimal value must equal " + expected + ".");
        }
    }
}

