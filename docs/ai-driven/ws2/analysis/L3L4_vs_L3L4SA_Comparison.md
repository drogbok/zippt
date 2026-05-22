# L3+L4 vs L3+L4+SA 비교 분석

## 0. 분석 기준

- L3+L4 코드: `src/main/java/com/zippt/l3l4`
- L3+L4+SA 코드: `src/main/java/com/zippt/l3l4sa`
- SA 문서: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`
- 추가 명세 유형: Data Dictionary
- 분석 목적: L3/L4로 만든 구조에 Data Dictionary가 추가되었을 때 코드가 어떤 방향으로 달라지는지 관찰한다.

---

## 1. 요약 결론

L3+L4 단계는 Entity, Control, Business Logic, Client/Server 경계를 명확히 만드는 데 효과가 있었다. 반면 L3+L4+SA 단계는 구조를 크게 다시 만들기보다, 이미 분리된 Entity와 Manager 안에 구체적인 데이터 제약 조건을 주입하는 방향으로 변화했다.

가장 두드러지는 차이는 다음 세 가지다.

1. 단순 null/blank 검증이 범위, 길이, 형식, 상태 전이 검증으로 바뀌었다.
2. 검증 실패가 `IllegalArgumentException` 수준에서 끝나지 않고 `ValidationErrorCode`와 `DomainValidationException`으로 분류되기 시작했다.
3. 예약, 후기, 낙찰 기준처럼 Use Case 사이에 걸친 규칙이 실제 저장 구조와 연계되어 검증된다.

따라서 SA의 효과는 "클래스 수 증가"보다 "도메인 규칙의 구체화"와 "오류 원인의 명확화"로 보는 것이 적절하다.

---

## 2. 정량 비교

### 2.1 Java 파일 수

| 구분 | Java 파일 수 | 관찰 |
|---|---:|---|
| L3+L4 | 54 | Static Modeling과 Object Structuring에 따른 기본 구조 |
| L3+L4+SA | 57 | 검증 공통 모듈 3개가 추가됨 |

추가된 파일:

| 파일 | 역할 |
|---|---|
| `common/validation/ValidationErrorCode.java` | 검증 실패 사유를 코드로 분류 |
| `common/validation/DomainValidationException.java` | 도메인 검증 실패 예외 |
| `common/validation/DataDictionaryValidator.java` | ID, 문자열 길이, 숫자 범위, 날짜 범위, 등록번호 형식 등 공통 검증 |

### 2.2 구조 변화

| 항목 | L3+L4 | L3+L4+SA |
|---|---|---|
| 최상위 구조 | `client`, `common`, `server` | 동일 |
| Entity 구성 | L3 Entity 중심 | 동일 Entity에 Data Dictionary 제약 추가 |
| BL 구성 | L4 Manager 중심 | 동일 Manager에서 검증 호출 강화 |
| 공통 검증 | 개별 클래스의 boolean 반환 중심 | 공통 Validator + 오류 코드 기반 예외 |
| 주요 변화 위치 | 구조와 책임 분리 | Entity/Manager 내부 validation |

SA는 아키텍처를 다시 만드는 단계가 아니라, L3/L4에서 만들어진 구조에 더 구체적인 데이터 의미를 넣는 단계로 나타났다.

---

## 3. 핵심 변화 포인트

### 3.1 수수료율 검증 강화

L3+L4의 `BidProposal.validateCommissionRate()`는 수수료율을 0 이상 100 이하로 검사한다. 하지만 ZIP-PT 도메인에서 입찰 제안 수수료율은 중개 서비스 수수료율이므로, SA Data Dictionary에서는 0 이상 10 이하와 소수점 2자리 이하로 구체화했다.

| 구분 | 검증 방식 |
|---|---|
| L3+L4 | `0 <= commissionRate <= 100` |
| L3+L4+SA | `0 <= commissionRate <= 10`, scale 2 이하 |

의미:

> SA가 없으면 AI는 "비율"이라는 일반 개념을 0~100으로 해석할 가능성이 높다. Data Dictionary를 추가하면 해당 시스템에서 허용되는 업무상 범위가 코드에 반영된다.

관련 코드:

- `src/main/java/com/zippt/l3l4/server/domain/BidProposal.java`
- `src/main/java/com/zippt/l3l4sa/server/domain/BidProposal.java`

### 3.2 낙찰 기준의 일관성 강화

L3+L4에서도 `WinnerSelectionCriteria`가 Entity로 분리되어 낙찰 기준을 구조화했다. 그러나 검증은 가중치가 음수가 아니고 합계가 양수인지 확인하는 수준이다.

SA 적용 후에는 다음 규칙이 추가되었다.

- `commissionRateWeight`는 0 이상 1 이하
- `marketingStrategyWeight`는 0 이상 1 이하
- 두 가중치의 합계는 정확히 1.0
- `PRICE_FIRST`이면 수수료율 가중치가 0.6 이상
- `SERVICE_FIRST`이면 마케팅 전략 가중치가 0.6 이상
- `BALANCED`이면 각 가중치가 0.4~0.6

의미:

> L3는 `WinnerSelectionCriteria`라는 개념을 만들어주고, L4는 그것을 어디서 생성하고 사용하는지 배치했다. SA는 그 기준이 "유효한 기준"인지 판단하는 수학적, 업무적 조건을 추가한다.

이 변화는 발표에서 설명하기 좋은 포인트다. L3+L4만으로도 낙찰 기준 객체는 생기지만, 잘못된 가중치 조합을 막는 것은 Data Dictionary가 들어온 뒤에야 명확해진다.

관련 코드:

- `src/main/java/com/zippt/l3l4/server/domain/WinnerSelectionCriteria.java`
- `src/main/java/com/zippt/l3l4sa/server/domain/WinnerSelectionCriteria.java`

### 3.3 경매 조건 입력 검증 구체화

L3+L4의 `AuctionCondition`은 `serviceCondition`, `minQualification`, `bidDeadline`을 갖고 있지만, 문자열은 null/blank 중심으로만 검증된다.

SA 적용 후에는 다음 조건이 반영되었다.

| 속성 | L3+L4 | L3+L4+SA |
|---|---|---|
| `serviceCondition` | 비어 있지 않음 | 10~500자 |
| `minQualification` | 비어 있지 않음 | 5~300자 |
| `bidDeadline` | 현재보다 이후 | 현재 + 1시간 이후, 90일 이내 |

의미:

> 같은 필수 입력이라도 Data Dictionary가 있으면 "비어 있지 않다"에서 끝나지 않고, 실제 서비스에서 받을 수 있는 입력 품질의 하한과 상한이 생긴다.

관련 코드:

- `src/main/java/com/zippt/l3l4/server/domain/AuctionCondition.java`
- `src/main/java/com/zippt/l3l4sa/server/domain/AuctionCondition.java`

### 3.4 공인중개사 자격 검증 강화

L3+L4에서는 입찰 시 `AgentCredential`의 상태가 `VERIFIED`인지 확인하는 방향이다.

SA 적용 후에는 다음 검증이 추가되었다.

- `credentialId`는 `credential-` prefix
- `agentId`는 `agent-` prefix
- `licenseNumber` 형식 검증
- `officeRegistrationNumber` 형식 검증
- `VERIFIED` 상태가 아니면 입찰 불가

의미:

> L3+L4는 "자격 정보가 있다"와 "검증된 중개사만 입찰한다"를 구조로 표현했다. SA는 자격 정보 자체가 올바른 데이터인지까지 판단하게 만든다.

관련 코드:

- `src/main/java/com/zippt/l3l4/server/domain/AgentCredential.java`
- `src/main/java/com/zippt/l3l4sa/server/domain/AgentCredential.java`

### 3.5 예약 중복 방지와 후기 작성 조건 강화

L3+L4에도 `Reservation`, `Review`, `ReservationManager`, `ReviewManager`가 존재한다. 하지만 상대적으로 구조만 있고 저장소와의 연결이나 중복 제약은 약하다.

SA 적용 후에는 `DataStore`에 예약과 후기를 저장하는 Map이 추가되고, Manager에서 다음 규칙을 검사한다.

예약:

- 방문 예정 시간은 현재 이후부터 60일 이내
- 동일 `agentId + visitAt` 조합의 예약 중복 방지

후기:

- `VISITED` 상태의 예약에 대해서만 후기 작성 가능
- 하나의 예약에는 후기 1개만 작성 가능
- rating은 1~5
- 후기 본문은 10~1000자

의미:

> 이 부분은 SA의 효과가 가장 명확하다. L3+L4에서는 예약과 후기가 별도 객체로 분리되는 데 그쳤다면, SA에서는 "언제 예약 가능한가", "언제 후기를 쓸 수 있는가", "중복 작성은 가능한가" 같은 업무 규칙이 실행 코드로 들어간다.

관련 코드:

- `src/main/java/com/zippt/l3l4sa/server/service/DataStore.java`
- `src/main/java/com/zippt/l3l4sa/server/service/ReservationManager.java`
- `src/main/java/com/zippt/l3l4sa/server/service/ReviewManager.java`
- `src/main/java/com/zippt/l3l4sa/server/domain/Review.java`

### 3.6 오류 원인 분류 가능

L3+L4에서는 검증 실패가 주로 `false` 반환이나 일반 예외로 처리된다. 이 방식은 실패했다는 사실은 알 수 있지만, 어떤 데이터 규칙을 위반했는지 분류하기 어렵다.

L3+L4+SA에서는 `ValidationErrorCode`와 `DomainValidationException`이 추가되어 다음처럼 실패 원인을 분류할 수 있다.

- `BID_COMMISSION_RATE_INVALID`
- `CRITERIA_WEIGHT_INVALID`
- `CRITERIA_PRIORITY_WEIGHT_INVALID`
- `AUCTION_DEADLINE_INVALID`
- `RESERVATION_DUPLICATE_INVALID`
- `REVIEW_RESERVATION_INVALID`
- `REVIEW_DUPLICATE_INVALID`
- `NOTIFICATION_STATUS_INVALID`

의미:

> Data Dictionary가 있으면 AI가 단순한 조건문만 만드는 것이 아니라, 실패 원인을 도메인 언어로 분류하는 코드까지 생성할 가능성이 높아진다.

---

## 4. L3/L4/SA의 역할 차이

| 단계 | 주된 역할 | 코드에서 보인 변화 |
|---|---|---|
| L3 Static Modeling | 도메인 개념과 속성 고정 | `Auction`, `BidProposal`, `WinnerSelectionCriteria`, `Reservation`, `Review` 등 Entity 생성 |
| L4 Object Structuring | 객체 책임과 실행 위치 고정 | `client/server/common`, Interface/Control/Entity/BL 분리 |
| SA Data Dictionary | 속성의 허용 값과 실패 조건 구체화 | 범위, 길이, 형식, 상태 조건, 중복 조건, 오류 코드 추가 |

정리하면, L3/L4는 "무엇이 존재하고 누가 책임지는가"를 결정하고, SA는 "그 값이 정확히 어떤 조건을 만족해야 하는가"를 결정한다.

---

## 5. 발표자료에 넣기 좋은 관찰 문장

> L3+L4에서는 구조가 좋아졌지만, 여전히 일부 검증은 AI가 일반적으로 추론한 수준에 머물렀다. 예를 들어 수수료율은 0~100으로 처리되었다.

> Data Dictionary를 추가하자 수수료율 0~10, 가중치 합계 1.0, 예약 중복 방지, 후기 작성 조건처럼 업무에서 실제로 중요한 제약이 코드에 반영되었다.

> 즉 SA는 새로운 구조를 만드는 단계라기보다, L3/L4에서 만든 구조의 빈칸을 도메인 규칙으로 채우는 단계였다.

> 특히 `WinnerSelectionCriteria`는 L3에서 Entity로 등장하고, L4에서 Register/Select Winner 흐름에 배치되며, SA에서 유효한 가중치 조건까지 갖추게 된다. 이 객체 하나만 봐도 명세 수준이 올라갈수록 코드가 어떻게 구체화되는지 설명할 수 있다.

> L3+L4+SA의 개선은 단순히 코드가 길어진 것이 아니라, 잘못된 입력이 시스템 상태를 오염시키기 전에 차단되는 방향으로 바뀐 것이다.

---

## 6. 남은 한계

- 현재 코드는 과제 비교용 생성 코드이므로 실제 DB transaction이나 persistence framework를 사용하지 않는다.
- `DataStore`는 in-memory Map 기반이라 운영 수준의 동시성 보장으로 해석하면 안 된다.
- 모든 Data Dictionary 항목이 완전히 구현된 것은 아니며, 핵심 Use Case인 Register Auction, Submit Bid, Select Winner, Reservation, Review 중심으로 반영되었다.
- 테스트 코드는 아직 별도로 작성하지 않았기 때문에, 발표에서는 `javac` 컴파일과 demo 실행 통과를 생성 코드 검증 수준으로 제시하는 것이 적절하다.

---

## 7. Part C 결론

Specification Augmentation으로 Data Dictionary를 추가했을 때 가장 큰 변화는 validation 품질이다. L3+L4가 도메인 객체와 객체 책임을 분리했다면, L3+L4+SA는 그 객체들이 받아들일 수 있는 값의 범위와 상태 조건을 명확히 했다.

따라서 Part C에서는 다음 결론을 중심으로 잡는 것이 좋다.

> 추가 명세는 AI 코드의 구조를 크게 뒤집지는 않았지만, 도메인 데이터의 허용 범위, 중복 조건, 상태 전이 조건, 오류 원인 분류를 구체화했다. 이로 인해 생성 코드는 더 방어적이고 설명 가능한 형태가 되었다.
