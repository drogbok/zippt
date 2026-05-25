# L3+L4 vs L3+L4+SA 비교 분석

## 0. 분석 기준

- L3+L4 코드: `src/main/java/com/zippt/l3l4`
- L3+L4+SA 코드: `src/main/java/com/zippt/l3l4sa`
- SA 문서: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`
- 추가 명세 유형: Data Dictionary + Business Rule + NFR
- 비교 목적: L3/L4 구조에 값 범위, 업무 규칙, 품질 기준을 추가했을 때 AI 생성 코드가 어떤 방향으로 달라지는지 관찰한다.

---

## 1. 요약 결론

L3+L4 단계는 Client/Server, Interface/Control/Entity/Business Logic 분리를 통해 책임 구조를 만들었다. 그러나 다음 정보는 L3/L4만으로는 충분히 고정되지 않는다.

- `bidDeadline`, `commissionRate`, `criteriaWeight`의 구체적인 허용 범위
- 중복 입찰, 마감 이후 입찰, 중복 낙찰 같은 도메인 행위 제약
- 검색/조회 성능, 입력 오류 복구, 처리 결과 추적 같은 품질 기준

SA 단계에서는 이를 10개 항목으로 보강했다.

| 유형 | 항목 수 | 핵심 효과 |
|---|---:|---|
| Data Dictionary | 3 | 값의 의미와 허용 범위를 구체화 |
| Business Rule | 3 | 도메인에서 허용/금지되는 행위를 구체화 |
| NFR | 4 | 같은 기능을 어떤 품질로 수행할지 구체화 |

핵심 메시지:

> L3/L4가 "누가 무엇을 책임지는가"를 정했다면, SA는 "그 책임을 어떤 값 범위, 업무 규칙, 품질 기준으로 수행해야 하는가"를 구체화했다.

---

## 2. SA 항목 10개와 코드 반영 위치

| ID | 유형 | 항목 | 주요 코드 위치 |
|---|---|---|---|
| DD-1 | Data Dictionary | 경매 마감기한 범위 | `AuctionCondition.validateDeadlineRange()` |
| DD-2 | Data Dictionary | 중개 수수료율 범위 | `BidProposal.validateCommissionRate()` |
| DD-3 | Data Dictionary | 낙찰 기준 가중치 | `WinnerSelectionCriteria.validateWeights()` |
| BR-1 | Business Rule | 동일 경매 중복 입찰 제한 | `SubmitBidManager.createOrUpdateBid()` |
| BR-2 | Business Rule | 마감 이후 입찰 불가 | `SubmitBidManager.validateAuctionOpen()` |
| BR-3 | Business Rule | 낙찰은 마감 이후 1회만 가능 | `SelectWinnerManager.validateAuctionClosed()`, `validateNoWinnerSelected()` |
| NFR-P1 | Performance | 매물 지역 검색 응답성 | `DataStore.propertiesByRegion`, `SearchPropertyManager.search()` |
| NFR-P2 | Performance | 경매/중개사별 입찰 조회 효율 | `DataStore.bidsByAuction`, `DataStore.bidsByAgent` |
| NFR-U1 | Usability | 콘솔 입력 오류 복구 | `ConsoleInputReader.readIntWithRetry()` |
| NFR-O1 | Observability | 처리 결과 로그 | `OperationLog`, `SearchPropertyManager.search()` |

---

## 3. Data Dictionary 변화

### 3.1 DD-1 경매 마감기한

L3+L4에서는 `AuctionCondition.bidDeadline` 속성은 존재하지만, 허용 범위가 구체적이지 않다.

SA 적용 후:

```java
DataDictionaryValidator.requireFutureWithin(
        bidDeadline,
        Duration.ofHours(1),
        Duration.ofDays(90),
        ValidationErrorCode.AUCTION_DEADLINE_INVALID);
```

의미:

- AI가 "미래 시각이면 된다" 정도로 추론하지 않고, 업무상 허용 범위를 코드로 반영한다.

### 3.2 DD-2 중개 수수료율

L3+L4에서는 `commissionRate`를 일반 비율처럼 0~100으로 해석할 수 있다.

SA 적용 후:

```java
DataDictionaryValidator.requireDecimalRange(
        commissionRate,
        BigDecimal.ZERO,
        BigDecimal.TEN,
        ValidationErrorCode.BID_COMMISSION_RATE_INVALID);
DataDictionaryValidator.requireScale(commissionRate, 2, ValidationErrorCode.DECIMAL_SCALE_INVALID);
```

의미:

- ZIP-PT 도메인에서 중개 수수료율은 0~10%, 소수점 2자리 이하라는 정책이 코드에 반영된다.

### 3.3 DD-3 낙찰 기준 가중치

SA 적용 후:

```text
commissionRateWeight + marketingStrategyWeight = 1.0
PRICE_FIRST이면 commissionRateWeight >= 0.6
SERVICE_FIRST이면 marketingStrategyWeight >= 0.6
BALANCED이면 각 가중치 0.4~0.6
```

의미:

- `WinnerSelectionCriteria`가 단순 데이터 객체가 아니라 유효한 낙찰 기준인지 스스로 검증한다.

---

## 4. Business Rule 변화

### 4.1 BR-1 동일 경매 중복 입찰 제한

SA 적용 후:

```java
Bid existing = store.bids().stream()
        .filter(bid -> bid.belongsToAuction(command.auctionId()))
        .filter(bid -> bid.submittedBy(command.agentId()))
        .findFirst()
        .orElse(null);
if (existing != null) {
    throw new IllegalStateException("Agent already submitted bid for this auction.");
}
```

의미:

- 같은 경매에 같은 중개사가 여러 활성 입찰을 만들 수 없도록 도메인 행위 규칙이 명확해진다.

### 4.2 BR-2 마감 이후 입찰 불가

SA 적용 후:

```java
if (auction == null || !auction.hasActiveStatus() || auction.isClosedForBidding(LocalDateTime.now())) {
    throw new IllegalStateException("Auction is not open for bidding.");
}
```

의미:

- 상태가 `OPEN`이어도 마감 시간이 지난 경우 입찰 저장 전에 거부한다.

### 4.3 BR-3 낙찰은 마감 이후 1회만 가능

SA 적용 후:

```java
validateAuctionClosed(command.auctionId());
validateNoWinnerSelected(auction);
```

의미:

- 마감 전 낙찰, 중복 낙찰, 해당 경매에 속하지 않은 bid 선택을 방지한다.

---

## 5. NFR 변화

### 5.1 NFR-P1 매물 지역 검색 응답성

L3+L4:

```java
store.properties().stream()
        .filter(property -> region.equals(property.getRegion()))
```

SA:

```java
propertiesByRegion.computeIfAbsent(property.getRegion(), ignored -> new ArrayList<>()).add(property);
store.findPropertyIdsByRegion(region);
```

### 5.2 NFR-P2 경매/중개사별 입찰 조회 효율

L3+L4:

```java
store.bids().stream()
        .filter(bid -> bid.belongsToAuction(auctionId))
```

SA:

```java
bidsByAuction.computeIfAbsent(bid.getAuctionId(), ignored -> new ArrayList<>()).add(bid);
bidsByAgent.computeIfAbsent(bid.getAgentId(), ignored -> new ArrayList<>()).add(bid);
store.findBidsByAuction(auctionId);
store.findBidsByAgent(agentId);
```

### 5.3 NFR-U1 콘솔 입력 오류 복구

SA:

```text
잘못된 숫자 입력 -> 오류 원인 안내 -> 같은 단계 재입력 -> 정상 값 수락
```

### 5.4 NFR-O1 처리 결과 로그

SA:

```java
new OperationLog("SEARCH_PROPERTY", buyerId, region, "SUCCESS(...)", elapsedNanos)
```

---

## 6. 테스트 후보

최종 발표 테스트는 별도로 선정한다. 현재 코드에서 바로 확인 가능한 후보는 다음과 같다.

| 후보 | 검증 가능 SA | 설명 |
|---|---|---|
| 경매 마감기한 검증 | DD-1 | 1시간 미만/90일 초과 deadline 거부 |
| 수수료율 범위 검증 | DD-2 | 10% 초과 또는 소수점 3자리 수수료율 거부 |
| 낙찰 기준 가중치 검증 | DD-3 | 합계 1.0이 아니거나 우선순위 규칙 위반 시 거부 |
| 중복 입찰 거부 | BR-1 | 동일 auctionId + agentId 두 번째 입찰 거부 |
| 마감 이후 입찰 거부 | BR-2 | deadline 이후 Submit Bid 거부 |
| 중복 낙찰 거부 | BR-3 | 이미 낙찰된 경매의 Select Winner 재시도 거부 |
| 매물 지역 검색 성능 비교 | NFR-P1 | 전체 순회 vs 지역 인덱스 조회 |
| 입찰 조회 성능 비교 | NFR-P2 | 전체 순회 vs auctionId/agentId 인덱스 조회 |
| 콘솔 입력 오류 복구 | NFR-U1 | `abc`, `0`, `3` 입력 복구 |
| OperationLog 생성 확인 | NFR-O1 | 작업명/행위자/대상/결과/소요시간 기록 확인 |

현재 `SaNfrBenchmark`는 NFR-P1, NFR-P2, NFR-U1, NFR-O1 중심으로 콘솔 비교를 제공한다. DD/BR 항목은 발표에서 선택할 테스트가 확정되면 별도 테스트로 추가하면 된다.

---

## 7. 결과 해석 주의점

- 성능 수치는 운영 성능 보장이 아니라 동일 toy dataset에서 명세 차이가 코드 구조와 실행 경로에 만든 차이를 보여주는 실험 결과이다.
- 같은 100만 건이어도 개선 배수는 같을 필요가 없다. 인덱스 조회 후 후처리 비용, 매칭 건수, 결과 변환 여부가 다르기 때문이다.
- Business Rule은 NFR이 아니다. 동일 경매 중복 입찰 제한, 마감 이후 입찰 불가, 중복 낙찰 금지는 도메인 행위 규칙이다.
- Data Dictionary는 기능 흐름보다 값의 의미와 범위를 고정하는 역할이다.

---

## 8. 발표자료용 핵심 문장

> SA를 Data Dictionary, Business Rule, NFR 세 유형으로 나누어 적용했다. Data Dictionary는 값의 범위를, Business Rule은 도메인 행위 제약을, NFR은 같은 기능을 수행하는 품질 기준을 보강한다.

> L3/L4만으로도 객체 구조는 분리되지만, 수수료율 범위나 마감기한, 중복 입찰 제한, 검색 인덱스 같은 세부 정책은 여전히 불명확할 수 있다.

> SA를 추가하자 AI 생성 코드는 단순 구조 분리를 넘어 validation, 도메인 행위 제한, 조회 인덱스, 입력 복구, 처리 로그를 포함하는 방향으로 구체화되었다.

