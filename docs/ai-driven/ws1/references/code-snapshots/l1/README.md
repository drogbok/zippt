# ZIP-PT L1 Code — UC Diagram 기반

> project_proposal.md 및 prompt_1.md 에 정의된 ZIP-PT(집피티) AI 기반 부동산 매칭 플랫폼의
> Use Case Diagram 수준 명세만으로 생성된 Java 뼈대 코드.

## 패키지 구조

```
com.zippt.l1
├── actors/       Buyer, Seller, Agent, ExternalDataAPI  (4)
├── enums/        Role, ReservationStatus, AuctionStatus, PropertyType  (4)
├── model/        User, Property, Reservation, Auction, Bid, Review  (6)
├── usecase/
│   ├── ValidateUser  (<<abstract>>)
│   ├── account/     RegisterAccount, LoginSystem
│   ├── property/    RegisterProperty, SearchProperty, NLSearchProperty
│   ├── reservation/ RequestReservation, ConfirmReservation, CompleteVisit, WriteReview
│   └── auction/     CreateAuction, SubmitBid, SelectWinner
└── Main.java
```

## 상태 전이 (Enum)

- `ReservationStatus` : PENDING → CONFIRMED → VISITED → REVIEWED
- `AuctionStatus` : OPEN → ACTIVE → AWARDED → COMPLETED / CANCELLED

## 빌드

```bash
cd l1
find src/main/java -name '*.java' | xargs javac -d build
```

## 한계

- 모든 `execute()` 가 `// TODO` — Diagram 수준에서는 내부 로직 추론 불가.
- Enum 만으로 선언된 상태는 전이 로직이 없음 (Description 필요).
- project_proposal 4.1 의 동시성 제어 요구사항이 반영되지 않음.

L2 결과물과 비교해 명세 수준의 중요성을 확인하라.
