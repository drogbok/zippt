# WS2_ZIP-PT_집피티_SA

## 0. Specification Augmentation 개요

- 대상 시스템: 집피티(ZIP-PT)
- 단계: Part C Specification Augmentation
- 선택한 추가 명세 유형: Data Dictionary + Business Rule + NFR
- 연결 대상:
  - L3 Static Modeling의 `Auction`, `Bid`, `BidProposal`, `AuctionCondition`, `WinnerSelectionCriteria`, `Property`, `Agent` Entity
  - L4 Object Structuring의 `SubmitBidManager`, `SelectWinnerManager`, `SearchPropertyManager`, `AuctionQueryControl`, `DataStore`

### 0.1 세 가지 유형으로 나눈 이유

SA는 L3/L4 구조를 새로 뒤집는 단계가 아니라, L3/L4만으로는 부족했던 세부 명세를 보강하는 단계로 해석한다. 이번 SA는 다음 세 유형으로 구성한다.

| 유형 | 역할 | 예 |
|---|---|---|
| Data Dictionary | 값의 의미와 허용 범위를 명확히 한다. | 경매 마감기한, 수수료율, 낙찰 기준 가중치 |
| Business Rule | 도메인에서 허용/금지되는 행위를 명확히 한다. | 중복 입찰 금지, 마감 이후 입찰 불가, 중복 낙찰 금지 |
| NFR | 같은 기능을 어떤 품질로 수행할지 명확히 한다. | 인덱스 조회, 동시 요청 일관성 |

> 핵심 관찰 질문: SA를 추가하면 AI가 값 범위, 업무 규칙, 성능/동시성 품질 기준을 더 명확한 코드 구조와 검증 로직으로 생성하는가?

### 0.2 L2 동시성 맥락과 연결

L2 시점의 요구사항에는 이미 운영 서비스에서 발생할 수 있는 동시성 시나리오가 존재했다.

```text
여러 중개사가 동시에 입찰하거나, 여러 요청이 동시에 낙찰 처리를 시도할 수 있다.
시스템은 중복 입찰/중복 낙찰로 인한 상태 불일치를 방지해야 한다.
```

따라서 SA의 동시성 항목은 새로운 기능을 추가하는 것이 아니라, L2부터 존재했던 시나리오를 L3/L4의 `Auction`, `Bid`, `SubmitBidManager`, `SelectWinnerManager` 구조 안에서 검증 가능한 품질 요구로 구체화한 것이다.

---

## 1. 최종 SA 항목 10개

| ID | 유형 | 항목 | L3/L4 연결 지점 | 코드 반영 기대 |
|---|---|---|---|---|
| DD-1 | Data Dictionary | 경매 마감기한 범위 | `AuctionCondition.bidDeadline`, `Auction.bidDeadline` | 현재 시각 + 1시간 이후, 최대 90일 이내 |
| DD-2 | Data Dictionary | 중개 수수료율 범위 | `BidProposal.commissionRate` | 0~10%, 소수점 2자리 이하 |
| DD-3 | Data Dictionary | 낙찰 기준 가중치 | `WinnerSelectionCriteria` | 두 가중치 합계 1.0, 우선순위별 최소 가중치 |
| BR-1 | Business Rule | 동일 경매 중복 입찰 제한 | `SubmitBidManager`, `Bid.auctionId`, `Bid.agentId` | 동일 `auctionId + agentId` 활성 입찰 1건만 허용 |
| BR-2 | Business Rule | 마감 이후 입찰 불가 | `SubmitBidManager`, `Auction` | `OPEN` 상태라도 `bidDeadline` 이후 입찰 거부 |
| BR-3 | Business Rule | 낙찰은 마감 이후 1회만 가능 | `SelectWinnerManager`, `AuctionLifecycleControl` | 마감 전 낙찰 거부, `WINNER_SELECTED` 이후 중복 낙찰 거부 |
| NFR-P1 | Performance | 매물 지역 검색 응답성 | `SearchPropertyManager`, `DataStore`, `Property.region` | 전체 순회 대신 `propertiesByRegion` 인덱스 조회 |
| NFR-P2 | Performance | 경매/중개사별 입찰 조회 효율 | `AuctionQueryControl`, `DataStore`, `Bid.auctionId`, `Bid.agentId` | `bidsByAuction`, `bidsByAgent` 인덱스 조회 |
| NFR-C1 | Concurrency | 동시 입찰 요청 원자성 | `SubmitBidManager`, `DataStore.lockFor()` | 동일 `auctionId + agentId` 동시 요청 중 활성 입찰 1건만 성공 |
| NFR-C2 | Concurrency | 동시 낙찰 선택 일관성 | `SelectWinnerManager`, `AuctionLifecycleControl`, `DataStore.lockFor()` | 같은 경매에 대한 동시 낙찰 요청 중 최종 낙찰 1건만 성공 |

---

## 2. Data Dictionary 상세

### DD-1 경매 마감기한 범위

| 속성 | 타입 | 의미 | 제약 |
|---|---|---|---|
| `AuctionCondition.bidDeadline` | datetime | 입찰을 받을 수 있는 마지막 시각 | 현재 시각보다 최소 1시간 이후, 최대 90일 이내 |

코드 반영 기대:

```text
DataDictionaryValidator.requireFutureWithin(
    bidDeadline,
    Duration.ofHours(1),
    Duration.ofDays(90),
    AUCTION_DEADLINE_INVALID
)
```

### DD-2 중개 수수료율 범위

| 속성 | 타입 | 의미 | 제약 |
|---|---|---|---|
| `BidProposal.commissionRate` | Decimal | 중개사가 제안하는 서비스 수수료율 | 0 이상 10 이하, 소수점 2자리 이하 |

코드 반영 기대:

```text
commissionRate >= 0
commissionRate <= 10
scale <= 2
```

### DD-3 낙찰 기준 가중치

| 속성 | 타입 | 의미 | 제약 |
|---|---|---|---|
| `commissionRateWeight` | Decimal | 수수료율 점수 가중치 | 0~1 |
| `marketingStrategyWeight` | Decimal | 마케팅 전략 점수 가중치 | 0~1 |
| `priorityType` | Enum | 낙찰 우선 기준 | PRICE_FIRST, SERVICE_FIRST, BALANCED |

추가 불변식:

```text
commissionRateWeight + marketingStrategyWeight = 1.0
PRICE_FIRST이면 commissionRateWeight >= 0.6
SERVICE_FIRST이면 marketingStrategyWeight >= 0.6
BALANCED이면 각 가중치 0.4~0.6
```

---

## 3. Business Rule 상세

### BR-1 동일 경매 중복 입찰 제한

```text
동일 auctionId + agentId 조합에 대해 활성 입찰은 최대 1건만 허용한다.
재제출 가능 상태가 아닌 기존 입찰이 있으면 새 입찰을 거부한다.
```

코드 반영 기대:

- `SubmitBidManager.createOrUpdateBid()`에서 기존 입찰 조회
- 기존 입찰이 있고 재제출 허용 상태가 아니면 `IllegalStateException`

### BR-2 마감 이후 입찰 불가

```text
경매 상태가 OPEN이어도 현재 시각이 bidDeadline 이후이면 입찰을 저장하지 않는다.
```

코드 반영 기대:

- `SubmitBidManager.validateAuctionOpen()`에서 `Auction.isClosedForBidding(now)` 확인
- 마감된 경매는 입찰서 생성/저장/알림 생성 이전에 거부

### BR-3 낙찰은 마감 이후 1회만 가능

```text
낙찰자 선정은 입찰 마감 이후에만 가능하다.
이미 WINNER_SELECTED 상태인 경매는 다시 낙찰자를 선정할 수 없다.
선택된 bidId는 해당 auctionId에 속한 입찰이어야 한다.
```

코드 반영 기대:

- `SelectWinnerManager.validateAuctionClosed()`
- `SelectWinnerManager.validateNoWinnerSelected()`
- `SelectWinnerManager.markWinner()`

---

## 4. NFR 상세

### NFR-P1 매물 지역 검색 응답성

L3+L4 baseline:

```text
store.properties().stream()
    .filter(property -> region.equals(property.getRegion()))
```

SA 적용 후:

```text
propertiesByRegion.computeIfAbsent(region, ...).add(property)
findPropertyIdsByRegion(region)
```

### NFR-P2 경매/중개사별 입찰 조회 효율

L3+L4 baseline:

```text
store.bids().stream()
    .filter(bid -> bid.belongsToAuction(auctionId))
```

SA 적용 후:

```text
bidsByAuction.computeIfAbsent(auctionId, ...).add(bid)
bidsByAgent.computeIfAbsent(agentId, ...).add(bid)
findBidsByAuction(auctionId)
findBidsByAgent(agentId)
```

### NFR-C1 동시 입찰 요청 원자성

```text
동일 auctionId + agentId 조합으로 여러 Submit Bid 요청이 동시에 도착해도,
검증과 저장은 auctionId 단위 임계 영역 안에서 처리되어 활성 입찰은 1건만 남아야 한다.
```

코드 반영 기대:

- `SubmitBidManager.submitBid()`에서 `store.lockFor("auction:" + auctionId)` 사용
- 중복 입찰 조회, 신규 입찰 저장, `bidCount` 증가를 같은 임계 영역에서 처리
- 동시 요청 테스트에서 성공 1건, 거부 N-1건 확인

### NFR-C2 동시 낙찰 선택 일관성

```text
같은 auctionId에 대해 여러 Select Winner 요청이 동시에 도착해도,
낙찰 가능 여부 확인과 상태 변경은 하나의 임계 영역에서 처리되어 최종 낙찰은 1건만 성공해야 한다.
```

코드 반영 기대:

- `SelectWinnerManager.selectWinner()`에서 `store.lockFor("auction:" + auctionId)` 사용
- `validateNoWinnerSelected()`와 `markWinner()`를 같은 임계 영역에서 처리
- 동시 요청 테스트에서 성공 1건, 거부 N-1건 확인

---

## 5. 테스트 후보

최종 발표 테스트는 별도로 선정한다. 현재 코드에서 바로 확인 가능한 후보는 다음과 같다.

| 후보 | 검증 가능 SA |
|---|---|
| 경매 마감기한 검증 | DD-1 |
| 수수료율 범위 검증 | DD-2 |
| 낙찰 기준 가중치 검증 | DD-3 |
| 중복 입찰 거부 | BR-1 |
| 마감 이후 입찰 거부 | BR-2 |
| 중복 낙찰 거부 | BR-3 |
| 매물 지역 검색 성능 비교 | NFR-P1 |
| 경매/중개사별 입찰 조회 성능 비교 | NFR-P2 |
| 동시 입찰 원자성 | NFR-C1 |
| 동시 낙찰 선택 일관성 | NFR-C2 |

벤치마크 실행 클래스:

```text
src/main/java/com/zippt/benchmark/SaNfrBenchmark.java
src/main/java/com/zippt/benchmark/ConcurrencyBenchmark.java
```

현재 벤치마크는 NFR-P1, NFR-P2, NFR-C1, NFR-C2 중심으로 콘솔 비교를 제공한다. DD/BR 항목은 발표 방향이 확정되면 별도 테스트 후보로 추가할 수 있다.

`ConcurrencyBenchmark`는 L2 baseline, L3+L4, L3+L4+SA를 같은 동시 입찰 시나리오로 실행하여 다음 차이를 보여준다.

| 단계 | 관찰 포인트 |
|---|---|
| L2 baseline | 중복 검증과 저장이 같은 임계 영역에 묶이지 않아 동시 요청에서 중복 입찰이 남을 수 있음 |
| L3+L4 | `SubmitBidManager`가 `auctionId` 단위 처리 지점을 구조화 |
| L3+L4+SA | 동시 요청 원자성을 품질 기준으로 명시하고 성공 1건/거부 N-1건을 검증 |

---

## 6. 분석 관점

| 관찰 항목 | 해석 |
|---|---|
| Data Dictionary 반영 | AI가 일반적인 값 추론 대신 명시된 범위/정밀도를 코드에 반영하는지 확인 |
| Business Rule 반영 | AI가 도메인 행위의 허용/금지 조건을 Manager/Entity 흐름에 반영하는지 확인 |
| NFR 반영 | 기능 결과는 유지하면서 조회 방식과 동시 요청 처리 방식이 달라지는지 확인 |
| L3/L4 구조 유지 | SA가 새 아키텍처를 만들지 않고 기존 객체 책임 안에 보강되는지 확인 |
