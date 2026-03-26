package com.zippt.enums;

public enum Role {
    BUYER("매수자"),
    SELLER("매도자"),
    AGENT("중개사");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
