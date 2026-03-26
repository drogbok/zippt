package com.zippt.enums;

public enum ReservationStatus {
    PENDING("대기"),
    CONFIRMED("확정"),
    REJECTED("거절"),
    VISITED("방문 완료"),
    REVIEWED("후기 완료");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
