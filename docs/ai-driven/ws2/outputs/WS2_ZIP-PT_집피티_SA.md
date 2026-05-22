# WS2_ZIP-PT_집피티_SA

## 0. Specification Augmentation 개요

- 대상 시스템: 집피티(ZIP-PT)
- 단계: Part C Specification Augmentation
- 선택한 추가 명세 유형: Data Dictionary
- 연결 대상:
  - L3 Static Modeling의 Entity Class/Attribute
  - L4 Object Structuring의 Entity Operation/Validation Operation

### 0.1 Data Dictionary를 선택한 이유

L3 Static Modeling은 Entity Class와 속성 타입을 정의했지만, 각 속성의 의미, 허용 범위, 형식, 검증 실패 조건까지는 충분히 고정하지 않았다. Data Dictionary를 추가하면 AI 코드 생성 시 다음 변화가 관찰될 수 있다.

- `String`, `Decimal`, `datetime` 속성에 대한 구체적 validation 코드 생성
- enum 값과 상태 전이 조건의 명확화
- 입력 DTO/Command와 Entity Operation의 검증 책임 강화
- L3+L4 코드 대비 예외 처리와 오류 메시지의 구체화

따라서 Part C에서는 Data Dictionary를 통해 "추가 명세가 AI 코드 생성 품질에 관찰 가능한 차이를 만드는가"를 확인한다.

---

## 1. 공통 데이터 규칙

| 규칙 ID | 대상 | 규칙 | 코드 반영 기대 |
|---|---|---|---|
| DR-001 | 모든 `String` ID | null/blank 금지, 영문 prefix + UUID 또는 시스템 발급 문자열 | 생성자/팩토리에서 ID 검증 |
| DR-002 | 모든 금액 `Decimal` | 음수 금지, 통화 단위 값은 소수점 2자리 이내 | `BigDecimal.signum()` 및 scale 검증 |
| DR-003 | 모든 비율 `Decimal` | 0 이상 100 이하 | 수수료율/가중치 validation |
| DR-004 | 모든 `datetime` | 과거 시각 금지 여부를 속성별로 명시 | deadline/visitAt 검증 분리 |
| DR-005 | 모든 enum | 정의되지 않은 문자열 입력 금지 | 문자열 파싱 시 enum 변환 실패 처리 |
| DR-006 | 사용자 입력 텍스트 | 앞뒤 공백 제거 후 길이 검증 | trim + min/max length 검증 |

---

## 2. Entity별 Data Dictionary

### 2.1 User

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| userId | String | 사용자 식별자 | 필수, `user-` prefix 권장, 중복 금지 | USER_ID_INVALID |
| name | String | 사용자 표시 이름 | 필수, 2~30자 | USER_NAME_INVALID |
| email | String | 로그인/연락 이메일 | 필수, 이메일 형식, 중복 금지 | USER_EMAIL_INVALID |
| passwordHash | String | 해시된 비밀번호 | 필수, 평문 저장 금지 | USER_PASSWORD_HASH_INVALID |
| role | UserRole | 사용자 역할 | BUYER/SELLER/AGENT 중 하나 | USER_ROLE_INVALID |
| authStatus | AuthStatus | 인증 세션 상태 | LOGGED_IN 상태에서만 보호 기능 접근 가능 | USER_AUTH_INVALID |

### 2.2 AgentCredential

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| credentialId | String | 자격 증명 식별자 | 필수, 중복 금지 | CREDENTIAL_ID_INVALID |
| agentId | String | 공인중개사 사용자 ID | 필수, Agent 사용자와 연결되어야 함 | CREDENTIAL_AGENT_INVALID |
| licenseNumber | String | 공인중개사 자격증 번호 | 필수, 대문자/숫자/하이픈만 허용, 6~30자 | LICENSE_NUMBER_INVALID |
| officeRegistrationNumber | String | 사무소 등록번호 | 필수, 대문자/숫자/하이픈만 허용, 6~30자 | OFFICE_REG_NO_INVALID |
| verifiedAt | datetime | 검증 완료 시각 | VERIFIED 상태일 때 필수 | CREDENTIAL_VERIFIED_AT_INVALID |
| status | CredentialStatus | 자격 검증 상태 | VERIFIED 상태에서만 입찰 가능 | CREDENTIAL_STATUS_INVALID |

### 2.3 Property

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| propertyId | String | 매물 식별자 | 필수, `property-` prefix 권장 | PROPERTY_ID_INVALID |
| sellerId | String | 매도자 ID | 필수, Seller 사용자와 연결되어야 함 | PROPERTY_SELLER_INVALID |
| address | String | 상세 주소 | 필수, 5~120자 | PROPERTY_ADDRESS_INVALID |
| region | String | 검색/분석 지역 | 필수, 2~30자 | PROPERTY_REGION_INVALID |
| areaSquareMeter | Decimal | 전용/공급 면적 | 0보다 커야 함 | PROPERTY_AREA_INVALID |
| askingPrice | Decimal | 희망 매도가 | 0보다 커야 함, 소수점 2자리 이내 | PROPERTY_PRICE_INVALID |
| propertyType | PropertyType | 매물 유형 | enum 값 필수 | PROPERTY_TYPE_INVALID |
| description | String | 매물 설명 | 선택, 최대 1000자 | PROPERTY_DESC_TOO_LONG |
| status | PropertyStatus | 매물 상태 | SOLD 상태에서는 신규 경매 등록 불가 | PROPERTY_STATUS_INVALID |

### 2.4 Auction

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| auctionId | String | 경매 공고 식별자 | 필수, `auction-` prefix 권장 | AUCTION_ID_INVALID |
| propertyId | String | 대상 매물 ID | 필수, 동일 매물에 OPEN 경매 중복 금지 | AUCTION_PROPERTY_INVALID |
| sellerId | String | 공고 등록 매도자 ID | 필수, 대상 매물 소유자와 일치 | AUCTION_SELLER_INVALID |
| status | AuctionStatus | 경매 상태 | DRAFT -> OPEN -> CLOSED/PENDING_WINNER -> WINNER_SELECTED/CANCELLED 전이만 허용 | AUCTION_STATUS_INVALID |
| bidDeadline | datetime | 입찰 마감 시각 | 현재 시각보다 최소 1시간 이후, 최대 90일 이내 | AUCTION_DEADLINE_INVALID |
| bidCount | int | 접수된 입찰 수 | 0 이상, Bid 생성/취소와 일관성 유지 | AUCTION_BID_COUNT_INVALID |
| selectedBidId | String | 낙찰 입찰 ID | WINNER_SELECTED 상태에서 필수 | AUCTION_SELECTED_BID_INVALID |

### 2.5 AuctionCondition

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| auctionConditionId | String | 공고 조건 식별자 | 필수 | AUCTION_CONDITION_ID_INVALID |
| auctionId | String | 연결 경매 ID | 필수 | AUCTION_CONDITION_AUCTION_INVALID |
| serviceCondition | String | 매도자가 요구하는 서비스 조건 | 필수, 10~500자 | AUCTION_SERVICE_CONDITION_INVALID |
| minQualification | String | 최소 자격 요건 | 필수, 5~300자 | AUCTION_MIN_QUAL_INVALID |
| bidDeadline | datetime | 입찰 마감 시각 | Auction.bidDeadline과 동일해야 함 | AUCTION_CONDITION_DEADLINE_INVALID |

### 2.6 WinnerSelectionCriteria

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| criteriaId | String | 낙찰 기준 식별자 | 필수 | CRITERIA_ID_INVALID |
| auctionId | String | 연결 경매 ID | 필수 | CRITERIA_AUCTION_INVALID |
| priorityType | WinnerPriorityType | 낙찰 우선 유형 | PRICE_FIRST/SERVICE_FIRST/BALANCED 중 하나 | CRITERIA_PRIORITY_INVALID |
| commissionRateWeight | Decimal | 수수료율 가중치 | 0 이상 1 이하 | CRITERIA_COMMISSION_WEIGHT_INVALID |
| marketingStrategyWeight | Decimal | 마케팅 전략 가중치 | 0 이상 1 이하 | CRITERIA_MARKETING_WEIGHT_INVALID |

추가 불변식:

```text
commissionRateWeight + marketingStrategyWeight = 1.0
```

코드 반영 기대:

- L3+L4 코드의 `validateWeights()`가 "0보다 큰 합계" 수준에서 "합계가 1.0인지" 검증하는 방향으로 강화된다.
- `priorityType`에 따라 최소 가중치 조건을 추가할 수 있다.
  - PRICE_FIRST: commissionRateWeight >= 0.6
  - SERVICE_FIRST: marketingStrategyWeight >= 0.6
  - BALANCED: 각 가중치 0.4~0.6

### 2.7 Bid

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| bidId | String | 입찰서 식별자 | 필수, `bid-` prefix 권장 | BID_ID_INVALID |
| auctionId | String | 대상 경매 ID | 필수, OPEN 상태 경매만 가능 | BID_AUCTION_INVALID |
| agentId | String | 제출 공인중개사 ID | 필수, VERIFIED 자격 상태 필요 | BID_AGENT_INVALID |
| submittedAt | datetime | 제출 시각 | bidDeadline 이전이어야 함 | BID_SUBMITTED_AT_INVALID |
| status | BidStatus | 입찰 상태 | DRAFT -> SUBMITTED/RESUBMIT_ALLOWED -> WON/LOST/CANCELLED | BID_STATUS_INVALID |
| receiptNumber | String | 접수번호 | 제출 완료 시 필수, 중복 금지 | BID_RECEIPT_INVALID |
| isResubmitted | bool | 재제출 여부 | 재제출 시 기존 receiptNumber 유지 | BID_RESUBMIT_INVALID |

추가 불변식:

```text
동일 auctionId + agentId 조합에 대해 SUBMITTED/WON/LOST 상태 Bid는 최대 1건
```

### 2.8 BidProposal

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| bidProposalId | String | 입찰 제안 식별자 | 필수 | BID_PROPOSAL_ID_INVALID |
| bidId | String | 연결 입찰서 ID | 필수 | BID_PROPOSAL_BID_INVALID |
| commissionRate | Decimal | 제안 수수료율 | 0 이상 10 이하, 소수점 2자리 이내 | BID_COMMISSION_RATE_INVALID |
| marketingStrategy | String | 마케팅 전략 | 필수, 20~1000자 | BID_MARKETING_STRATEGY_INVALID |
| expectedSalePeriodDays | int | 예상 매각 기간 | 1~365일 | BID_EXPECTED_PERIOD_INVALID |
| serviceTerms | String | 서비스 조건 | 필수, 10~500자 | BID_SERVICE_TERMS_INVALID |

코드 반영 기대:

- L3+L4 코드의 `validateCommissionRate()`는 현재 0~100 범위인데, SA 적용 후 0~10으로 강화된다.
- `marketingStrategy`, `serviceTerms`는 단순 blank 검증에서 최소/최대 길이 검증으로 강화된다.

### 2.9 Reservation

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| reservationId | String | 예약 식별자 | 필수, `reservation-` prefix 권장 | RESERVATION_ID_INVALID |
| buyerId | String | 방문 요청 매수자 ID | 필수, Buyer 사용자와 연결 | RESERVATION_BUYER_INVALID |
| propertyId | String | 방문 대상 매물 ID | 필수, INACTIVE/SOLD 매물 예약 불가 | RESERVATION_PROPERTY_INVALID |
| agentId | String | 담당 중개사 ID | 필수, VERIFIED 자격 상태 필요 | RESERVATION_AGENT_INVALID |
| requestedAt | datetime | 예약 요청 시각 | 시스템 시각으로 자동 생성 | RESERVATION_REQUESTED_AT_INVALID |
| visitAt | datetime | 방문 예정 시각 | 현재 시각보다 이후, 최대 60일 이내 | RESERVATION_VISIT_AT_INVALID |
| status | ReservationStatus | 예약 상태 | PENDING -> CONFIRMED -> VISITED -> REVIEWED 전이만 허용, REJECTED/CANCELLED는 종료 상태 | RESERVATION_STATUS_INVALID |

추가 불변식:

```text
동일 agentId + visitAt 조합에 CONFIRMED/PENDING 예약은 최대 1건
```

### 2.10 Review

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| reviewId | String | 후기 식별자 | 필수, `review-` prefix 권장 | REVIEW_ID_INVALID |
| reservationId | String | 연결 예약 ID | 필수, VISITED 상태 예약만 가능 | REVIEW_RESERVATION_INVALID |
| buyerId | String | 후기 작성 매수자 ID | 예약의 buyerId와 일치해야 함 | REVIEW_BUYER_INVALID |
| agentId | String | 평가 대상 중개사 ID | 예약의 agentId와 일치해야 함 | REVIEW_AGENT_INVALID |
| propertyId | String | 방문 매물 ID | 예약의 propertyId와 일치해야 함 | REVIEW_PROPERTY_INVALID |
| rating | int | 별점 | 1~5 정수 | REVIEW_RATING_INVALID |
| text | String | 후기 본문 | 필수, 10~1000자 | REVIEW_TEXT_INVALID |
| createdAt | datetime | 작성 시각 | 시스템 시각으로 자동 생성 | REVIEW_CREATED_AT_INVALID |

추가 불변식:

```text
하나의 reservationId에 Review는 최대 1건
```

### 2.11 Notification

| 속성 | 타입 | 의미 | 제약 조건 | 오류 코드 |
|---|---|---|---|---|
| notificationId | String | 알림 식별자 | 필수, `noti-` prefix 권장 | NOTIFICATION_ID_INVALID |
| receiverUserId | String | 수신 사용자 ID | 필수, 존재하는 User와 연결 | NOTIFICATION_RECEIVER_INVALID |
| notificationType | NotificationType | 알림 유형 | enum 값 필수 | NOTIFICATION_TYPE_INVALID |
| message | String | 알림 메시지 | 필수, 5~300자 | NOTIFICATION_MESSAGE_INVALID |
| createdAt | datetime | 생성 시각 | 시스템 시각으로 자동 생성 | NOTIFICATION_CREATED_AT_INVALID |
| readAt | datetime | 읽음 시각 | READ 상태일 때만 필수 | NOTIFICATION_READ_AT_INVALID |
| status | NotificationStatus | 알림 상태 | UNREAD -> READ 또는 FAILED | NOTIFICATION_STATUS_INVALID |

---

## 3. Use Case별 추가 검증 규칙

### 3.1 Register Auction

| 단계 | 추가 규칙 | 관련 Data Dictionary |
|---|---|---|
| 공고 대상 매물 선택 | 매물은 Seller 소유여야 하며 SOLD/INACTIVE 상태가 아니어야 함 | Property.sellerId, Property.status |
| 공고 조건 입력 | serviceCondition 10~500자, minQualification 5~300자 | AuctionCondition |
| 입찰 마감 기한 입력 | 현재 시각 + 1시간 이후, 90일 이내 | Auction.bidDeadline |
| 낙찰 기준 설정 | 가중치 합계 1.0, priorityType별 최소 가중치 충족 | WinnerSelectionCriteria |
| 공고 등록 확정 | 동일 propertyId에 OPEN 경매가 없어야 함 | Auction.propertyId/status |

### 3.2 Submit Bid

| 단계 | 추가 규칙 | 관련 Data Dictionary |
|---|---|---|
| 공고 선택 | 경매 상태 OPEN, 제출 시각이 bidDeadline 이전 | Auction.status, Auction.bidDeadline |
| 공인중개사 검증 | AgentCredential.status가 VERIFIED | AgentCredential |
| 입찰서 작성 | commissionRate 0~10, marketingStrategy 20~1000자, serviceTerms 10~500자 | BidProposal |
| 최종 제출 | 동일 auctionId + agentId 활성 입찰 최대 1건 | Bid |
| 재제출 | 기존 receiptNumber 유지, isResubmitted = true | Bid |

### 3.3 Select Winner

| 단계 | 추가 규칙 | 관련 Data Dictionary |
|---|---|---|
| 마감 공고 선택 | CLOSED 또는 PENDING_WINNER 상태만 가능 | Auction.status |
| 입찰서 목록 표시 | BidStatus.SUBMITTED 입찰만 후보 | Bid.status |
| 기준 적용 정렬 | WinnerSelectionCriteria.calculateBidScore() 사용 | WinnerSelectionCriteria, BidProposal |
| 낙찰 확정 | selectedBidId는 해당 auctionId의 Bid여야 함 | Bid.auctionId |
| 결과 반영 | 선택 Bid는 WON, 나머지는 LOST, Auction은 WINNER_SELECTED | Bid.status, Auction.status |

---

## 4. L3+L4 코드 대비 관찰 가능한 변화 예상

| 비교 항목 | L3+L4 코드 | L3+L4+SA 코드 기대 변화 |
|---|---|---|
| 수수료율 검증 | `BidProposal.validateCommissionRate()`가 0~100 범위 | 0~10 범위, 소수점 2자리 검증 |
| 낙찰 기준 검증 | `validateWeights()`가 0보다 큰 합계 정도 검증 | 합계 1.0, priorityType별 최소 가중치 검증 |
| 텍스트 입력 검증 | blank 검증 중심 | 최소/최대 길이 검증 추가 |
| 자격증 번호 검증 | VERIFIED 상태 중심 | licenseNumber/officeRegistrationNumber 형식 검증 추가 |
| 예약 검증 | 구조만 존재 | agentId + visitAt 중복 예약 방지 규칙 강화 |
| 후기 검증 | rating 1~5 검증 중심 | VISITED 예약 여부, 예약당 후기 1건 검증 추가 |
| 오류 표현 | `IllegalArgumentException` 중심 | 오류 코드 enum 또는 도메인별 ValidationException 생성 가능 |

---

## 5. SA 적용 프롬프트

다음 단계에서 L3+L4+SA 코드를 생성할 때 사용할 프롬프트는 아래와 같다.

```text
너는 COMET UML 기반 AI-Driven 개발 방법론의 L3+L4+Specification Augmentation 단계에 따라 Java 코드를 생성한다.

입력 산출물:
1. L3 Static Modeling 문서
2. L4 Object Structuring 문서
3. Specification Augmentation - Data Dictionary 문서

요구사항:
- 기존 L3+L4 코드 구조(client/common/server)는 유지한다.
- Data Dictionary의 제약 조건을 Entity 생성자, Entity Operation, Transaction Manager validation에 반영한다.
- 단순 null/blank 검증을 넘어 범위, 길이, enum, 상태 전이, 중복 제약을 코드로 구현한다.
- 검증 실패는 공통 ValidationErrorCode 또는 도메인별 ValidationException으로 표현한다.
- Register Auction, Submit Bid, Select Winner에서 Data Dictionary 규칙이 실제로 호출되도록 연결한다.
- 동시성 경계는 L4와 동일하게 auctionId/propertyId/agentId+visitAt/reservationId 단위 lock을 사용한다.
- 외부 프레임워크 없이 순수 Java로 작성한다.

관찰 목표:
- L3+L4 코드 대비 validation 코드가 구체화되는지 확인한다.
- `WinnerSelectionCriteria`, `BidProposal`, `AuctionCondition`, `AgentCredential`, `Reservation`, `Review`의 검증 책임이 강화되는지 확인한다.
- 오류 원인이 명확한 코드/예외로 분리되는지 확인한다.
```

---

## 6. Part C 분석에서 사용할 관찰 항목

| 관찰 항목 | 측정 방법 |
|---|---|
| Validation 메서드 수 변화 | L3+L4와 L3+L4+SA의 `validate*`, `ensure*`, `check*` 메서드 수 비교 |
| 오류 코드/예외 클래스 수 변화 | ValidationErrorCode 또는 Exception 클래스 수 비교 |
| Data Dictionary 반영률 | SA 표의 주요 제약 조건 중 코드에 반영된 항목 수 |
| 도메인별 검증 위치 | Entity 내부 검증인지 Transaction Manager 검증인지 분류 |
| 기존 구조 유지 여부 | client/server/common 패키지 구조가 유지되는지 확인 |

