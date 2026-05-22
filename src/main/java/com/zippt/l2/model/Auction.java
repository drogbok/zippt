package com.zippt.l2.model;

import com.zippt.l2.enums.AuctionStatus;
import java.time.LocalDateTime;

/**
 * [L2] Entity : 역경매 공고.
 * <p>
 * project_proposal.md 4.1 상태 전이 : OPEN -> ACTIVE -> AWARDED -> COMPLETED/CANCELLED.
 * <p>
 * <b>동시성 제어</b> : 여러 Agent 가 동시에 SubmitBid 로 접근 시,
 * {@link #registerBid(Bid)} 가 synchronized 되어 순차 처리.
 */
public class Auction {

    private final String id;
    private AuctionStatus status;
    private final LocalDateTime deadline;
    private final User seller;
    private int bidCount;

    public Auction(String id, AuctionStatus status,
                   LocalDateTime deadline, User seller) {
        this.id = id;
        this.status = status;
        this.deadline = deadline;
        this.seller = seller;
        this.bidCount = 0;
    }

    /**
     * A1 가드 : 추가 입찰이 가능한 상태인지 검사.
     * OPEN 또는 ACTIVE 이고, 마감 전인 경우에만 true.
     */
    public boolean acceptsBids() {
        if (status == AuctionStatus.AWARDED
            || status == AuctionStatus.COMPLETED
            || status == AuctionStatus.CANCELLED) {
            return false;
        }
        return LocalDateTime.now().isBefore(deadline);
    }

    /**
     * [동시성 제어] 입찰 건수 증가 + 상태 전이.
     * <p>
     * project_proposal.md : "서버 수신 순서에 따른 순차 처리" 요구사항 반영.
     * synchronized 로 모니터 획득 후 atomic 하게 처리.
     */
    public synchronized void registerBid(Bid bid) {
        this.bidCount++;                                        // Postcondition Q1
        if (this.status == AuctionStatus.OPEN) {
            this.status = AuctionStatus.ACTIVE;                 // Postcondition Q2 (첫 입찰 시)
        }
    }

    public String getId()                { return id; }
    public AuctionStatus getStatus()     { return status; }
    public LocalDateTime getDeadline()   { return deadline; }
    public User getSeller()              { return seller; }
    public int getBidCount()             { return bidCount; }
}
