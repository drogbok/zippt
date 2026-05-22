# ZIP-PT L2 Code — UC Description 기반 (Submit Bid)

> ZIP-PT 의 역경매 핵심 UC 인 **Submit Bid** 의 Use Case Description 을 기반으로 정밀 구현.
> **project_proposal.md 4.1 의 동시성 제어 요구사항** 을 synchronized 로 반영.

## 패키지 구조

```
com.zippt.l2
├── enums/       AuctionStatus
├── model/       User, Auction, BidForm, Bid, BidReceipt
├── exception/   AuctionNotActive (A1), BidValidation (A2), BidCancelled (A3),
│                BidStorage (A4), ConcurrentBid (A5), Auth (P1/P3), Storage
├── port/        AuctionRepository, BidRepository, NotificationQueue  (interface)
└── usecase/     ValidateUser, BidValidator, SubmitBidUseCase
```

## 동시성 제어 (project_proposal 4.1 반영)

| 지점 | 구현 |
|---|---|
| `Auction.registerBid()` | `synchronized` 메서드 — 입찰 건수 증가 + OPEN→ACTIVE 전이를 atomic 보장 |
| `SubmitBidUseCase.execute()` | `synchronized (auction) { ... }` 블록 — 모니터 재진입 후 `acceptsBids()` 재검사로 A5 감지 |
| A5 `ConcurrentBidException` | 동시 낙찰 충돌 시 rollback + 경고 로그 |

## Description ↔ 코드 매핑

| Desc | 내용 | 코드 위치 |
|---|---|---|
| 1 | `<<include>>` Validate User | `validateUser.execute(agent)` |
| 2~3 | 활성 경매 조회 | `auctionRepo.findActive()` |
| 4 | 경매 선택 + 마감 확인 | `findById()` + `acceptsBids()` |
| 5 | 상세 정보 표시 | (프레젠테이션 책임, 주석만) |
| 6 | 입찰서 작성 | `form` 파라미터 |
| 7 | 유효성 검증 | `BidValidator.validate(form)` |
| 8 | 제출 확정 | 메서드 호출 자체 |
| 9 | 저장소 쓰기 | `bidRepo.save(bid)` (try/catch) |
| 10 | 경매 상태 갱신 (동시성) | `synchronized (auction) { auction.registerBid(bid) }` |
| 11 | 접수번호 반환 | `new BidReceipt(...)` |
| 12 | 알림 큐 등재 | `notificationQueue.enqueue(...)` |
| 13 | 정상 종료 | `return receipt` |

## 빌드

```bash
cd l2
find src/main/java -name '*.java' | xargs javac -d build
```

port 3종(AuctionRepository / BidRepository / NotificationQueue) 은 인터페이스만 선언되어 있으므로
실제 실행하려면 Mock 구현체를 주입해야 한다.
