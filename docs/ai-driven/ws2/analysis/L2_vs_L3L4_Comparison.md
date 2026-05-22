# L2 vs L3+L4 비교 분석

## 0. 분석 기준

- L2 baseline: `src/main/java/com/zippt/l2`
- L3+L4 코드: `src/main/java/com/zippt/l3l4`
- L3 명세: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_StaticModeling.md`
- L4 명세: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_ObjectStructuring.md`
- 비교 목적: 명세 수준이 높아졌을 때 AI 코드 생성 결과가 단순히 "코드가 길어진 것"이 아니라 어떤 구조적 문제를 개선하는지 관찰한다.

---

## 1. 요약 결론

L2 코드는 Use Case Description을 직접 코드로 옮긴 결과라서 `SubmitBidUseCase` 중심의 절차적 구조가 두드러진다. 인증, 검증, 저장, 상태 변경, 알림, 동시성 처리가 하나의 유스케이스 흐름 안에 모여 있다.

L3+L4 코드는 Static Modeling과 Object Structuring을 통해 도메인 개념과 객체 책임이 먼저 고정되었기 때문에, 다음 변화가 나타났다.

1. `WinnerSelectionCriteria`, `AuctionCondition`, `BidProposal`, `AgentCredential` 등 도메인 개념이 독립 Entity로 분리되었다.
2. `RegisterAuctionManager`, `SubmitBidManager`, `SelectWinnerManager`가 분리되어 핵심 UC별 Business Logic 책임이 명확해졌다.
3. `AuctionClientControl`, `AuctionSession`, `BidSession`, `WinnerSelectionSession`이 생겨 Client 처리 세션과 Server 처리가 분리되었다.
4. `auctionId` 단위 lock 안에서 검증과 변경을 함께 수행하도록 바뀌어 TOCTOU 위험을 줄이는 방향으로 구조가 개선되었다.
5. 상태 전이가 `AuctionLifecycleControl`, `AuctionClientState`, `AuctionStatus`, `BidStatus`, `ReservationStatus`로 분리되어 추적성이 높아졌다.

핵심 메시지:

> L3는 도메인 명사를 Entity로 고정해 AI의 임의 추론을 줄였고, L4는 객체 책임과 실행 위치를 나눠 UseCase 절차 중심 코드를 Client/Server, Interface/Control/Entity/Business Logic 구조로 바꿨다.

---

## 2. 정량 비교

### 2.1 Java 파일 수

| 구분 | Java 파일 수 | 관찰 |
|---|---:|---|
| L2 | 20 | Submit Bid 중심의 제한된 모델 |
| L3+L4 | 54 | L3 엔티티와 L4 객체 책임 분리로 클래스 수 증가 |

### 2.2 패키지 구조 비교

| 구분 | 패키지 구조 | 관찰 |
|---|---|---|
| L2 | `enums`, `exception`, `model`, `port`, `usecase` | Use Case 실행을 위한 기능 중심 구조 |
| L3+L4 | `client`, `common`, `server` | Client/Server와 공통 명세가 최상위에서 분리됨 |

### 2.3 패키지별 파일 수

#### L2

| 패키지 | 파일 수 | 의미 |
|---|---:|---|
| `l2` | 1 | 데모 Main |
| `enums` | 1 | 경매 상태 |
| `exception` | 7 | Alternative 흐름 예외 |
| `model` | 5 | 최소 도메인 객체 |
| `port` | 3 | 저장소/알림 포트 |
| `usecase` | 3 | Validate, SubmitBid, Validator |

#### L3+L4

| 패키지 | 파일 수 | 의미 |
|---|---:|---|
| `client/control` | 1 | 클라이언트 상태 흐름 제어 |
| `client/domain` | 6 | 처리 세션 중 임시 데이터 |
| `client/ui` | 3 | 외부 사용자별 Interface Object |
| `common/command` | 1 | Client -> Server 요청 구조 |
| `common/enums` | 12 | 상태/타입 enum |
| `common/result` | 1 | Server -> Client 응답 구조 |
| `server/control` | 2 | 조회 제어, 상태 전이 제어 |
| `server/domain` | 17 | L3 Entity Class 기반 도메인 객체 |
| `server/integration` | 1 | 외부 시스템 Interface Object |
| `server/service` | 9 | Coordinator와 Business Logic Object |

---

## 3. 과제 기준 비교표

| 비교 항목 | L2 | L3+L4 | 개선 판단 |
|---|---|---|---|
| 생성된 클래스 수 | 20개 Java 파일 | 54개 Java 파일 | 단순 증가가 아니라 Entity, Interface, Control, BL 분리로 증가 |
| 명세 기반 클래스 수 | Use Case Description에 직접 등장한 객체 중심 | L3 엔티티 + L4 객체 유형 대부분 반영 | 명세와 코드의 대응 관계 증가 |
| AI 임의 생성 클래스 수 | Repository, Queue, Exception 등 구현 편의 클래스가 섞임 | ObjectStructuring에서 패키지/역할을 지정한 클래스 중심 | 임의 구조 생성 위험 감소 |
| ECB 분리 | `model/usecase/port` 중심, Boundary/Control 구분 약함 | `client.ui`, `client.control`, `server.domain`, `server.service`로 분리 | 분리됨 |
| Architecture C/S 분리 | 없음. `com.zippt.l2` 단일 패키지 아래 기능별 분리 | `client`, `server`, `common` 최상위 분리 | 있음 |
| 상태 관리 명확성 | `AuctionStatus` 중심. 상태 전이 규칙은 UseCase/Entity에 흩어짐 | `AuctionClientState`, `AuctionLifecycleControl`, `AuctionStatus`, `BidStatus`, `ReservationStatus` | 명확함 |
| 동시성 처리 | `Auction#registerBid()` 및 `synchronized(auction)` 일부 적용 | `auctionId`, `propertyId` 단위 lock과 검증+변경 통합 | 개선됨 |

---

## 4. 특징적 개선 포인트

### 4.1 낙찰 기준의 추적성 개선

L2에서는 핵심 코드가 `SubmitBidUseCase` 중심이라 `Register Auction -> Submit Bid -> Select Winner`를 관통하는 낙찰 기준이 독립 구조로 강하게 드러나지 않는다.

L3+L4에서는 `WinnerSelectionCriteria`가 독립 Entity로 생성되었다.

관련 코드:

- `src/main/java/com/zippt/l3l4/server/domain/WinnerSelectionCriteria.java`
- `src/main/java/com/zippt/l3l4/server/domain/Auction.java`
- `src/main/java/com/zippt/l3l4/server/service/RegisterAuctionManager.java`
- `src/main/java/com/zippt/l3l4/server/service/SelectWinnerManager.java`

흐름:

1. `AuctionSession`이 `CriteriaInput`을 누적한다.
2. `RegisterAuctionManager`가 `WinnerSelectionCriteria`를 생성한다.
3. `Auction`이 `selectionCriteria`를 보유한다.
4. `SelectWinnerManager`가 `auction.getSelectionCriteria().calculateBidScore(...)`로 입찰서를 정렬한다.

개선 의미:

> L3 Static Modeling에서 "낙찰 우선 기준"을 Entity로 고정했기 때문에, AI가 이를 일회성 파라미터나 주석으로 흘려보내지 않고 핵심 거래 흐름 전체에서 재사용하는 객체로 생성했다.

### 4.2 UseCase 단일 책임 과다 완화

L2의 중심 클래스:

- `src/main/java/com/zippt/l2/usecase/SubmitBidUseCase.java`

이 클래스는 다음 책임을 함께 가진다.

- 인증 검증
- 활성 경매 조회
- 공고 선택
- 입찰 유효성 검증
- 입찰 저장
- 경매 상태 갱신
- 알림 큐 등재
- 동시성 일부 처리

L3+L4에서는 책임이 다음처럼 분리된다.

| 책임 | L3+L4 클래스 |
|---|---|
| 클라이언트 흐름 상태 | `AuctionClientControl` |
| 입찰 세션 데이터 누적 | `BidSession` |
| 서버 요청 진입점 | `ZIPPTTransactionServer` |
| 인증 | `AuthenticationManager` |
| 입찰 비즈니스 규칙 | `SubmitBidManager` |
| 경매 상태 전이 | `AuctionLifecycleControl` |
| 입찰 데이터 | `Bid`, `BidProposal` |
| 알림 데이터 | `Notification` |

개선 의미:

> L4 Object Structuring이 "누가 소통하고, 무엇을 저장하고, 누가 제어하고, 어떤 BL을 처리하는가"를 미리 정했기 때문에, AI 코드가 하나의 UseCase 클래스에 절차를 몰아넣는 경향이 줄었다.

### 4.3 동시성 경계 개선

L2에도 동시성 처리는 있다.

- `Auction#registerBid(Bid)`가 `synchronized` 메서드이다.
- `SubmitBidUseCase`에서 `synchronized (auction)` 블록을 사용한다.

하지만 L2 흐름은 저장과 일부 검증이 lock 밖에 존재한다.

L2 구조 요약:

```text
Precondition 검증
BidValidator.validate(form)
bidRepo.save(bid)
synchronized (auction) {
    auction.acceptsBids() 재확인
    auction.registerBid(bid)
}
notificationQueue.enqueue(...)
```

L3+L4 구조:

```text
synchronized (store.lockFor("auction:" + auctionId)) {
    validateAuctionOpen(auctionId)
    validateBidProposal(proposal)
    createOrUpdateBid(...)
    auction.incrementBidCount()
    notifySellerOfNewBid(...)
}
```

개선 의미:

> L4에서 "검증과 변경을 같은 임계 영역 안에서 수행"하도록 명세했기 때문에, L3+L4 코드는 `auctionId` 단위 lock 안에서 마감 검증, 중복 입찰 검증, 입찰 저장, 입찰 수 증가를 함께 처리한다. 이는 마감 검증 후 저장 전에 상태가 바뀌는 TOCTOU 위험을 줄인다.

주의:

- 현재 코드는 실 DB 트랜잭션이 아니라 in-memory lock 수준이다.
- 따라서 "완전한 운영 동시성 보장"이 아니라 "동시성 경계가 명세 기반으로 더 명확해졌다"라고 표현하는 것이 적절하다.

### 4.4 상태 전이 추적성 개선

L2:

- `AuctionStatus`는 존재한다.
- 상태 전이 규칙은 `Auction`과 `SubmitBidUseCase`에 일부 섞여 있다.
- Client 처리 상태는 별도로 드러나지 않는다.

L3+L4:

- `AuctionClientState`: 클라이언트 처리 흐름 상태
- `AuctionStatus`: 서버 경매 상태
- `BidStatus`: 입찰서 상태
- `ReservationStatus`: 예약 상태
- `AuctionLifecycleControl`: 경매 상태 전이 규칙

개선 의미:

> L3에서 상태 enum을 정적 모델로 고정하고 L4에서 상태 전이 책임을 Control Object로 분리하자, 상태값 자체와 상태 변경 규칙이 추적 가능한 코드 구조가 되었다.

### 4.5 식별자 기반 저장 구조

L2의 데모 저장소는 일부 `List` 기반이다.

- `InMemoryBidRepository`는 `List<Bid>`를 사용한다.
- `hasActiveBid()`는 전체 Bid 목록을 순회한다.

L3+L4의 `DataStore`는 주요 서버 Entity를 ID 기반 `Map`으로 보관한다.

- `Map<String, User> users`
- `Map<String, Property> properties`
- `Map<String, Auction> auctions`
- `Map<String, Bid> bids`
- `Map<String, Object> locks`

개선 의미:

> L3에서 식별자 속성을 명확히 하고 L4에서 서버 Persistent Entity로 배치하면서, 단건 조회와 lock 대상이 ID 기반으로 구조화되었다.

주의:

- 현재 코드에도 전체 순회가 필요한 조회는 남아 있다.
- 따라서 성능 개선을 측정했다고 말하기보다는, "확장 가능한 key-value 기반 저장 구조로 이동했다"고 표현하는 것이 적절하다.

---

## 5. L2에서 남아 있던 문제와 L3+L4의 개선 방향

| L2 문제 | 원인 | L3+L4 개선 |
|---|---|---|
| `SubmitBidUseCase`에 책임 집중 | Use Case Description을 절차로 직접 변환 | Transaction Manager, Entity, Control로 분리 |
| 낙찰 기준이 구조적으로 약함 | L2는 Submit Bid 중심 기준선 | `WinnerSelectionCriteria` Entity로 고정 |
| Client/Server 경계 불명확 | L2에는 실행 위치 명세 없음 | `client`, `server`, `common` 패키지 분리 |
| 상태 전이 규칙 추적 어려움 | enum은 있으나 전이 제어 객체 없음 | `AuctionLifecycleControl` 도입 |
| 동시성 처리 범위가 부분적 | lock 대상과 임계 영역이 명세되지 않음 | `auctionId` lock 안에서 검증+변경 통합 |
| 저장 구조가 기능별 임시 구현에 가까움 | L2는 유스케이스 실행을 위한 최소 포트 | `DataStore`에서 주요 Entity를 ID 기반 Map으로 관리 |

---

## 6. 발표자료용 핵심 문장

> L2는 Use Case Description의 단계가 코드 순서로 잘 반영되었지만, 객체 책임이 UseCase 클래스에 집중되었다.

> L3 Static Modeling을 추가하자 `WinnerSelectionCriteria`, `AuctionCondition`, `BidProposal`처럼 UC 단계에 흩어진 도메인 개념이 독립 Entity로 고정되었다.

> L4 Object Structuring을 추가하자 Client/Server, Interface/Control/Entity/Business Logic이 분리되어 코드 구조가 과제 명세와 직접 대응하기 시작했다.

> 특히 L4에서 동시성 경계를 명세한 결과, L3+L4 코드에서는 `auctionId` 단위 lock 안에서 검증과 변경을 함께 수행하도록 바뀌어 TOCTOU 위험을 줄이는 방향으로 개선되었다.

> 따라서 L3+L4의 효과는 클래스 수 증가가 아니라, 도메인 추적성, 책임 분리, 상태 전이 명확성, 동시성 경계 명시라는 구조적 품질 개선으로 볼 수 있다.

---

## 7. 잔여 한계

- L3+L4 코드는 구조 비교를 위한 생성 코드이며, 운영 수준의 영속 저장소나 DB 트랜잭션은 포함하지 않는다.
- L3+L4의 `DataStore`는 in-memory 구조이므로 실제 성능 개선을 수치로 주장하기는 어렵다.
- 일부 조회는 여전히 전체 순회를 사용한다. 다만 단건 조회와 lock 대상은 ID 기반으로 구조화되었다.
- `ReservationManager`, `ReviewManager`, `ExternalDataAPIInterface`는 L3/L4 구조 반영 수준이며, 상세 비즈니스 구현은 SA 또는 후속 단계에서 확장 대상이다.

