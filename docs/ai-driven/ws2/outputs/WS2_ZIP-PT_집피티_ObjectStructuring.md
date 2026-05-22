# WS2_ZIP-PT_집피티_ObjectStructuring

## 0. 작성 기준

- 대상 시스템: 집피티(ZIP-PT)
- 단계: L4 Object Structuring
- 입력 산출물:
  - L0 Problem Description: `docs/project_proposal.md`
  - L1 Use Case Diagram: `docs/ai-driven/ws1/submitted/[제출] WS1_ZIP-PT_집피티_L1.md`
  - L2 Use Case Description: `docs/ai-driven/ws1/submitted/[제출] WS1_ZIP-PT_집피티_UseCaseDescription.md`
  - L3 Static Modeling: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_StaticModeling.md`
- 주요 분석 범위:
  - 핵심 연속 흐름: Register Auction -> Submit Bid -> Select Winner
  - L3에서 식별한 전체 엔티티 중 L2 코드 비교에 직접 영향을 주는 경매/입찰 중심 객체

> 이 문서는 L4 Object Structuring 결과를 정의한다. 실제 Java 구현은 다음 단계인 L3+L4 코드 생성에서 수행한다.

---

## 1. Client/Server 식별

### 1.1 서브시스템 분해

| 서브시스템 | 스테레오타입 | 책임 |
|---|---|---|
| ZIP-PT Client Subsystem | «subsystem» | Buyer/Seller/Agent 입력 수집, 화면/콘솔 출력, 처리 세션 중 임시 데이터 보관, 서버 요청 송신 |
| ZIP-PT Server Subsystem | «subsystem» | 사용자 인증, 매물/경매/입찰/예약/후기/알림 영속 데이터 관리, 핵심 비즈니스 규칙 처리, 동시성 제어 |
| External Data Subsystem | «external system» | 실거래가/뉴스 데이터 제공 |

### 1.2 Use Case 패키징

| 원 UC | Client UC | Server UC | 관계 |
|---|---|---|---|
| Register Auction | Client Register Auction | Server Register Auction | Client UC «include» Server UC |
| Submit Bid | Client Submit Bid | Server Submit Bid | Client UC «include» Server UC |
| Select Winner | Client Select Winner | Server Select Winner | Client UC «include» Server UC |
| Validate User | Client Validate User | Server Validate User | Client UC «include» Server UC |
| Search Property on Map | Client Search Property | Server Search Property | Client UC «include» Server UC |
| Request Visit | Client Request Visit | Server Request Visit | Client UC «include» Server UC |
| Register Review | Client Register Review | Server Register Review | Client UC «include» Server UC |

### 1.3 배치 원칙

| 객체/데이터 유형 | 배치 | 근거 |
|---|---|---|
| 사용자 입력 양식, 선택 화면 상태 | Client | 처리 세션 동안만 필요하며 외부 사용자와 직접 상호작용 |
| Auction, Bid, Property, User, AgentCredential | Server | 여러 사용자가 공유하고 저장소 일관성이 필요함 |
| WinnerSelectionCriteria | Server | Register Auction에서 생성되고 Submit Bid/Select Winner에서 재사용됨 |
| Notification | Server | 여러 사용자에게 전달되며 실패/읽음 상태 추적이 필요함 |
| MarketData, NewsData | Server | 외부 데이터 연동 결과를 검색/분석에 재사용 |

---

## 2. Interface Object 식별

### 2.1 외부 클래스 -> Interface Object 변환

| L3 External Class | Interface Object | 스테레오타입 | 배치 | 책임 |
|---|---|---|---|---|
| Buyer | BuyerInterface | «user interface» | client.ui | 매수자의 매물 검색, 예약, 후기 입력/출력 |
| Seller | SellerInterface | «user interface» | client.ui | 매도자의 매물 선택, 공고 조건 입력, 낙찰자 선정 입력/출력 |
| Agent | AgentInterface | «user interface» | client.ui | 공인중개사의 입찰서 작성, 예약 관리 입력/출력 |
| ExternalDataAPI | ExternalDataAPIInterface | «external system interface» | server.integration | 실거래가/뉴스 데이터 요청 및 응답 변환 |

### 2.2 핵심 Interface Object Operation

#### «user interface» SellerInterface

```text
+ requestAuctionRegistration(sellerId : String) : None
+ selectProperty(propertyId : String) : None
+ enterAuctionCondition(conditionData : AuctionConditionInput) : None
+ enterWinnerSelectionCriteria(criteriaInput : CriteriaInput) : None
+ confirmAuctionRegistration() : None
+ requestClosedAuctionList(sellerId : String) : None
+ selectWinner(auctionId : String, bidId : String) : None
+ postponeWinnerSelection(auctionId : String) : None
+ showMessage(message : String) : None
```

#### «user interface» AgentInterface

```text
+ requestOpenAuctionList(agentId : String) : None
+ selectAuction(auctionId : String) : None
+ enterBidProposal(proposalInput : BidProposalInput) : None
+ confirmBidSubmission() : None
+ cancelBidSubmission() : None
+ showMessage(message : String) : None
```

#### «user interface» BuyerInterface

```text
+ requestPropertySearch(conditionInput : PropertyConditionInput) : None
+ requestVisit(propertyId : String, agentId : String, visitAt : datetime) : None
+ enterReview(reservationId : String, rating : int, text : String) : None
+ showMessage(message : String) : None
```

#### «external system interface» ExternalDataAPIInterface

```text
+ fetchMarketData(region : String, propertyType : PropertyType) : Collection<MarketData>
+ fetchNewsData(region : String) : Collection<NewsData>
```

> Interface Object는 외부 입출력만 담당한다. 인증, 검증, 경매 상태 변경, 입찰 저장, 낙찰 확정 같은 비즈니스 규칙은 서버 Control/Business Logic Object로 위임한다.

---

## 3. Client Object 설계

### 3.1 Client Control Object

#### «state dependent control» AuctionClientControl

| 항목 | 내용 |
|---|---|
| 배치 | client.control |
| 책임 | Seller/Agent의 경매 관련 UI 흐름 상태 전이 관리 |
| 상태 | IDLE, REGISTERING_AUCTION, WRITING_BID, SELECTING_WINNER, WAITING_SERVER_RESPONSE, COMPLETED, CANCELLED |
| 협력 객체 | SellerInterface, AgentInterface, AuctionSession, BidSession, ZIPPTServerGateway |

```text
+ startRegisterAuction(sellerId : String) : None
+ propertySelected(propertyId : String) : None
+ auctionConditionEntered(input : AuctionConditionInput) : None
+ criteriaEntered(input : CriteriaInput) : None
+ confirmAuctionRegistration() : None
+ startSubmitBid(agentId : String) : None
+ auctionSelected(auctionId : String) : None
+ bidProposalEntered(input : BidProposalInput) : None
+ confirmBidSubmission() : None
+ startSelectWinner(sellerId : String) : None
+ winnerSelected(auctionId : String, bidId : String) : None
+ cancelCurrentFlow() : None
```

상태 전이:

| 현재 상태 | 이벤트 | 다음 상태 |
|---|---|---|
| IDLE | startRegisterAuction | REGISTERING_AUCTION |
| REGISTERING_AUCTION | confirmAuctionRegistration | WAITING_SERVER_RESPONSE |
| IDLE | startSubmitBid | WRITING_BID |
| WRITING_BID | confirmBidSubmission | WAITING_SERVER_RESPONSE |
| IDLE | startSelectWinner | SELECTING_WINNER |
| SELECTING_WINNER | winnerSelected | WAITING_SERVER_RESPONSE |
| WAITING_SERVER_RESPONSE | serverSuccess | COMPLETED |
| WAITING_SERVER_RESPONSE | serverFailure | IDLE |
| 모든 처리 상태 | cancelCurrentFlow | CANCELLED |

### 3.2 Client Transient Entity Object

#### «entity» AuctionSession

| 항목 | 내용 |
|---|---|
| 배치 | client.domain |
| 생명주기 | Client Register Auction 처리 세션 동안만 존재 |
| 책임 | 매물 선택, 공고 조건, 낙찰 기준 입력 데이터를 서버 요청 전까지 누적 |

```text
- sellerId : String
- selectedPropertyId : String
- conditionInput : AuctionConditionInput
- criteriaInput : CriteriaInput

+ storeSeller(sellerId : String) : None
+ storeSelectedProperty(propertyId : String) : None
+ storeAuctionCondition(input : AuctionConditionInput) : None
+ storeWinnerSelectionCriteria(input : CriteriaInput) : None
+ toRegisterAuctionCommand() : RegisterAuctionCommand
+ discard() : None
```

#### «entity» BidSession

| 항목 | 내용 |
|---|---|
| 배치 | client.domain |
| 생명주기 | Client Submit Bid 처리 세션 동안만 존재 |
| 책임 | 공고 선택, 입찰 제안 입력 데이터를 서버 요청 전까지 누적 |

```text
- agentId : String
- selectedAuctionId : String
- proposalInput : BidProposalInput

+ storeAgent(agentId : String) : None
+ storeSelectedAuction(auctionId : String) : None
+ storeBidProposal(input : BidProposalInput) : None
+ toSubmitBidCommand() : SubmitBidCommand
+ discard() : None
```

#### «entity» WinnerSelectionSession

| 항목 | 내용 |
|---|---|
| 배치 | client.domain |
| 생명주기 | Client Select Winner 처리 세션 동안만 존재 |
| 책임 | 마감 공고 선택, 낙찰 입찰서 선택 정보를 서버 요청 전까지 누적 |

```text
- sellerId : String
- selectedAuctionId : String
- selectedBidId : String

+ storeSeller(sellerId : String) : None
+ storeSelectedAuction(auctionId : String) : None
+ storeSelectedBid(bidId : String) : None
+ toSelectWinnerCommand() : SelectWinnerCommand
+ discard() : None
```

---

## 4. Server Object 설계

### 4.1 Server Coordinator

#### «coordinator» ZIPPTTransactionServer

| 항목 | 내용 |
|---|---|
| 배치 | server.service |
| 책임 | Client 요청의 단일 진입점, 요청 유형별 Transaction Manager 위임 |
| 협력 객체 | AuthenticationManager, RegisterAuctionManager, SubmitBidManager, SelectWinnerManager, SearchPropertyManager, ReservationManager, ReviewManager |

```text
+ handleRegisterAuction(command : RegisterAuctionCommand) : AuctionRegistrationResult
+ handleSubmitBid(command : SubmitBidCommand) : BidSubmissionResult
+ handleSelectWinner(command : SelectWinnerCommand) : WinnerSelectionResult
+ handleSearchProperty(command : SearchPropertyCommand) : PropertySearchResult
+ handleRequestVisit(command : RequestVisitCommand) : ReservationResult
+ handleRegisterReview(command : RegisterReviewCommand) : ReviewResult
```

> Coordinator는 요청 라우팅만 담당한다. 검증/변경/저장은 각 Transaction Manager와 Entity Object에 위임한다.

### 4.2 Business Logic Object

#### «business logic» AuthenticationManager

| 항목 | 내용 |
|---|---|
| 배치 | server.service |
| 책임 | Validate User 처리, 사용자 세션/권한 검증 |
| 접근 Entity | User |

```text
+ validateUser(userId : String, requiredRole : UserRole) : User
+ validateSeller(sellerId : String) : Seller
+ validateAgent(agentId : String) : Agent
+ validateBuyer(buyerId : String) : Buyer
```

#### «transaction manager» RegisterAuctionManager

| 항목 | 내용 |
|---|---|
| 배치 | server.service |
| 책임 | Register Auction UC의 서버 측 비즈니스 규칙 처리 |
| 접근 Entity | Seller, Property, Auction, AuctionCondition, WinnerSelectionCriteria, Agent, Notification |

```text
+ registerAuction(command : RegisterAuctionCommand) : AuctionRegistrationResult
+ validateNoActiveAuction(propertyId : String) : bool
+ validateAuctionCondition(condition : AuctionCondition) : bool
+ createAuction(command : RegisterAuctionCommand) : Auction
+ notifyQualifiedAgents(auction : Auction) : None
```

주요 협력:

| L2 단계 | 협력 객체 | 메시지 |
|---|---|---|
| 1 | AuthenticationManager | validateSeller(sellerId) |
| 3~5 | Property | getPropertyInfo() |
| 8 | AuctionCondition, WinnerSelectionCriteria | validate condition/criteria |
| 10~11 | Auction | open() |
| 12 | Notification | createNewAuctionNotification() |

#### «transaction manager» SubmitBidManager

| 항목 | 내용 |
|---|---|
| 배치 | server.service |
| 책임 | Submit Bid UC의 서버 측 비즈니스 규칙 처리 |
| 접근 Entity | Agent, AgentCredential, Auction, Bid, BidProposal, Notification |

```text
+ submitBid(command : SubmitBidCommand) : BidSubmissionResult
+ validateAuctionOpen(auctionId : String) : Auction
+ validateAgentCredential(agentId : String) : AgentCredential
+ validateBidProposal(proposal : BidProposal) : bool
+ createOrUpdateBid(command : SubmitBidCommand) : Bid
+ notifySellerOfNewBid(bid : Bid) : None
```

주요 협력:

| L2 단계 | 협력 객체 | 메시지 |
|---|---|---|
| 1 | AuthenticationManager | validateAgent(agentId) |
| 3~5 | Auction | getAuctionDetailWithCriteria() |
| 7 | BidProposal | validateCommissionRate(), validateRequiredFields() |
| 9 | Bid | submit(), resubmit() |
| 9 | Auction | incrementBidCount() |
| 11 | Notification | createNewBidNotification() |

#### «transaction manager» SelectWinnerManager

| 항목 | 내용 |
|---|---|
| 배치 | server.service |
| 책임 | Select Winner UC의 서버 측 비즈니스 규칙 처리, 단일 낙찰 보장 |
| 접근 Entity | Seller, Auction, Bid, BidProposal, Notification |

```text
+ selectWinner(command : SelectWinnerCommand) : WinnerSelectionResult
+ validateAuctionClosed(auctionId : String) : Auction
+ validateNoWinnerSelected(auction : Auction) : bool
+ sortBidsByCriteria(auction : Auction) : Collection<Bid>
+ markWinner(auction : Auction, selectedBidId : String) : None
+ notifyBidResults(auction : Auction) : None
+ postponeWinnerSelection(auctionId : String) : None
```

주요 협력:

| L2 단계 | 협력 객체 | 메시지 |
|---|---|---|
| 1 | AuthenticationManager | validateSeller(sellerId) |
| 3~5 | Auction, Bid | findClosedAuctions(), findBidsByAuction() |
| 6 | WinnerSelectionCriteria | score/sort bids |
| 9~11 | Bid, Auction | markWon(), markLost(), markWinnerSelected() |
| 12 | Notification | createBidResultNotification() |

### 4.3 Control Object

#### «control» AuctionQueryControl

| 항목 | 내용 |
|---|---|
| 배치 | server.control |
| 책임 | 경매 목록/상세 조회 흐름 조율 |
| 상태 의존성 | 없음 |

```text
+ getSellerProperties(sellerId : String) : Collection<Property>
+ getOpenAuctionsForAgent(agentId : String) : Collection<Auction>
+ getClosedAuctionsForSeller(sellerId : String) : Collection<Auction>
+ getAuctionDetail(auctionId : String) : AuctionDetail
+ getBidsForAuction(auctionId : String) : Collection<Bid>
```

#### «state dependent control» AuctionLifecycleControl

| 항목 | 내용 |
|---|---|
| 배치 | server.control |
| 책임 | AuctionStatus 전이의 유효성 관리 |
| 상태 | DRAFT, OPEN, CLOSED, PENDING_WINNER, WINNER_SELECTED, CANCELLED |

상태 전이:

| 현재 상태 | 이벤트 | 다음 상태 |
|---|---|---|
| DRAFT | openAuction | OPEN |
| OPEN | deadlineReached | CLOSED |
| CLOSED | postponeSelection | PENDING_WINNER |
| CLOSED/PENDING_WINNER | selectWinner | WINNER_SELECTED |
| DRAFT/OPEN | cancelAuction | CANCELLED |

```text
+ openAuction(auction : Auction) : None
+ closeAuction(auction : Auction) : None
+ postponeSelection(auction : Auction) : None
+ markWinnerSelected(auction : Auction, selectedBidId : String) : None
+ cancelAuction(auction : Auction) : None
```

---

## 5. Entity Object 설계

### 5.1 Server Persistent Entity Object

| Entity Object | 배치 | 주요 Operation |
|---|---|---|
| User | server.domain | isAuthenticated(), hasRole(role) |
| Buyer | server.domain | getPreferredCondition() |
| Seller | server.domain | ownsProperty(propertyId) |
| Agent | server.domain | isCredentialVerified(), updateReputation(score) |
| AgentCredential | server.domain | verify(), reject(), isVerified() |
| Property | server.domain | isOwnedBy(sellerId), markOnAuction(), getBasicInfo() |
| Auction | server.domain | open(), close(), postponeWinnerSelection(), markWinnerSelected(bidId), incrementBidCount(), hasActiveStatus(), isClosedForBidding() |
| AuctionCondition | server.domain | validateRequiredFields(), validateDeadlineRange() |
| WinnerSelectionCriteria | server.domain | validateWeights(), calculateBidScore(bidProposal) |
| Bid | server.domain | submit(), resubmit(proposal), markWon(), markLost(), cancel(), isResubmitAllowed() |
| BidProposal | server.domain | validateRequiredFields(), validateCommissionRate() |
| Reservation | server.domain | confirm(), markVisited(), markReviewed(), reject(), cancel() |
| Review | server.domain | validateRating(), attachToReservation(reservationId) |
| Notification | server.domain | markRead(), markFailed() |
| MarketData | server.domain | getTransactionPrice(), getTransactionDate() |
| NewsData | server.domain | getSummary(), getPublishedAt() |

### 5.2 Client Transient Entity Object

| Entity Object | 배치 | 주요 Operation |
|---|---|---|
| AuctionSession | client.domain | storeSelectedProperty(), storeAuctionCondition(), storeWinnerSelectionCriteria(), toRegisterAuctionCommand(), discard() |
| BidSession | client.domain | storeSelectedAuction(), storeBidProposal(), toSubmitBidCommand(), discard() |
| WinnerSelectionSession | client.domain | storeSelectedAuction(), storeSelectedBid(), toSelectWinnerCommand(), discard() |
| PropertySearchSession | client.domain | storeSearchCondition(), toSearchPropertyCommand(), discard() |
| ReservationSession | client.domain | storeVisitRequest(), toRequestVisitCommand(), discard() |
| ReviewSession | client.domain | storeReviewInput(), toRegisterReviewCommand(), discard() |

### 5.3 핵심 Entity Operation 상세

#### «entity» Auction

```text
+ open() : None
+ close() : None
+ postponeWinnerSelection() : None
+ markWinnerSelected(selectedBidId : String) : None
+ cancel() : None
+ incrementBidCount() : None
+ hasActiveStatus() : bool
+ isClosedForBidding(now : datetime) : bool
+ hasWinnerSelected() : bool
+ getSelectionCriteria() : WinnerSelectionCriteria
```

#### «entity» Bid

```text
+ submit(submittedAt : datetime) : None
+ resubmit(proposal : BidProposal, submittedAt : datetime) : None
+ markWon() : None
+ markLost() : None
+ cancel() : None
+ isResubmitAllowed() : bool
+ belongsToAuction(auctionId : String) : bool
+ submittedBy(agentId : String) : bool
```

#### «entity» WinnerSelectionCriteria

```text
+ validateWeights() : bool
+ calculateBidScore(proposal : BidProposal) : Decimal
+ isPriceFirst() : bool
+ isServiceFirst() : bool
```

#### «entity» Reservation

```text
+ confirm() : None
+ reject() : None
+ markVisited() : None
+ markReviewed() : None
+ cancel() : None
+ canWriteReview() : bool
```

---

## 6. 동시성 설계

### 6.1 동시성 위험 객체

| 객체 | 위험 | 보호 방법 |
|---|---|---|
| Auction | 동시에 입찰 수 증가 또는 낙찰 확정 시 상태 불일치 | auctionId 단위 lock |
| Bid | 동일 Agent의 중복 제출/재제출 경합 | auctionId + agentId 단위 lock |
| SelectWinnerManager | 여러 낙찰 확정 요청 동시 도착 시 중복 낙찰 | auctionId 단위 lock, 검증+변경 단일 임계 영역 |
| SubmitBidManager | 마감 검증 후 저장 전 마감되는 TOCTOU | auctionId 단위 lock 안에서 마감 검증+입찰 저장+입찰 수 증가 |
| Reservation | 동일 중개사 일정 중복 예약 | agentId + visitAt 단위 lock |
| Review | 동일 예약에 후기 중복 작성 | reservationId 단위 lock |

### 6.2 임계 영역 원칙

| UC | 임계 영역 범위 |
|---|---|
| Submit Bid | Auction OPEN/마감 검증 -> Agent 중복 입찰 검증 -> Bid 저장/갱신 -> Auction bidCount 증가 |
| Select Winner | Auction CLOSED 검증 -> winner 미선정 검증 -> 선택 Bid markWon -> 나머지 Bid markLost -> Auction markWinnerSelected |
| Request Visit | Agent 일정 중복 검증 -> Reservation 생성 |
| Register Review | Reservation VISITED 검증 -> Review 생성 -> Reservation REVIEWED 변경 |

검증과 변경을 분리된 lock으로 처리하지 않는다. 특히 Submit Bid와 Select Winner는 검증 결과가 변경 시점까지 유지되어야 하므로 같은 임계 영역 안에서 실행한다.

---

## 7. 코드 구조 및 패키지 매핑

### 7.1 패키지 구조

```text
com.zippt.l3l4
├── client
│   ├── ui
│   │   ├── BuyerInterface.java
│   │   ├── SellerInterface.java
│   │   └── AgentInterface.java
│   ├── control
│   │   ├── AuctionClientControl.java
│   │   └── AuctionClientState.java
│   └── domain
│       ├── AuctionSession.java
│       ├── BidSession.java
│       ├── WinnerSelectionSession.java
│       ├── PropertySearchSession.java
│       ├── ReservationSession.java
│       └── ReviewSession.java
├── server
│   ├── service
│   │   ├── ZIPPTTransactionServer.java
│   │   ├── AuthenticationManager.java
│   │   ├── RegisterAuctionManager.java
│   │   ├── SubmitBidManager.java
│   │   ├── SelectWinnerManager.java
│   │   ├── SearchPropertyManager.java
│   │   ├── ReservationManager.java
│   │   └── ReviewManager.java
│   ├── control
│   │   ├── AuctionQueryControl.java
│   │   └── AuctionLifecycleControl.java
│   ├── domain
│   │   ├── User.java
│   │   ├── Buyer.java
│   │   ├── Seller.java
│   │   ├── Agent.java
│   │   ├── AgentCredential.java
│   │   ├── Property.java
│   │   ├── PropertyCondition.java
│   │   ├── Auction.java
│   │   ├── AuctionCondition.java
│   │   ├── WinnerSelectionCriteria.java
│   │   ├── Bid.java
│   │   ├── BidProposal.java
│   │   ├── Reservation.java
│   │   ├── Review.java
│   │   ├── Notification.java
│   │   ├── MarketData.java
│   │   └── NewsData.java
│   └── integration
│       └── ExternalDataAPIInterface.java
└── common
    ├── command
    ├── result
    └── enums
```

### 7.2 Object 유형별 패키지 매핑

| Object 유형 | 스테레오타입 | 패키지 |
|---|---|---|
| 사용자 Interface Object | «user interface» | client.ui |
| 외부 시스템 Interface Object | «external system interface» | server.integration |
| Client Control Object | «state dependent control» | client.control |
| Client Transient Entity Object | «entity» | client.domain |
| Server Coordinator | «coordinator» | server.service |
| Server Business Logic Object | «business logic» / «transaction manager» | server.service |
| Server Control Object | «control» / «state dependent control» | server.control |
| Server Persistent Entity Object | «entity» | server.domain |
| Enumeration | «enumeration» | common.enums |

---

## 8. L2 대비 L4에서 기대되는 코드 변화

| 비교 항목 | L2 baseline 예상 | L3+L4 코드 기대 변화 |
|---|---|---|
| 클래스 수 | Submit Bid 중심의 제한된 클래스 | Interface/Control/Entity/BL 분리로 클래스 수 증가 |
| ECB 분리 | usecase/model/port 중심, Boundary/Control 구분 약함 | Interface Object, Control Object, Entity Object, Business Logic Object 명시 분리 |
| Client/Server 분리 | 단일 l2 패키지 아래 기능 중심 배치 | client/server 최상위 패키지 분리 |
| 상태 관리 | AuctionStatus 일부 존재 | AuctionClientState, AuctionLifecycleControl, Auction/Bid/Reservation 상태 전이 명시 |
| 동시성 처리 | 일부 예외/검증 수준 | auctionId, agentId, reservationId 단위 임계 영역 명시 |
| AI 임의 생성 위험 | Repository/Queue 등 임의 보강 가능 | 객체 유형, 책임, 패키지 위치가 명세되어 임의 구조 감소 |

---

## 9. L3와의 일치성 점검

| L3 요소 | L4 반영 위치 |
|---|---|
| Buyer/Seller/Agent external user | BuyerInterface, SellerInterface, AgentInterface |
| ExternalDataAPI external system | ExternalDataAPIInterface |
| User/Buyer/Seller/Agent entity | server.domain |
| Auction/Bid/WinnerSelectionCriteria entity | server.domain + Transaction Manager 협력 객체 |
| Bid as Association Class 성격 | Bid를 독립 Entity Object로 유지 |
| Reservation as Association Class 성격 | Reservation을 독립 Entity Object로 유지 |
| AuctionStatus/BidStatus/ReservationStatus enum | Lifecycle control 및 Entity Operation의 상태 기준 |

