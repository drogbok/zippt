package com.zippt.enums;

public enum PropertyType {
    APARTMENT("아파트"),
    VILLA("빌라"),
    OFFICETEL("오피스텔"),
    HOUSE("단독주택"),
    COMMERCIAL("상가");

    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
