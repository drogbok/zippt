# ZIP-PT 발표용 코드 근거 발췌본

이 문서는 NotebookLM에 `.java` 파일을 직접 넣기 어려운 경우를 위해, 발표에 필요한 코드 근거를 Markdown으로 요약한 것이다.

## 1. L2 baseline: check-then-save 경쟁 조건

원본 파일:

```text
src/main/java/com/zippt/l2/usecase/SubmitBidUseCase.java
```

핵심 흐름:

```java
if (bidRepo.hasActiveBid(agent, auctionId)
        && !bidRepo.isResubmitAllowed(auctionId)) {
    throw new BidValidationException("resubmit not allowed");
}

Auction auction = auctionRepo.findById(auctionId);
if (auction == null || !auction.acceptsBids()) {
    throw new AuctionNotActiveException();
}

Bid bid = new Bid(agent, auction, form, LocalDateTime.now());
bidRepo.save(bid);

synchronized (auction) {
    if (!auction.acceptsBids()) {
        bidRepo.rollback();
        throw new ConcurrentBidException();
    }
    auction.registerBid(bid);
}
```

발표 해석:

```text
L2에는 synchronized가 일부 존재하지만, 중복 입찰 검증(hasActiveBid)과 저장(save)이 같은 임계 영역에 묶여 있지 않다.
따라서 여러 스레드가 동시에 hasActiveBid=false를 관찰하면 같은 agentId/auctionId 조합의 입찰이 여러 건 저장될 수 있다.
```

## 2. L2 Auction: 부분적인 synchronized

원본 파일:

```text
src/main/java/com/zippt/l2/model/Auction.java
```

핵심 흐름:

```java
public synchronized void registerBid(Bid bid) {
    this.bidCount++;
    if (this.status == AuctionStatus.OPEN) {
        this.status = AuctionStatus.ACTIVE;
    }
}
```

발표 해석:

```text
L2의 registerBid는 bidCount 증가와 상태 전이를 synchronized로 보호한다.
하지만 중복 입찰 여부 확인과 입찰 저장은 registerBid 이전에 수행되므로, 핵심 경쟁 조건 전체를 보호하지는 못한다.
```

## 3. L3+L4: SubmitBidManager의 책임 구조화

원본 파일:

```text
src/main/java/com/zippt/l3l4/server/service/SubmitBidManager.java
```

핵심 흐름:

```java
public BidSubmissionResult submitBid(SubmitBidCommand command) {
    Agent agent = authenticationManager.validateAgent(command.agentId());
    validateAgentCredential(agent.getUserId());

    synchronized (store.lockFor("auction:" + command.auctionId())) {
        Auction auction = validateAuctionOpen(command.auctionId());
        BidProposal proposal = new BidProposal(...);

        if (!validateBidProposal(proposal)) {
            throw new IllegalArgumentException("Invalid bid proposal.");
        }

        Bid bid = createOrUpdateBid(command, proposal);
        if (!bid.isResubmitted()) {
            auction.incrementBidCount();
        }
        notifySellerOfNewBid(auction, bid);
        return new BidSubmissionResult(...);
    }
}
```

발표 해석:

```text
L3+L4에서는 입찰 제출 책임이 SubmitBidManager로 이동하고, auctionId 단위 lock 안에서 검증, 생성/수정, bidCount 증가가 함께 처리된다.
즉 L2의 절차적 흐름이 책임 중심 구조로 재배치된다.
```

## 4. L3+L4+SA: 동시성 품질 기준의 명시

원본 파일:

```text
src/main/java/com/zippt/l3l4sa/server/service/SubmitBidManager.java
src/main/java/com/zippt/l3l4sa/server/service/SelectWinnerManager.java
```

핵심 흐름:

```java
synchronized (store.lockFor("auction:" + command.auctionId())) {
    Auction auction = validateAuctionOpen(command.auctionId());
    Bid bid = createOrUpdateBid(command, proposal);
    auction.incrementBidCount();
    notifySellerOfNewBid(auction, bid);
}
```

```java
synchronized (store.lockFor("auction:" + command.auctionId())) {
    Auction auction = validateAuctionClosed(command.auctionId());
    validateNoWinnerSelected(auction);
    markWinner(auction, command.selectedBidId());
    notifyBidResults(auction);
}
```

발표 해석:

```text
SA는 단순히 lock을 추가했다는 의미가 아니다.
동시 입찰 원자성(NFR-C1)과 동시 낙찰 선택 일관성(NFR-C2)을 명세로 고정하고,
이를 멀티스레드 테스트에서 성공 1건/거부 N-1건으로 검증 가능하게 만들었다.
```

## 5. DataStore lockFor

원본 파일:

```text
src/main/java/com/zippt/l3l4sa/server/service/DataStore.java
```

핵심 흐름:

```java
private final Map<String, Object> locks = new ConcurrentHashMap<>();

public Object lockFor(String key) {
    return locks.computeIfAbsent(key, ignored -> new Object());
}
```

발표 해석:

```text
동일 auctionId에 대해서는 같은 lock 객체를 사용한다.
따라서 같은 경매에 대한 입찰/낙찰 요청은 순차 처리되고, 서로 다른 경매는 별도 lock을 사용할 수 있다.
```

## 6. 성능 NFR: 전체 순회에서 인덱스 조회로 변경

원본 파일:

```text
src/main/java/com/zippt/l3l4sa/server/service/DataStore.java
```

핵심 흐름:

```java
private final Map<String, List<Property>> propertiesByRegion = new LinkedHashMap<>();
private final Map<String, List<Bid>> bidsByAuction = new LinkedHashMap<>();
private final Map<String, List<Bid>> bidsByAgent = new LinkedHashMap<>();

public void saveProperty(Property property) {
    properties.put(property.getPropertyId(), property);
    propertiesByRegion.computeIfAbsent(property.getRegion(), ignored -> new ArrayList<>()).add(property);
}

public void saveBid(Bid bid) {
    bids.put(bid.getBidId(), bid);
    bidsByAuction.computeIfAbsent(bid.getAuctionId(), ignored -> new ArrayList<>()).add(bid);
    bidsByAgent.computeIfAbsent(bid.getAgentId(), ignored -> new ArrayList<>()).add(bid);
}
```

발표 해석:

```text
SA의 성능 NFR은 기능 결과를 바꾸지 않고 실행 경로를 바꾼다.
L3+L4의 전체 stream/filter 조회가 SA에서는 region, auctionId, agentId 인덱스 조회로 바뀐다.
```

## 7. ConcurrencyBenchmark

원본 파일:

```text
src/main/java/com/zippt/benchmark/ConcurrencyBenchmark.java
```

핵심 시나리오:

```text
동일 agentId가 동일 auctionId에 동시에 100개의 입찰 요청을 보낸다.
목표 상태는 활성 입찰 1건이다.
```

최근 실행 결과:

```text
L2 baseline: 성공 100건, 거부 0건, 저장 100건
L3+L4: 성공 1건, 거부 99건, 저장 1건
L3+L4+SA: 성공 1건, 거부 99건, 저장 1건
```

발표 해석:

```text
L2는 기능 흐름은 구현되어 있지만 동시 요청에서 중복 입찰이 저장된다.
L3+L4는 책임 경계를 분리하여 auctionId 단위 처리 지점을 만든다.
SA는 이 동시성 품질 기준을 명세로 고정하고 테스트로 검증한다.
```

## 8. 발표에서 사용할 최종 표현

```text
핵심 경쟁 조건인 동시 입찰과 동시 낙찰 경로는 auctionId 단위 임계 영역으로 보호되며,
단일 JVM 기준 멀티스레드 테스트에서 thread-safe하게 동작함을 확인했다.
```
