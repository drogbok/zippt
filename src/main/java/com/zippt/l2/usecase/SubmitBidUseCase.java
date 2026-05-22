package com.zippt.l2.usecase;

import com.zippt.l2.model.*;
import com.zippt.l2.exception.*;
import com.zippt.l2.port.AuctionRepository;
import com.zippt.l2.port.BidRepository;
import com.zippt.l2.port.NotificationQueue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * [L2] Use Case : 입찰 참여 (Submit Bid).
 * <p>
 * project_proposal.md 4.1 동시성 제어 요구사항 반영:
 * <ul>
 *   <li>여러 중개사의 동시 입찰은 {@link Auction#registerBid} 의 synchronized 로 순차 처리.</li>
 *   <li>경매가 AWARDED 로 전이된 후 도착한 입찰은 A5 ({@link ConcurrentBidException}) 로 차단.</li>
 * </ul>
 * <p>
 * Description 1~13단계 / Precondition P1~P4 / Alternative A1~A5 / Postcondition Q1~Q5 매핑.
 */
public class SubmitBidUseCase {

    private static final Logger LOG = Logger.getLogger("SubmitBid");

    private final ValidateUser      validateUser;
    private final AuctionRepository auctionRepo;
    private final BidRepository     bidRepo;
    private final NotificationQueue notificationQueue;

    public SubmitBidUseCase(ValidateUser validateUser,
                            AuctionRepository auctionRepo,
                            BidRepository bidRepo,
                            NotificationQueue notificationQueue) {
        this.validateUser      = validateUser;
        this.auctionRepo       = auctionRepo;
        this.bidRepo           = bidRepo;
        this.notificationQueue = notificationQueue;
    }

    /**
     * 정상 흐름 실행. Description 1~13단계에 대응.
     */
    public BidReceipt execute(User agent, String auctionId, BidForm form)
            throws AuthException,
                   AuctionNotActiveException,
                   BidValidationException,
                   BidStorageException,
                   ConcurrentBidException {

        // =========== Precondition 가드 ===========
        if (agent == null || !agent.isAuthenticated()) {
            throw new AuthException();                                              // P1
        }
        if (!agent.isCertified()) {
            throw new AuthException();                                              // P3
        }
        if (bidRepo.hasActiveBid(agent, auctionId)
                && !bidRepo.isResubmitAllowed(auctionId)) {
            throw new BidValidationException("resubmit not allowed");               // P4
        }

        // ---- Description 1. <<include>> Validate User -----------------------
        validateUser.execute(agent);                                                // 1

        // ---- Description 2~3. 활성 경매 목록 조회 -----------------------------
        List<Auction> actives = auctionRepo.findActive();                           // 2, 3

        // ---- Description 4. 경매 선택 -----------------------------------------
        Auction auction = auctionRepo.findById(auctionId);                          // 4
        if (auction == null || !auction.acceptsBids()) {
            throw new AuctionNotActiveException();                                  // A1 (P2 동시 충족)
        }

        // ---- Description 5. 상세 정보 표시 (프레젠테이션 계층 책임) -----------
        // 본 Use Case 범위 밖이므로 생략                                          // 5

        // ---- Description 6. 입찰서 작성 (form 파라미터) -----------------------  // 6

        // ---- Description 7. 유효성 검증 ---------------------------------------
        BidValidator.validate(form);                                                // 7 (실패 시 A2)

        // ---- Description 8. 최종 제출 확정 ------------------------------------
        // 호출 자체가 확정 의사표시                                               // 8

        // ---- Description 9. 저장소 쓰기 + 타임스탬프 기록 ---------------------
        Bid bid = new Bid(agent, auction, form, LocalDateTime.now());
        try {
            bidRepo.save(bid);                                                      // 9
        } catch (StorageException e) {
            bidRepo.rollback();                                                     // A4-1
            LOG.severe("[SubmitBid] storage failed: " + e.getMessage());            // A4-3
            throw new BidStorageException(e);                                       // A4-2
        }

        // ---- Description 10. 경매 상태 갱신 (동시성 제어 진입점) -------------
        // synchronized 블록 안에서 상태 전이 및 중복 낙찰 방지 검사.
        synchronized (auction) {
            if (!auction.acceptsBids()) {
                // A5 : synchronized 진입 직전에 다른 트랜잭션이 AWARDED 시킨 경우
                bidRepo.rollback();
                LOG.warning("[SubmitBid] concurrent winning detected for auction=" + auctionId);
                throw new ConcurrentBidException();                                 // A5
            }
            auction.registerBid(bid);                                               // 10 (Q1, Q2)
        }

        // ---- Description 11. 접수번호 발급 ------------------------------------
        BidReceipt receipt = new BidReceipt(bid.getId(), bid.getTimestamp());       // 11

        // ---- Description 12. 매도자 알림 큐 등재 ------------------------------
        notificationQueue.enqueue(auction.getSeller(), bid);                        // 12, Q3

        // ---- Description 13. 정상 종료 ----------------------------------------
        return receipt;                                                             // 13
        // Postcondition Q1, Q2, Q3 달성.
    }

    /**
     * Alternative A3 : 6~8단계 진행 중 사용자 취소.
     */
    public void cancel(BidForm draft) throws BidCancelledException {
        if (draft != null) {
            draft.discard();                                                        // A3-1
        }
        // A3-2 : 저장소 변경 없음 — rollback 호출 불필요.
        // Postcondition Q4 달성.
        throw new BidCancelledException();
    }
}
