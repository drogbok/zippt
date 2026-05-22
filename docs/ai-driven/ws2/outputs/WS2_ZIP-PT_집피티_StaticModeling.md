# WS2_ZIP-PT_집피티_StaticModeling

## 0. 작성 기준

- 대상 시스템: 집피티(ZIP-PT)
- 단계: L3 Static Modeling
- 입력 산출물:
  - L0 Problem Description: `docs/project_proposal.md`
  - L1 Use Case Diagram: `docs/ai-driven/ws1/submitted/[제출] WS1_ZIP-PT_집피티_L1.md`
  - L2 Use Case Description: `docs/ai-driven/ws1/submitted/[제출] WS1_ZIP-PT_집피티_UseCaseDescription.md`
  - L2 baseline 코드 기준: `src/main/java/com/zippt/l2`
- 주요 분석 범위:
  - L2에서 상세화된 핵심 연속 흐름: Register Auction -> Submit Bid -> Select Winner
  - L0/L1에서 확인되는 전체 도메인: 계정, 매물, 예약, 후기, 외부 데이터

> 이 문서는 L3 Static Modeling 결과만 정의한다. Control Object, Business Logic Object, Service, Repository, DTO, Manager, Controller 등 구현 설계 용어는 L4 이후 단계에서 다룬다.

---

## 1. Problem Domain Static Modeling

### 1.1 물리적 클래스 식별

| 물리적 클래스 | 도메인 의미 | 식별 근거 |
|---|---|---|
| User | ZIP-PT를 사용하는 사람의 공통 개념 | L0 주요 액터, L1 계정 및 인증 UC |
| Buyer | 매물을 검색하고 방문 예약 및 후기를 작성하는 매수자 | L1 Actor, L0 매수자 설명 |
| Seller | 매물을 등록하고 역경매 공고를 생성하며 낙찰자를 선정하는 매도자 | L1 Actor, L2 Register Auction/Select Winner |
| Agent | 입찰에 참여하고 방문 예약을 관리하는 공인중개사 | L1 Actor, L2 Submit Bid |
| AgentCredential | 공인중개사의 자격 증명 정보 | L2 Submit Bid P3 |
| Property | 매도자가 등록하거나 매수자가 검색하는 부동산 매물 | L0 매물 검색/등록, L2 Register Auction P2 |
| PropertyCondition | 매물 검색 조건 또는 필터 조건 | L0 자연어 검색/필터링 |
| Auction | 매물에 대해 생성되는 역경매 공고 | L2 Register Auction, Submit Bid, Select Winner |
| AuctionCondition | 경매 공고 조건 | L2 Register Auction 6단계 |
| WinnerSelectionCriteria | 낙찰 우선 기준 | L2 Register Auction 7단계, Submit Bid/Select Winner 참조 |
| Bid | 공인중개사가 제출하는 입찰서 | L2 Submit Bid |
| BidProposal | 입찰서의 제안 내용 | L2 Submit Bid 6단계 |
| Reservation | 매수자가 매물 방문을 요청하고 확정하는 예약 | L0 방문 예약 시스템, L1 Request/Manage/Confirm Visit |
| Review | 방문 완료 후 작성되는 후기 | L0 후기 시스템, L1 Register Review |
| Notification | 사용자에게 전달되는 알림 | L2 Register Auction 12단계, Submit Bid 11단계, Select Winner 12단계 |
| MarketData | 실거래가 등 외부 정형 데이터 | L0 외부 데이터 API |
| NewsData | 뉴스 등 외부 비정형 데이터 | L0 외부 데이터 API |

### 1.2 물리적 클래스 관계 및 다중성

| 관계 | 다중성 | 관계 유형 | 설명 |
|---|---:|---|---|
| User -> Buyer/Seller/Agent | 1 -> 0..1 each | Generalization | Buyer, Seller, Agent는 사용자 역할의 특수화 |
| Seller -> Property | 1 -> 0..* | Association | 한 매도자는 여러 매물을 보유할 수 있음 |
| Property -> Auction | 1 -> 0..* | Association | 하나의 매물에 대해 시간상 여러 경매 공고가 생성될 수 있음 |
| Auction -> AuctionCondition | 1 -> 1 | Composition | 공고 조건은 특정 경매 공고의 일부 |
| Auction -> WinnerSelectionCriteria | 1 -> 1 | Composition | 낙찰 기준은 특정 경매 공고 등록 시 설정됨 |
| Agent -> AgentCredential | 1 -> 1 | Composition | 자격 증명은 공인중개사 식별/검증 정보 |
| Auction -> Bid | 1 -> 0..* | Composition | 입찰서는 특정 경매 공고에 접수됨 |
| Agent -> Bid | 1 -> 0..* | Association | 한 공인중개사는 여러 경매에 입찰 가능 |
| Bid -> BidProposal | 1 -> 1 | Composition | 제안 내용은 입찰서의 일부 |
| Buyer -> Reservation | 1 -> 0..* | Association | 매수자는 여러 방문 예약을 신청 가능 |
| Property -> Reservation | 1 -> 0..* | Association | 하나의 매물에 대해 여러 방문 예약이 가능 |
| Agent -> Reservation | 1 -> 0..* | Association | 중개사는 여러 방문 예약을 관리 가능 |
| Reservation -> Review | 1 -> 0..1 | Association | 방문 완료 예약 1건당 후기는 최대 1건 |
| User -> Notification | 1 -> 0..* | Association | 사용자는 여러 알림을 받을 수 있음 |
| Property -> MarketData | 1 -> 0..* | Association | 매물은 여러 실거래가 데이터와 비교 가능 |
| Property -> NewsData | 1 -> 0..* | Association | 매물/지역은 여러 뉴스 데이터와 연결 가능 |

> 관계 식별 기준: 이 단계에서는 "누가 처리한다"가 아니라 "어떤 도메인 정보가 서로 구조적으로 연결되는가"만 판단했다.

---

## 2. System Context Static Modeling

### 2.1 시스템 경계

| 요소 | 스테레오타입 | 설명 |
|---|---|---|
| ZIP-PT System | «system» | AI 기반 부동산 매칭 및 거래 지원 플랫폼 |

### 2.2 외부 클래스 식별

| 외부 클래스 | 스테레오타입 | 시스템과의 관계 | 식별 근거 |
|---|---|---|---|
| Buyer | «external user» | Buyer (0..*) -> ZIP-PT System (1) | 매물 검색, 방문 예약, 후기 작성 |
| Seller | «external user» | Seller (0..*) -> ZIP-PT System (1) | 매물 등록, 경매 공고 등록, 낙찰자 선정 |
| Agent | «external user» | Agent (0..*) -> ZIP-PT System (1) | 입찰 참여, 예약 관리, 임장 완료 확인 |
| ExternalDataAPI | «external system» | ZIP-PT System (1) -> ExternalDataAPI (0..*) | 국토교통부 실거래가, 포털 뉴스 데이터 제공 |

### 2.3 Client/Server 분리 근거

L0에서는 Presentation Tier, Application/Logic Tier, Data Tier로 구성된 Multi-tier 구조가 제시되어 있다. L3에서는 세부 객체 배치는 수행하지 않지만, 시스템 컨텍스트 관점에서 다음 분리를 전제로 둔다.

| 서브시스템 | 스테레오타입 | 포함 경계 |
|---|---|---|
| ZIP-PT Client Subsystem | «subsystem» | Buyer/Seller/Agent가 입력을 제공하고 결과를 확인하는 사용자 접근 경계 |
| ZIP-PT Server Subsystem | «subsystem» | 매물, 경매, 입찰, 예약, 후기, 알림, 외부 데이터 연동 정보를 보관/처리하는 시스템 내부 경계 |

> 실제 Interface Object, Control Object, Business Logic Object 배치는 L4 Object Structuring에서 결정한다.

---

## 3. Entity Class Static Modeling

### 3.1 엔티티 클래스 목록

| 엔티티 클래스 | 스테레오타입 | 주요 책임 데이터 |
|---|---|---|
| User | «entity» | 사용자 공통 식별/인증 정보 |
| Buyer | «entity» | 매수자 역할 정보 |
| Seller | «entity» | 매도자 역할 정보 |
| Agent | «entity» | 공인중개사 역할 정보 |
| AgentCredential | «entity» | 공인중개사 자격 증명 |
| Property | «entity» | 매물 기본 정보 |
| PropertyCondition | «entity» | 검색/필터 조건 |
| Auction | «entity» | 역경매 공고 |
| AuctionCondition | «entity» | 공고 조건 |
| WinnerSelectionCriteria | «entity» | 낙찰 우선 기준 |
| Bid | «entity» | 입찰서 접수 상태 |
| BidProposal | «entity» | 입찰 제안 내용 |
| Reservation | «entity» | 방문 예약 상태 |
| Review | «entity» | 방문 후기/평판 |
| Notification | «entity» | 사용자 알림 |
| MarketData | «entity» | 실거래가 데이터 |
| NewsData | «entity» | 뉴스 데이터 |

### 3.2 엔티티 클래스 속성

#### «entity» User

```text
- userId : String
- name : String
- email : String
- passwordHash : String
- role : UserRole
- authStatus : AuthStatus = LOGGED_OUT
```

#### «entity» Buyer extends User

```text
- preferredRegion : String
- preferredPriceMin : Decimal
- preferredPriceMax : Decimal
```

#### «entity» Seller extends User

```text
- sellerGrade : String
```

#### «entity» Agent extends User

```text
- officeName : String
- serviceRegion : String
- reputationScore : Decimal = 0.00
- credentialStatus : CredentialStatus
```

#### «entity» AgentCredential

```text
- credentialId : String
- agentId : String
- licenseNumber : String
- officeRegistrationNumber : String
- verifiedAt : datetime
- status : CredentialStatus
```

#### «entity» Property

```text
- propertyId : String
- sellerId : String
- address : String
- region : String
- areaSquareMeter : Decimal
- askingPrice : Decimal
- propertyType : PropertyType
- description : String
- registeredAt : datetime
- status : PropertyStatus
```

#### «entity» PropertyCondition

```text
- conditionId : String
- region : String
- priceMin : Decimal
- priceMax : Decimal
- areaMin : Decimal
- areaMax : Decimal
- naturalLanguageQuery : String
```

#### «entity» Auction

```text
- auctionId : String
- propertyId : String
- sellerId : String
- status : AuctionStatus = DRAFT
- bidDeadline : datetime
- bidCount : int = 0
- createdAt : datetime
- selectedBidId : String
```

#### «entity» AuctionCondition

```text
- auctionConditionId : String
- auctionId : String
- serviceCondition : String
- minQualification : String
- deadlinePolicy : String
```

#### «entity» WinnerSelectionCriteria

```text
- criteriaId : String
- auctionId : String
- priorityType : WinnerPriorityType
- commissionRateWeight : Decimal
- marketingStrategyWeight : Decimal
```

#### «entity» Bid

```text
- bidId : String
- auctionId : String
- agentId : String
- submittedAt : datetime
- status : BidStatus = DRAFT
- receiptNumber : String
- isResubmitted : bool = false
```

#### «entity» BidProposal

```text
- bidProposalId : String
- bidId : String
- commissionRate : Decimal
- marketingStrategy : String
- expectedSalePeriodDays : int
- serviceTerms : String
```

#### «entity» Reservation

```text
- reservationId : String
- buyerId : String
- propertyId : String
- agentId : String
- requestedAt : datetime
- visitAt : datetime
- status : ReservationStatus = PENDING
```

#### «entity» Review

```text
- reviewId : String
- reservationId : String
- buyerId : String
- agentId : String
- propertyId : String
- rating : int
- text : String
- createdAt : datetime
```

#### «entity» Notification

```text
- notificationId : String
- receiverUserId : String
- notificationType : NotificationType
- message : String
- createdAt : datetime
- readAt : datetime
- status : NotificationStatus = UNREAD
```

#### «entity» MarketData

```text
- marketDataId : String
- region : String
- propertyType : PropertyType
- transactionPrice : Decimal
- transactionDate : date
- source : String
```

#### «entity» NewsData

```text
- newsDataId : String
- region : String
- title : String
- contentSummary : String
- publishedAt : datetime
- source : String
```

### 3.3 엔티티 관계 및 다중성

| 관계 | 다중성 | 설명 |
|---|---:|---|
| User <|-- Buyer | 1 -> 0..1 | Buyer는 User의 특수 타입 |
| User <|-- Seller | 1 -> 0..1 | Seller는 User의 특수 타입 |
| User <|-- Agent | 1 -> 0..1 | Agent는 User의 특수 타입 |
| Agent (1) -- (1) AgentCredential | 1:1 | 공인중개사 자격 증명 |
| Seller (1) -- (0..*) Property | 1:N | 매도자 소유 매물 |
| Property (1) -- (0..*) Auction | 1:N | 매물별 경매 공고 |
| Auction (1) -- (1) AuctionCondition | 1:1 | 공고 조건 |
| Auction (1) -- (1) WinnerSelectionCriteria | 1:1 | 낙찰 기준 |
| Auction (1) -- (0..*) Bid | 1:N | 경매에 접수된 입찰서 |
| Agent (1) -- (0..*) Bid | 1:N | 공인중개사가 제출한 입찰서 |
| Bid (1) -- (1) BidProposal | 1:1 | 입찰 제안 내용 |
| Buyer (1) -- (0..*) Reservation | 1:N | 매수자 방문 예약 |
| Property (1) -- (0..*) Reservation | 1:N | 매물 방문 예약 |
| Agent (1) -- (0..*) Reservation | 1:N | 중개사 담당 예약 |
| Reservation (1) -- (0..1) Review | 1:0..1 | 예약 1건당 후기 최대 1건 |
| User (1) -- (0..*) Notification | 1:N | 사용자 알림 |
| Property (1) -- (0..*) MarketData | 1:N | 매물/지역 관련 실거래가 |
| Property (1) -- (0..*) NewsData | 1:N | 매물/지역 관련 뉴스 |

### 3.4 Association Class 적용 검토

| 후보 관계 | 판단 | 근거 |
|---|---|---|
| Auction - Agent | Bid를 Association Class 성격의 독립 엔티티로 모델링 | Agent가 Auction에 참여하는 관계 자체에 수수료율, 마케팅 전략, 접수 시각, 상태가 필요함 |
| Buyer - Property | Reservation을 독립 엔티티로 모델링 | Buyer가 Property를 방문하려는 관계 자체에 방문 시각, 담당 Agent, 예약 상태가 필요함 |

---

## 4. Enumeration 정의

### «enumeration» UserRole

```text
BUYER
SELLER
AGENT
```

사용 관계:

```text
User «use» UserRole
```

### «enumeration» AuthStatus

```text
LOGGED_OUT
LOGGED_IN
EXPIRED
```

사용 관계:

```text
User «use» AuthStatus
```

### «enumeration» CredentialStatus

```text
PENDING
VERIFIED
REJECTED
EXPIRED
```

사용 관계:

```text
Agent «use» CredentialStatus
AgentCredential «use» CredentialStatus
```

### «enumeration» PropertyType

```text
APARTMENT
VILLA
OFFICETEL
HOUSE
LAND
```

사용 관계:

```text
Property «use» PropertyType
MarketData «use» PropertyType
```

### «enumeration» PropertyStatus

```text
REGISTERED
ON_AUCTION
RESERVED
SOLD
INACTIVE
```

사용 관계:

```text
Property «use» PropertyStatus
```

### «enumeration» AuctionStatus

```text
DRAFT
OPEN
CLOSED
PENDING_WINNER
WINNER_SELECTED
CANCELLED
```

사용 관계:

```text
Auction «use» AuctionStatus
```

### «enumeration» WinnerPriorityType

```text
PRICE_FIRST
SERVICE_FIRST
BALANCED
```

사용 관계:

```text
WinnerSelectionCriteria «use» WinnerPriorityType
```

### «enumeration» BidStatus

```text
DRAFT
SUBMITTED
RESUBMIT_ALLOWED
WON
LOST
CANCELLED
```

사용 관계:

```text
Bid «use» BidStatus
```

### «enumeration» ReservationStatus

```text
PENDING
CONFIRMED
VISITED
REVIEWED
REJECTED
CANCELLED
```

사용 관계:

```text
Reservation «use» ReservationStatus
```

### «enumeration» NotificationType

```text
NEW_AUCTION
NEW_BID
BID_RESULT
VISIT_REQUEST
VISIT_CONFIRMED
REVIEW_REQUEST
```

사용 관계:

```text
Notification «use» NotificationType
```

### «enumeration» NotificationStatus

```text
UNREAD
READ
FAILED
```

사용 관계:

```text
Notification «use» NotificationStatus
```

---

## 5. L2 핵심 UC와 L3 엔티티 추적성

| L2 UC | 주요 L3 엔티티 | 정적 모델 반영 내용 |
|---|---|---|
| Register Auction | Seller, Property, Auction, AuctionCondition, WinnerSelectionCriteria, Notification | 매도자 소유 매물, 경매 공고, 공고 조건, 낙찰 기준, 공인중개사 알림 구조 |
| Submit Bid | Agent, AgentCredential, Auction, Bid, BidProposal, Notification | 공인중개사 자격 검증, 경매별 입찰서, 제안 조건, 매도자 알림 구조 |
| Select Winner | Seller, Auction, Bid, BidProposal, Notification | 마감 공고, 낙찰/미낙찰 상태, 참여 중개사 결과 알림 구조 |

---

## 6. L4 Object Structuring으로 넘길 결정 사항

- Buyer, Seller, Agent는 L3에서는 Entity Class이면서 System Context의 External User로도 식별된다. L4에서는 외부 사용자별 Interface Object와 내부 Entity Object를 분리해야 한다.
- Bid는 Auction-Agent 관계의 속성을 보유하므로 단순 연결이 아니라 독립 Entity Object로 유지해야 한다.
- Reservation은 Buyer-Property-Agent 관계의 속성을 보유하므로 독립 Entity Object로 유지해야 한다.
- AuctionStatus, BidStatus, ReservationStatus는 상태 기반 Control Object 또는 Entity Operation 설계의 근거가 된다.
- ExternalDataAPI는 L3에서 외부 시스템으로만 식별했고, 구체적인 연동 Interface Object는 L4에서 설계한다.
