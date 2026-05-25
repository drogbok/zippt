package com.zippt.benchmark;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SaNfrBenchmark {
    private static final String BUYER_ID = "buyer-benchmark";
    private static final String TARGET_REGION = "TARGET_REGION";
    private static final String POPULAR_REGION = "REGION_7";
    private static final String TARGET_AUCTION_ID = "auction-target";
    private static final String TARGET_AGENT_ID = "agent-target";
    private static final int RESULT_LIMIT = 20;

    public static void main(String[] args) {
        int itemCount = resolveItemCount(args);

        System.out.println("============================================================");
        System.out.println("ZIP-PT SA NFR 비교 테스트");
        System.out.println("데이터 크기: " + String.format("%,d", itemCount) + "건");
        System.out.println("실행 옵션: java ... com.zippt.benchmark.SaNfrBenchmark [건수]");
        System.out.println("예: 10,000,000건 시연 -> ... SaNfrBenchmark 10000000");
        System.out.println("============================================================");

        runPropertySearchTest(itemCount);
        runBidLookupTest(itemCount);
        runAgentBidLookupTest(itemCount);
        runBoundedSearchResultTest(itemCount);
        runConcurrentDuplicateBidTest();
        runConcurrentWinnerSelectionTest();
    }

    private static int resolveItemCount(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        return Integer.getInteger("zippt.benchmark.size", 1_000_000);
    }

    private static void runPropertySearchTest(int itemCount) {
        System.out.println();
        System.out.println("[TEST-1] 성능 NFR-P1: 매물 지역 검색 응답성");
        System.out.println("목표: 동일 조건 검색에서 L3+L4 전체 순회와 SA 지역 인덱스 조회 시간을 비교합니다.");
        System.out.println("검색 조건: region = " + TARGET_REGION + " (마지막 1건만 매칭)");

        long l3Time = benchmarkL3L4PropertySearch(itemCount);
        long saTime = benchmarkSaPropertySearch(itemCount);

        printComparison("매물 지역 검색", l3Time, saTime);
    }

    private static long benchmarkL3L4PropertySearch(int itemCount) {
        com.zippt.l3l4.server.service.DataStore store = new com.zippt.l3l4.server.service.DataStore();
        com.zippt.l3l4.server.domain.Buyer buyer = new com.zippt.l3l4.server.domain.Buyer(
                BUYER_ID, "benchmark buyer", "buyer@zippt.test", "hash",
                TARGET_REGION, BigDecimal.ZERO, BigDecimal.TEN
        );
        buyer.login();
        store.saveUser(buyer);
        seedL3L4Properties(store, itemCount);

        com.zippt.l3l4.server.service.SearchPropertyManager manager =
                new com.zippt.l3l4.server.service.SearchPropertyManager(
                        store,
                        new com.zippt.l3l4.server.service.AuthenticationManager(store)
                );
        com.zippt.l3l4.common.command.Commands.SearchPropertyCommand command =
                new com.zippt.l3l4.common.command.Commands.SearchPropertyCommand(
                        BUYER_ID,
                        new com.zippt.l3l4.common.command.Commands.PropertyConditionInput(
                                TARGET_REGION, null, null, null, null, null, null
                        )
                );

        long startedAt = System.nanoTime();
        int resultCount = manager.search(command).propertyIds().size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  L3+L4 baseline 결과: " + resultCount + "건, 전체 properties() stream/filter 수행");
        return elapsed;
    }

    private static long benchmarkSaPropertySearch(int itemCount) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        com.zippt.l3l4sa.server.domain.Buyer buyer = new com.zippt.l3l4sa.server.domain.Buyer(
                BUYER_ID, "benchmark buyer", "buyer@zippt.test", "hash",
                TARGET_REGION, BigDecimal.ZERO, BigDecimal.TEN
        );
        buyer.login();
        store.saveUser(buyer);
        seedSaProperties(store, itemCount);

        com.zippt.l3l4sa.server.service.SearchPropertyManager manager =
                new com.zippt.l3l4sa.server.service.SearchPropertyManager(
                        store,
                        new com.zippt.l3l4sa.server.service.AuthenticationManager(store)
                );
        com.zippt.l3l4sa.common.command.Commands.SearchPropertyCommand command =
                new com.zippt.l3l4sa.common.command.Commands.SearchPropertyCommand(
                        BUYER_ID,
                        new com.zippt.l3l4sa.common.command.Commands.PropertyConditionInput(
                                TARGET_REGION, null, null, null, null, null, null
                        )
                );

        long startedAt = System.nanoTime();
        int resultCount = manager.search(command).propertyIds().size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  SA NFR 결과: " + resultCount + "건, findPropertyIdsByRegion(region) 인덱스 조회 수행");
        System.out.println("  SA 보조 로그 수: " + store.operationLogs().size() + "건");
        return elapsed;
    }

    private static void seedL3L4Properties(com.zippt.l3l4.server.service.DataStore store, int itemCount) {
        BigDecimal area = BigDecimal.valueOf(84);
        BigDecimal price = BigDecimal.valueOf(500_000_000L);
        for (int i = 0; i < itemCount; i++) {
            String region = i == itemCount - 1 ? TARGET_REGION : "REGION_" + (i % 1_000);
            store.saveProperty(new com.zippt.l3l4.server.domain.Property(
                    "property-" + i,
                    "seller-1",
                    "address-" + i,
                    region,
                    area,
                    price,
                    com.zippt.l3l4.common.enums.PropertyType.APARTMENT,
                    "benchmark property"
            ));
        }
    }

    private static void seedSaProperties(com.zippt.l3l4sa.server.service.DataStore store, int itemCount) {
        BigDecimal area = BigDecimal.valueOf(84);
        BigDecimal price = BigDecimal.valueOf(500_000_000L);
        for (int i = 0; i < itemCount; i++) {
            String region = i == itemCount - 1 ? TARGET_REGION : "REGION_" + (i % 1_000);
            store.saveProperty(new com.zippt.l3l4sa.server.domain.Property(
                    "property-" + i,
                    "seller-1",
                    "address-" + i,
                    region,
                    area,
                    price,
                    com.zippt.l3l4sa.common.enums.PropertyType.APARTMENT,
                    "benchmark property"
            ));
        }
    }

    private static void runBidLookupTest(int itemCount) {
        System.out.println();
        System.out.println("[TEST-2] 성능 NFR-P2: 경매별 입찰 조회 효율");
        System.out.println("목표: 특정 auctionId의 입찰 조회에서 L3+L4 전체 Bid 순회와 SA auctionId 인덱스 조회 시간을 비교합니다.");
        System.out.println("검색 조건: auctionId = " + TARGET_AUCTION_ID + " (마지막 1건만 매칭)");

        long l3Time = benchmarkL3L4BidLookup(itemCount);
        long saTime = benchmarkSaBidLookup(itemCount);

        printComparison("경매별 입찰 조회", l3Time, saTime);
    }

    private static void runAgentBidLookupTest(int itemCount) {
        System.out.println();
        System.out.println("[TEST-3] 성능 NFR-P2: 중개사별 입찰 이력 조회 효율");
        System.out.println("목표: 특정 agentId의 입찰 이력 조회에서 L3+L4 전체 Bid 순회와 SA agentId 인덱스 조회 시간을 비교합니다.");
        System.out.println("검색 조건: agentId = " + TARGET_AGENT_ID + " (마지막 1건만 매칭)");

        long l3Time = benchmarkL3L4AgentBidLookup(itemCount);
        long saTime = benchmarkSaAgentBidLookup(itemCount);

        printComparison("중개사별 입찰 이력 조회", l3Time, saTime);
    }

    private static long benchmarkL3L4AgentBidLookup(int itemCount) {
        com.zippt.l3l4.server.service.DataStore store = new com.zippt.l3l4.server.service.DataStore();
        seedL3L4Bids(store, itemCount);

        long startedAt = System.nanoTime();
        int resultCount = store.bids().stream()
                .filter(bid -> bid.submittedBy(TARGET_AGENT_ID))
                .toList()
                .size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  L3+L4 baseline 결과: " + resultCount + "건, 전체 bids() stream/filter 수행");
        return elapsed;
    }

    private static long benchmarkSaAgentBidLookup(int itemCount) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        seedSaBids(store, itemCount);

        long startedAt = System.nanoTime();
        int resultCount = store.findBidsByAgent(TARGET_AGENT_ID).size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  SA NFR 결과: " + resultCount + "건, findBidsByAgent(agentId) 인덱스 조회 수행");
        return elapsed;
    }

    private static void runBoundedSearchResultTest(int itemCount) {
        System.out.println();
        System.out.println("[TEST-4] 보조 성능 확인: 검색 결과 상한 적용");
        System.out.println("목표: NFR-P1 매물 검색 인덱스를 사용할 때 필요한 " + RESULT_LIMIT + "건만 반환해 응답 크기를 제한하는지 비교합니다.");
        System.out.println("검색 조건: region = " + POPULAR_REGION + ", limit = " + RESULT_LIMIT);

        long l3Time = benchmarkL3L4BoundedPropertySearch(itemCount);
        long saTime = benchmarkSaBoundedPropertySearch(itemCount);

        printComparison("검색 결과 상한 적용", l3Time, saTime);
    }

    private static long benchmarkL3L4BoundedPropertySearch(int itemCount) {
        com.zippt.l3l4.server.service.DataStore store = new com.zippt.l3l4.server.service.DataStore();
        seedL3L4Properties(store, itemCount);

        long startedAt = System.nanoTime();
        int resultCount = store.properties().stream()
                .filter(property -> POPULAR_REGION.equals(property.getRegion()))
                .map(property -> property.getPropertyId())
                .limit(RESULT_LIMIT)
                .toList()
                .size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  L3+L4 baseline 결과: " + resultCount + "건, 전체 properties() stream/filter 이후 limit 적용");
        return elapsed;
    }

    private static long benchmarkSaBoundedPropertySearch(int itemCount) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        seedSaProperties(store, itemCount);

        long startedAt = System.nanoTime();
        int resultCount = store.findPropertyIdsByRegion(POPULAR_REGION, RESULT_LIMIT).size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  SA NFR 결과: " + resultCount + "건, 지역 인덱스에서 필요한 수만 제한 조회");
        return elapsed;
    }

    private static void runConcurrentDuplicateBidTest() {
        System.out.println();
        System.out.println("[TEST-5] 동시성 NFR-C1: 동일 경매 중복 입찰 원자성");
        System.out.println("목표: L2부터 존재한 동시 입찰 시나리오에서 같은 agentId의 동시 요청 20개 중 1건만 성공하는지 확인합니다.");

        com.zippt.l3l4sa.server.service.DataStore store = createSaAuctionStore(
                TARGET_AUCTION_ID, LocalDateTime.now().plusHours(2), true);
        com.zippt.l3l4sa.server.service.SubmitBidManager manager =
                new com.zippt.l3l4sa.server.service.SubmitBidManager(
                        store,
                        new com.zippt.l3l4sa.server.service.AuthenticationManager(store)
                );
        int requestCount = 20;
        ConcurrentResult result = runConcurrently(requestCount, () -> manager.submitBid(
                new com.zippt.l3l4sa.common.command.Commands.SubmitBidCommand(
                        TARGET_AGENT_ID,
                        TARGET_AUCTION_ID,
                        new com.zippt.l3l4sa.common.command.Commands.BidProposalInput(
                                BigDecimal.valueOf(3.5),
                                "동시 입찰 검증용 마케팅 전략입니다.",
                                30,
                                "동시 입찰 검증용 서비스 조건입니다."
                        )
                )
        ));

        long savedBids = store.findBidsByAuction(TARGET_AUCTION_ID).stream()
                .filter(bid -> bid.submittedBy(TARGET_AGENT_ID))
                .count();
        System.out.println("  SA NFR 결과: 요청 " + requestCount + "건 중 성공 " + result.successCount()
                + "건, 거부 " + result.failureCount() + "건");
        System.out.println("  저장 상태: 동일 auctionId + agentId 활성 입찰 " + savedBids + "건");
    }

    private static void runConcurrentWinnerSelectionTest() {
        System.out.println();
        System.out.println("[TEST-6] 동시성 NFR-C2: 동시 낙찰 선택 일관성");
        System.out.println("목표: L2부터 존재한 중복 낙찰 방지 시나리오에서 동시 낙찰 요청 20개 중 1건만 성공하는지 확인합니다.");

        com.zippt.l3l4sa.server.service.DataStore store = createSaAuctionStore(
                TARGET_AUCTION_ID, LocalDateTime.now().minusMinutes(1), false);
        com.zippt.l3l4sa.server.domain.Auction auction = store.findAuction(TARGET_AUCTION_ID);
        auction.close();
        String selectedBidId = "bid-winner";
        store.saveBid(new com.zippt.l3l4sa.server.domain.Bid(
                selectedBidId,
                TARGET_AUCTION_ID,
                TARGET_AGENT_ID,
                new com.zippt.l3l4sa.server.domain.BidProposal(
                        "proposal-winner",
                        selectedBidId,
                        BigDecimal.valueOf(3.5),
                        "낙찰 동시성 검증용 마케팅 전략입니다.",
                        30,
                        "낙찰 동시성 검증용 서비스 조건입니다."
                )
        ));
        com.zippt.l3l4sa.server.service.SelectWinnerManager manager =
                new com.zippt.l3l4sa.server.service.SelectWinnerManager(
                        store,
                        new com.zippt.l3l4sa.server.service.AuthenticationManager(store),
                        new com.zippt.l3l4sa.server.control.AuctionLifecycleControl()
                );

        int requestCount = 20;
        ConcurrentResult result = runConcurrently(requestCount, () -> manager.selectWinner(
                new com.zippt.l3l4sa.common.command.Commands.SelectWinnerCommand(
                        "seller-benchmark",
                        TARGET_AUCTION_ID,
                        selectedBidId
                )
        ));

        System.out.println("  SA NFR 결과: 요청 " + requestCount + "건 중 성공 " + result.successCount()
                + "건, 거부 " + result.failureCount() + "건");
        System.out.println("  저장 상태: auction.status=" + auction.getStatus()
                + ", selectedBidId=" + auction.getSelectedBidId());
    }

    private static com.zippt.l3l4sa.server.service.DataStore createSaAuctionStore(
            String auctionId, LocalDateTime deadline, boolean openAuction) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        com.zippt.l3l4sa.server.domain.Seller seller = new com.zippt.l3l4sa.server.domain.Seller(
                "seller-benchmark", "benchmark seller", "seller@zippt.test", "hash", "STANDARD");
        seller.login();
        store.saveUser(seller);

        com.zippt.l3l4sa.server.domain.Agent agent = new com.zippt.l3l4sa.server.domain.Agent(
                TARGET_AGENT_ID, "benchmark agent", "agent@zippt.test", "hash", "benchmark office", TARGET_REGION);
        agent.login();
        agent.verifyCredential();
        store.saveUser(agent);
        com.zippt.l3l4sa.server.domain.AgentCredential credential =
                new com.zippt.l3l4sa.server.domain.AgentCredential(
                        "credential-benchmark", TARGET_AGENT_ID, "LIC-123456", "OFF-123456");
        credential.verify();
        store.saveCredential(credential);

        com.zippt.l3l4sa.server.domain.Auction auction = new com.zippt.l3l4sa.server.domain.Auction(
                auctionId,
                "property-benchmark",
                seller.getUserId(),
                new com.zippt.l3l4sa.server.domain.AuctionCondition(
                        "condition-benchmark",
                        auctionId,
                        "기본 중개 서비스 조건입니다.",
                        "검증된 중개사",
                        deadline
                ),
                new com.zippt.l3l4sa.server.domain.WinnerSelectionCriteria(
                        "criteria-benchmark",
                        auctionId,
                        com.zippt.l3l4sa.common.enums.WinnerPriorityType.BALANCED,
                        BigDecimal.valueOf(0.5),
                        BigDecimal.valueOf(0.5)
                )
        );
        if (openAuction) {
            auction.open();
        }
        store.saveAuction(auction);
        return store;
    }

    private static ConcurrentResult runConcurrently(int requestCount, Runnable action) {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<String> failureMessages = new ArrayList<>();

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
        return new ConcurrentResult(successCount.get(), failureCount.get());
    }

    private record ConcurrentResult(int successCount, int failureCount) {
    }

    private static long benchmarkL3L4BidLookup(int itemCount) {
        com.zippt.l3l4.server.service.DataStore store = new com.zippt.l3l4.server.service.DataStore();
        seedL3L4Bids(store, itemCount);
        com.zippt.l3l4.server.control.AuctionQueryControl queryControl =
                new com.zippt.l3l4.server.control.AuctionQueryControl(store);

        long startedAt = System.nanoTime();
        int resultCount = queryControl.getBidsForAuction(TARGET_AUCTION_ID).size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  L3+L4 baseline 결과: " + resultCount + "건, 전체 bids() stream/filter 수행");
        return elapsed;
    }

    private static long benchmarkSaBidLookup(int itemCount) {
        com.zippt.l3l4sa.server.service.DataStore store = new com.zippt.l3l4sa.server.service.DataStore();
        seedSaBids(store, itemCount);
        com.zippt.l3l4sa.server.control.AuctionQueryControl queryControl =
                new com.zippt.l3l4sa.server.control.AuctionQueryControl(store);

        long startedAt = System.nanoTime();
        int resultCount = queryControl.getBidsForAuction(TARGET_AUCTION_ID).size();
        long elapsed = System.nanoTime() - startedAt;
        System.out.println("  SA NFR 결과: " + resultCount + "건, findBidsByAuction(auctionId) 인덱스 조회 수행");
        return elapsed;
    }

    private static void seedL3L4Bids(com.zippt.l3l4.server.service.DataStore store, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            String auctionId = i == itemCount - 1 ? TARGET_AUCTION_ID : "auction-" + (i % 1_000);
            String agentId = i == itemCount - 1 ? TARGET_AGENT_ID : "agent-" + i;
            com.zippt.l3l4.server.domain.BidProposal proposal = new com.zippt.l3l4.server.domain.BidProposal(
                    "proposal-" + i,
                    "bid-" + i,
                    BigDecimal.ONE,
                    "benchmark marketing strategy",
                    30,
                    "benchmark terms"
            );
            store.saveBid(new com.zippt.l3l4.server.domain.Bid("bid-" + i, auctionId, agentId, proposal));
        }
    }

    private static void seedSaBids(com.zippt.l3l4sa.server.service.DataStore store, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            String auctionId = i == itemCount - 1 ? TARGET_AUCTION_ID : "auction-" + (i % 1_000);
            String agentId = i == itemCount - 1 ? TARGET_AGENT_ID : "agent-" + i;
            com.zippt.l3l4sa.server.domain.BidProposal proposal = new com.zippt.l3l4sa.server.domain.BidProposal(
                    "proposal-" + i,
                    "bid-" + i,
                    BigDecimal.ONE,
                    "benchmark marketing strategy",
                    30,
                    "benchmark terms"
            );
            store.saveBid(new com.zippt.l3l4sa.server.domain.Bid("bid-" + i, auctionId, agentId, proposal));
        }
    }

    private static void printComparison(String label, long l3Time, long saTime) {
        double l3Millis = l3Time / 1_000_000.0;
        double saMillis = saTime / 1_000_000.0;
        double ratio = saTime == 0 ? 0 : (double) l3Time / saTime;
        System.out.printf("  비교 요약 - %s: L3+L4 %.3f ms / SA %.3f ms / 약 %.1f배 차이%n",
                label, l3Millis, saMillis, ratio);
    }
}
