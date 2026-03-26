package com.zippt.enums;

public enum AuctionStatus {
    OPEN("입찰 대기"),
    ACTIVE("입찰 진행 중"),
    AWARDED("낙찰"),
    COMPLETED("거래 완료"),
    CANCELLED("취소");

    private final String displayName;

    AuctionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
