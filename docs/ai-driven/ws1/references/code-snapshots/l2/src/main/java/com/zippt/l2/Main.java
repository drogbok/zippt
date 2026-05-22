package com.zippt.l2;

/**
 * [L2] 엔트리 포인트.
 * <p>
 * Description 에 명시된 기능만 구현되었으며,
 * 실제 실행을 위한 Mock Repository / Queue 구현은 별도 테스트 모듈이 필요함.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("[ZIP-PT L2] SubmitBid use case is implemented.");
        System.out.println("  -> See com.zippt.l2.usecase.SubmitBidUseCase");
        System.out.println("  -> Port implementations (AuctionRepository, BidRepository, "
                         + "NotificationQueue) must be injected by caller.");
    }
}
