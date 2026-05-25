package com.zippt.benchmark;

import com.zippt.l2.enums.AuctionStatus;
import com.zippt.l2.exception.StorageException;
import com.zippt.l2.model.Auction;
import com.zippt.l2.model.Bid;
import com.zippt.l2.model.BidForm;
import com.zippt.l2.model.User;
import com.zippt.l2.port.AuctionRepository;
import com.zippt.l2.port.BidRepository;
import com.zippt.l2.port.NotificationQueue;
import com.zippt.l2.usecase.SubmitBidUseCase;
import com.zippt.l2.usecase.ValidateUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyBenchmark {
    private static final String AUCTION_ID = "auction-concurrency";
    private static final String AGENT_ID = "agent-concurrency";

    public static void main(String[] args) {
        int requestCount = args.length > 0 ? Integer.parseInt(args[0]) : 100;

        System.out.println("============================================================");
        System.out.println("ZIP-PT 멀티스레딩/동시성 단계별 비교");
        System.out.println("동시 요청 수: " + requestCount + "건");
        System.out.println("시나리오: 동일 agentId가 동일 auctionId에 동시에 입찰 제출");
        System.out.println("목표: 활성 입찰은 최종 1건만 남아야 함");
        System.out.println("============================================================");

        StageResult l2 = runL2DuplicateBidRace(requestCount);
        StageResult l3l4 = runL3L4DuplicateBidRace(requestCount);
        StageResult sa = runSaDuplicateBidRace(requestCount);

        System.out.println();
        System.out.println("[요약]");
        printStageSummary("L2 baseline", l2, "유스케이스 흐름은 있으나 중복 검증과 저장이 같은 임계 영역에 묶이지 않음");
        printStageSummary("L3+L4", l3l4, "SubmitBidManager 책임으로 auctionId 단위 처리 지점이 구조화됨");
        printStageSummary("L3+L4+SA", sa, "동시 요청 원자성을 품질 기준으로 명시하고 성공 1건/거부 N-1건을 검증");
        System.out.println();
        System.out.println("발표 해석: L2는 기능 흐름 중심이라 동시 요청에서 race condition이 드러난다.");
        System.out.println("발표 해석: L3+L4는 책임 경계를 분리하고, SA는 이를 운영에 가까운 원자성/일관성 기준으로 고정한다.");
        System.out.println("한계: 단일 JVM 인메모리 검증이므로 실제 운영에는 DB transaction, unique constraint, 분산락, 장애 복구가 추가로 필요하다.");
    }

    private static StageResult runL2DuplicateBidRace(int requestCount) {
        System.out.println();
        System.out.println("[L2] 동시 중복 입찰 취약점 확인");
        System.out.println("설명: L2 SubmitBidUseCase는 hasActiveBid 검증 후 save를 수행하고, 이후 auction lock에 진입합니다.");
        System.out.println("관찰: 모든 스레드가 동시에 hasActiveBid=false를 본 뒤 저장하면 중복 입찰이 남을 수 있습니다.");

        User seller = new User("seller-l2", "seller", true, true);
        User agent = new User(AGENT_ID, "agent", true, true);
        Auction auction = new Auction(AUCTION_ID, AuctionStatus.OPEN, LocalDateTime.now().plusHours(1), seller);
        RaceyBidRepository bidRepository = new RaceyBidRepository(requestCount);
        SubmitBidUseCase useCase = new SubmitBidUseCase(
                new ValidateUser(),
                new SingleAuctionRepository(auction),
                bidRepository,
                (targetSeller, bid) -> { }
        );

        StageResult result = runConcurrently(requestCount, () -> useCase.execute(
                agent,
                AUCTION_ID,
                new BidForm(3.5, "L2 concurrency test", 30, "standard service")
        ));
        int duplicateCount = Math.max(0, bidRepository.savedCount() - 1);
        System.out.println("  결과: 성공 " + result.successCount() + "건, 거부 " + result.failureCount() + "건");
        System.out.println("  저장 상태: 동일 auctionId + agentId 입찰 " + bidRepository.savedCount()
                + "건, 중복 " + duplicateCount + "건");
        System.out.println("  L2 경매 bidCount: " + auction.getBidCount());
        return result.withSavedCount(bidRepository.savedCount());
    }

    private static StageResult runL3L4DuplicateBidRace(int requestCount) {
        System.out.println();
        System.out.println("[L3+L4] 동시 중복 입찰 구조화 확인");
        System.out.println("설명: SubmitBidManager가 auctionId 단위 lock 안에서 검증과 저장을 처리합니다.");

        com.zippt.l3l4.server.service.DataStore store = createL3L4AuctionStore(
                AUCTION_ID, LocalDateTime.now().plusHours(2));
        com.zippt.l3l4.server.service.SubmitBidManager manager =
                new com.zippt.l3l4.server.service.SubmitBidManager(
                        store,
                        new com.zippt.l3l4.server.service.AuthenticationManager(store)
                );

        StageResult result = runConcurrently(requestCount, () -> manager.submitBid(
                new com.zippt.l3l4.common.command.Commands.SubmitBidCommand(
                        AGENT_ID,
                        AUCTION_ID,
                        new com.zippt.l3l4.common.command.Commands.BidProposalInput(
                                BigDecimal.valueOf(3.5),
                                "L3L4 concurrency test",
                                30,
                                "standard service"
                        )
                )
        ));
        long savedCount = store.bids().stream()
                .filter(bid -> bid.belongsToAuction(AUCTION_ID))
                .filter(bid -> bid.submittedBy(AGENT_ID))
                .count();
        System.out.println("  결과: 성공 " + result.successCount() + "건, 거부 " + result.failureCount() + "건");
        System.out.println("  저장 상태: 동일 auctionId + agentId 입찰 " + savedCount + "건");
        return result.withSavedCount((int) savedCount);
    }

    private static StageResult runSaDuplicateBidRace(int requestCount) {
        System.out.println();
        System.out.println("[L3+L4+SA] 동시 중복 입찰 원자성 확인");
        System.out.println("설명: SA는 동일 경매/동일 중개사 동시 요청에서도 활성 입찰 1건만 허용하는 품질 기준을 둡니다.");

        com.zippt.l3l4sa.server.service.DataStore store = createSaAuctionStore(
                AUCTION_ID, LocalDateTime.now().plusHours(2));
        com.zippt.l3l4sa.server.service.SubmitBidManager manager =
                new com.zippt.l3l4sa.server.service.SubmitBidManager(
                        store,
                        new com.zippt.l3l4sa.server.service.AuthenticationManager(store)
                );

        StageResult result = runConcurrently(requestCount, () -> manager.submitBid(
                new com.zippt.l3l4sa.common.command.Commands.SubmitBidCommand(
                        AGENT_ID,
                        AUCTION_ID,
                        new com.zippt.l3l4sa.common.command.Commands.BidProposalInput(
                        BigDecimal.valueOf(3.5),
                                "SA concurrency benchmark marketing strategy",
                                30,
                                "standard service terms"
                        )
                )
        ));
        long savedCount = store.findBidsByAuction(AUCTION_ID).stream()
                .filter(bid -> bid.submittedBy(AGENT_ID))
                .count();
        System.out.println("  결과: 성공 " + result.successCount() + "건, 거부 " + result.failureCount() + "건");
        System.out.println("  저장 상태: 동일 auctionId + agentId 입찰 " + savedCount + "건");
        return result.withSavedCount((int) savedCount);
    }

    private static StageResult runConcurrently(int requestCount, ThrowingRunnable action) {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<String> failureMessages = new ArrayList<>();
        long startedAt = System.nanoTime();

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    synchronized (failureMessages) {
                        failureMessages.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                    failureCount.incrementAndGet();
                }
            });
        }

        try {
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            throw new IllegalStateException("Concurrent benchmark interrupted.", e);
        }

        if (!failureMessages.isEmpty()) {
            System.out.println("  대표 거부 메시지: " + failureMessages.get(0));
        }
        return new StageResult(successCount.get(), failureCount.get(), 0, System.nanoTime() - startedAt);
    }

    private static com.zippt.l3l4.server.service.DataStore createL3L4AuctionStore(
            String auctionId, LocalDateTime deadline) {
        com.zippt.l3l4.server.service.DataStore store = new com.zippt.l3l4.server.service.DataStore();
        com.zippt.l3l4.server.domain.Seller seller = new com.zippt.l3l4.server.domain.Seller(
                "seller-l3l4", "seller", "seller@zippt.test", "hash", "STANDARD");
        seller.login();
        store.saveUser(seller);
        com.zippt.l3l4.server.domain.Agent agent = new com.zippt.l3l4.server.domain.Agent(
                AGENT_ID, "agent", "agent@zippt.test", "hash", "office", "REGION");
        agent.login();
        agent.verifyCredential();
        store.saveUser(agent);
        com.zippt.l3l4.server.domain.AgentCredential credential =
                new com.zippt.l3l4.server.domain.AgentCredential("credential-l3l4", AGENT_ID, "LIC-123456", "OFF-123456");
        credential.verify();
        store.saveCredential(credential);

        com.zippt.l3l4.server.domain.Auction auction = new com.zippt.l3l4.server.domain.Auction(
                auctionId,
                "property-l3l4",
                seller.getUserId(),
                new com.zippt.l3l4.server.domain.AuctionCondition(
                        "condition-l3l4", auctionId, "service condition", "verified agent", deadline),
                new com.zippt.l3l4.server.domain.WinnerSelectionCriteria(
                        "criteria-l3l4",
                        auctionId,
                        com.zippt.l3l4.common.enums.WinnerPriorityType.BALANCED,
                        BigDecimal.valueOf(0.5),
                        BigDecimal.valueOf(0.5)
                )
        );
        auction.open();
        store.saveAuction(auction);
        return store;
    }

    private static com.zippt.l3l4sa.server.service.DataStore createSaAuctionStore(
            String auctionId, LocalDateTime deadline) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        com.zippt.l3l4sa.server.domain.Seller seller = new com.zippt.l3l4sa.server.domain.Seller(
                "seller-sa", "seller", "seller@zippt.test", "hash", "STANDARD");
        seller.login();
        store.saveUser(seller);
        com.zippt.l3l4sa.server.domain.Agent agent = new com.zippt.l3l4sa.server.domain.Agent(
                AGENT_ID, "agent", "agent@zippt.test", "hash", "office", "REGION");
        agent.login();
        agent.verifyCredential();
        store.saveUser(agent);
        com.zippt.l3l4sa.server.domain.AgentCredential credential =
                new com.zippt.l3l4sa.server.domain.AgentCredential("credential-sa", AGENT_ID, "LIC-123456", "OFF-123456");
        credential.verify();
        store.saveCredential(credential);

        com.zippt.l3l4sa.server.domain.Auction auction = new com.zippt.l3l4sa.server.domain.Auction(
                auctionId,
                "property-sa",
                seller.getUserId(),
                new com.zippt.l3l4sa.server.domain.AuctionCondition(
                        "condition-sa", auctionId, "기본 중개 서비스 조건입니다.", "검증된 중개사", deadline),
                new com.zippt.l3l4sa.server.domain.WinnerSelectionCriteria(
                        "criteria-sa",
                        auctionId,
                        com.zippt.l3l4sa.common.enums.WinnerPriorityType.BALANCED,
                        BigDecimal.valueOf(0.5),
                        BigDecimal.valueOf(0.5)
                )
        );
        auction.open();
        store.saveAuction(auction);
        return store;
    }

    private static void printStageSummary(String stage, StageResult result, String interpretation) {
        double elapsedMillis = result.elapsedNanos() / 1_000_000.0;
        System.out.printf("- %s: success=%d, rejected=%d, saved=%d, elapsed=%.3f ms%n",
                stage, result.successCount(), result.failureCount(), result.savedCount(), elapsedMillis);
        System.out.println("  " + interpretation);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record StageResult(int successCount, int failureCount, int savedCount, long elapsedNanos) {
        StageResult withSavedCount(int newSavedCount) {
            return new StageResult(successCount, failureCount, newSavedCount, elapsedNanos);
        }
    }

    private static class SingleAuctionRepository implements AuctionRepository {
        private final Auction auction;

        SingleAuctionRepository(Auction auction) {
            this.auction = auction;
        }

        @Override
        public List<Auction> findActive() {
            return List.of(auction);
        }

        @Override
        public Auction findById(String auctionId) {
            return auction.getId().equals(auctionId) ? auction : null;
        }
    }

    private static class RaceyBidRepository implements BidRepository {
        private final List<Bid> bids = new ArrayList<>();
        private final CountDownLatch allThreadsAtDuplicateCheck;

        RaceyBidRepository(int requestCount) {
            this.allThreadsAtDuplicateCheck = new CountDownLatch(requestCount);
        }

        @Override
        public void save(Bid bid) throws StorageException {
            synchronized (bids) {
                bids.add(bid);
            }
        }

        @Override
        public void rollback() {
        }

        @Override
        public boolean hasActiveBid(User agent, String auctionId) {
            allThreadsAtDuplicateCheck.countDown();
            try {
                allThreadsAtDuplicateCheck.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted during duplicate check.", e);
            }
            // L2 취약점 시연: 검증과 저장이 원자적으로 묶이지 않으면 모든 스레드가 동시에 false를 관찰할 수 있다.
            return bids.stream()
                    .anyMatch(bid -> bid.getAgent().getId().equals(agent.getId())
                            && bid.getAuction().getId().equals(auctionId));
        }

        @Override
        public boolean isResubmitAllowed(String auctionId) {
            return false;
        }

        int savedCount() {
            synchronized (bids) {
                return bids.size();
            }
        }
    }
}
