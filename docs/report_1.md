# **Problem Description 기반 AI 코드 생성**

**팀명:** 집피티

**시스템명:** 집피티 (ZIP-PT)

**작성일:** 2026-03-26

**그룹원:** 이원복, 양우영, 문희석, 원효섭, 한다현, 이혜원

# **Part A. AI 코드 생성**

## **Step 1. 프롬프트 작성 (WS0집피티pmt.md)**

*Problem Description을 읽고 팀이 파악한 내용을 바탕으로 AI에게 개발을 요청하는 프롬프트를 작성하세요.*

```
## Prompt #1 — L0: 초기 코드 생성 프롬프트 (Phase 1)

### [역할 지정]

너는 소프트웨어 엔지니어이다. **'집피티(ZIP-PT)'**라는 AI 기반 부동산 매칭 및 거래 지원 플랫폼을
**순수 Java(Pure Java)**로 구현하라. Spring, Jakarta EE 등 외부 프레임워크는 사용하지 않으며,
JDK 표준 라이브러리만 사용한다.

### [프로젝트 컨텍스트]

**집피티(ZIP-PT)**는 매수자(Buyer), 매도자(Seller), 중개사(Agent) 세 가지 액터가 상호작용하는
부동산 거래 플랫폼이다. 핵심 가치는 다음과 같다:

1. **매수자**는 매물을 검색하고, 원하는 매물에 대해 중개사를 통한 방문 예약을 할 수 있다.
2. **매도자**는 매물을 등록하고, 중개사들을 대상으로 수수료 역경매 입찰을 생성할 수 있다.
3. **중개사**는 매수자의 방문 예약을 관리하고, 매도자의 역경매에 입찰하여 거래를 성사시킨다.

### [구현 요구사항]

#### 1. 계정 시스템 (Account System)
- 매수자(Buyer), 매도자(Seller), 중개사(Agent) 세 가지 역할(Role)의 사용자 등록 및 로그인 기능을 구현하라.
- 각 역할별로 접근 가능한 기능이 다르며, 역할에 따라 메뉴가 분기되어야 한다.

#### 2. 매물 관리 (Property Management)
- 매도자가 매물 정보(주소, 면적, 가격, 매물 유형 등)를 등록/수정/삭제할 수 있어야 한다.
- 매수자는 조건(지역, 가격 범위, 면적 등)을 기반으로 매물을 검색할 수 있어야 한다.

#### 3. 방문 예약 시스템 (Reservation System)
- 매수자가 매물에 대해 중개사를 지정하여 방문 예약을 신청한다.
- 예약은 다음 상태 전이(State Transition)를 따른다:
  - **Pending(대기)** → **Confirmed(확정)** → **Visited(방문 완료)** → **Reviewed(후기 완료)**
- 중개사가 예약을 확정(Confirm) 또는 거절(Reject)할 수 있다.
- 중개사가 방문 완료(Visited) 처리를 할 수 있다.

#### 4. 후기 시스템 (Review System)
- 방문 완료(Visited) 상태의 예약 건에 한해, 매수자가 별점(1~5)과 텍스트 후기를 작성할 수 있다.
- 후기 작성 후 예약 상태는 Reviewed로 전이된다.
- 방문 완료 상태가 아닌 예약에 대한 후기 작성 시도는 거부한다.

#### 5. 역경매 입찰 시스템 (Reverse Auction System)
- 매도자가 매물에 대해 역경매를 생성하면, 중개사들이 수수료 조건을 입찰한다.
- 입찰 상태 전이: **입찰 대기(Open)** → **입찰 진행 중(Active)** → **낙찰(Awarded)** → **거래 완료(Completed) / 취소(Cancelled)**
- 매도자가 입찰 목록을 확인하고 최종 낙찰자를 선정할 수 있다.

### [제약 조건]

1. **예약 중복 방지:** 중개사의 기존 확정된 일정과 겹치는 시간대의 예약은 거부해야 한다.
2. **입찰 동시성 제어:** 여러 중개사가 동시에 낙찰을 시도할 경우, `synchronized` 블록 또는 동등한 메커니즘으로 순차 처리하여 중복 낙찰을 방지한다.
3. **후기 권한 제어:** Visited 상태가 아닌 예약 건에 대한 후기 작성은 거부한다.
4. **출력 방식:** 별도의 UI(GUI/Web) 없이, **콘솔(System.out) 기반 텍스트 메뉴**로 시스템 흐름과 처리 결과를 표시한다.
5. **데이터 저장:** 외부 DB 없이 **인메모리(In-Memory) 자료구조**(List, Map 등)로 데이터를 관리한다. 프로그램 종료 시 데이터는 소멸되어도 무방하다.
6. **Mock 데이터:** 시스템 시작 시 테스트를 위한 샘플 데이터(사용자 3~5명, 매물 5~10건 등)를 자동 생성하라.

### [코드 구조 가이드]

객체지향 설계 원칙을 따르며, 최소한 다음과 같은 패키지 구조를 권장한다:

src/
├── main/
│   └── java/
│       └── com/zippt/
│           ├── Main.java
│           ├── model/
│           ├── service/
│           ├── repository/
│           ├── enums/
│           └── ui/

### [출력 형식]

- 전체 소스 코드를 파일 단위로 생성하라.
- 각 클래스에는 역할과 책임을 설명하는 간결한 주석을 포함하라.
- `Main.java`를 실행하면 콘솔 메뉴가 출력되고, 사용자 입력에 따라 시스템이 동작해야 한다.
```

## **Step 2. 코드 생성 실행 (WS0집피티.java)**

*프롬프트를 통해 생성된 순수 Java 코드를 입력하세요 (UI 및 외부 프레임워크 제외).*

### 동작 관련 스펙

AI가 prompt_1.md를 기반으로 총 **26개 소스 파일**을 생성하였다. 코드는 단일 파일이 아닌 멀티 파일 프로젝트로 구성되며, 패키지 구조는 아래와 같다.

#### 전체 패키지 구조

```
src/main/java/com/zippt/
├── Main.java                              // 엔트리 포인트 (콘솔/GUI 분기)
├── data/
│   └── MockDataInitializer.java           // 테스트용 Mock 데이터 초기화 (사용자 7명, 매물 8건)
├── enums/
│   ├── Role.java                          // 역할 (BUYER, SELLER, AGENT)
│   ├── ReservationStatus.java             // 예약 상태 (PENDING → CONFIRMED → VISITED → REVIEWED + REJECTED)
│   ├── AuctionStatus.java                 // 경매 상태 (OPEN → ACTIVE → AWARDED → COMPLETED/CANCELLED)
│   └── PropertyType.java                  // 매물 유형 (APARTMENT, VILLA, OFFICETEL, HOUSE, COMMERCIAL)
├── model/
│   ├── User.java                          // 사용자 (id, username, password, name, phone, role, region)
│   ├── Property.java                      // 매물 (sellerId, address, district, areaSqm, priceInWan, type 등)
│   ├── Reservation.java                   // 예약 (buyerId, agentId, propertyId, status, dateTime)
│   ├── Review.java                        // 후기 (reservationId, buyerId, agentId, propertyId, rating, content)
│   ├── Auction.java                       // 역경매 (propertyId, sellerId, status, requirements, awardedBidId)
│   └── Bid.java                           // 입찰 (auctionId, agentId, commissionRate, conditions)
├── repository/
│   ├── UserRepository.java                // ConcurrentHashMap + AtomicLong 기반 인메모리 저장소
│   ├── PropertyRepository.java
│   ├── ReservationRepository.java
│   ├── ReviewRepository.java
│   └── AuctionRepository.java            // 경매/입찰 저장소 분리 (auctionStore + bidStore)
├── service/
│   ├── UserService.java                   // 회원가입(중복검사), 로그인(평문 비교)
│   ├── PropertyService.java               // 매물 CRUD + 조건 검색(Stream 필터)
│   ├── ReservationService.java            // 예약 생성/확정/거절/방문완료 + 일정 충돌 검사(60분 블록)
│   ├── ReviewService.java                 // 후기 작성(VISITED 검증, 예약당 1회) + 상태 REVIEWED 갱신
│   └── AuctionService.java               // 역경매 생성/입찰/낙찰(synchronized)/완료/취소
└── ui/
    ├── ConsoleUI.java                     // 메인 콘솔 UI (회원가입/로그인 → 역할별 메뉴 분기)
    ├── BuyerMenu.java                     // 매수자 메 (검색, 상세조회, 예약, 후기)
    ├── SellerMenu.java                    // 매도자 메뉴 (매물 CRUD, 역경매 관리, 낙찰)
    └── AgentMenu.java                     // 중개사 메뉴 (예약 관리, 입찰, 내 입찰 조회)
```

#### 핵심 비즈니스 로직 요약


| 도메인        | 주요 기능          | 핵심 구현                                                                                  |
| ---------- | -------------- | -------------------------------------------------------------------------------------- |
| **계정 시스템** | 회원가입, 로그인      | `UserService`: 아이디 중복 검사, 역할별 메뉴 분기, 중개사 담당지역 설정                                       |
| **매물 관리**  | 등록/수정/삭제/검색    | `PropertyService`: 소유권(sellerId) 검증, Stream 기반 다중 조건 필터링 (지역·가격·면적·유형)                 |
| **방문 예약**  | 생성/확정/거절/방문완료  | `ReservationService`: 상태 전이 검증, `hasScheduleConflict()` — CONFIRMED 예약 대상 60분 블록 충돌 검사 |
| **후기 시스템** | 후기 작성          | `ReviewService`: VISITED 상태 검증, 매수자 본인 검증, 예약당 1회 제한, 저장 후 REVIEWED 상태 전이              |
| **역경매**    | 생성/입찰/낙찰/완료/취소 | `AuctionService`: 경매당 중개사 1회 입찰, 첫 입찰 시 OPEN→ACTIVE, `synchronized(awardLock)` 낙찰 직렬화  |


#### 제약 조건 구현


| 제약 조건         | 구현 방식                                                                                                         |
| ------------- | ------------------------------------------------------------------------------------------------------------- |
| **예약 중복 방지**  | `ReservationService.hasScheduleConflict()`: 동일 중개사의 CONFIRMED 예약과 새 예약 시각 차이가 60분 미만이면 거부                     |
| **입찰 동시성 제어** | `AuctionService.awardBid()`: `synchronized(awardLock)` 블록으로 낙찰 처리 직렬화, 이중 낙찰 방지                               |
| **후기 권한 제어**  | `ReviewService.create()`: 예약 상태가 VISITED가 아니면 `IllegalStateException` 발생, `existsByReservationId()`로 중복 후기 방지 |
| **인메모리 저장**   | `ConcurrentHashMap<Long, T>` + `AtomicLong` ID 자동 발급, 프로세스 종료 시 데이터 소멸                                        |
| **Mock 데이터**  | `MockDataInitializer.init()`: 사용자 7명(매수2, 매도2, 중개3), 매물 8건 자동 생성                                              |


# **Part B. 생성 결과 관찰**

## **B-1. 코드 구조 체크리스트**

생성된 코드를 보고 아래 항목을 체크하세요.


| **관찰 항목**           | **있음** | **없음** | **비고 (구현 내용 기술)**                                                                                                                                                                                                                                                                                            |
| ------------------- | ------ | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **정상 흐름 주요 기능 구현**  | ✅      |        | 계정(가입/로그인), 매물(CRUD+검색), 예약(생성→확정→방문완료), 후기(작성), 역경매(생성→입찰→낙찰→완료) — 5개 도메인의 정상 흐름이 모두 구현됨. `UserService.register/login`, `PropertyService.register/update/delete/search`, `ReservationService.create/confirm/markVisited`, `ReviewService.create`, `AuctionService.create/placeBid/awardBid/complete` 메서드 존재 |
| **대안 흐름(예외 처리) 구현** | ✅      |        | 각 Service 메서드에서 `IllegalArgumentException`(존재하지 않는 대상, 권한 위반)과 `IllegalStateException`(상태 전이 위반)을 발생시킴. 콘솔 UI에서 `try-catch`로 처리하여 사용자에게 오류 메시지 출력. 예: 미존재 예약 접근, 본인 매물이 아닌 수정/삭제 시도, VISITED가 아닌 예약에 후기 작성 시도 등                                                                                              |
| **Cancel 처리 구현**    | ✅      |        | 예약 거절(`ReservationService.reject` — PENDING→REJECTED), 역경매 취소(`AuctionService.cancel` — CANCELLED 상태 전이), 이미 완료된 경매 취소 거부 등 구현됨                                                                                                                                                                              |
| **Operator 기능 구현**  |        | ❌      | 시스템 관리자(Operator/Admin) 역할은 구현되지 않음. 3개 역할(Buyer, Seller, Agent)만 존재하며, 전체 사용자·데이터를 관리하는 운영자 기능 없음                                                                                                                                                                                                           |
| **주요 기능별 메서드 분리**   | ✅      |        | 서비스 계층에서 기능별 메서드가 명확히 분리됨. 예: `ReservationService`는 `create`, `confirm`, `reject`, `markVisited`, `markReviewed`, `hasScheduleConflict` 등으로 분리. 콘솔 UI도 `BuyerMenu`, `SellerMenu`, `AgentMenu`로 역할별 분리                                                                                                        |




## **B-2. AI가 임의로 결정한 부분**

생성된 코드에서 Problem Description에 근거 없이 AI가 스스로 결정한 것으로 보이는 항목을 작성하세요.


| **항목**                | **AI의 결정 내용**                                                                         | **PD에 근거가 있는가?**                                                                                             |
| --------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| REJECTED 상태 추가        | 예약 상태 전이에 PD에는 없는 `REJECTED(거절)` 상태를 추가함. PD에는 Pending→Confirmed→Visited→Reviewed만 명시 | 부분적 근거 있음 — PD에 "중개사가 예약을 확정 또는 거절"이라 기술되어 있으나, 명시적 상태값으로는 정의되지 않음. AI가 거절 시 전이할 상태가 필요하다고 판단하여 REJECTED를 추가 |
| 일정 충돌 블록 시간 60분       | 예약 중복 방지 시 충돌 판단 기준을 **60분(SCHEDULE_BLOCK_MINUTES)**으로 설정                             | 근거 없음 — PD에는 "겹치는 시간대의 예약은 거부"라고만 기술되어 있고, 구체적인 시간 간격(60분)은 AI가 임의 결정                                        |
| PropertyType 매물 유형 5종 | 아파트, 빌라, 오피스텔, 단독주택, 상가 5가지 유형을 enum으로 정의                                             | 근거 없음 — PD에는 "매물 유형 등"이라고만 명시되었고, 구체적 유형 종류는 AI가 부동산 도메인 지식으로 결정                                             |
| 중개사 담당지역(region) 필드   | User 모델에 중개사 전용 `region` 필드를 추가하여 담당 지역 관리                                            | 근거 없음 — PD에는 중개사의 담당 지역 개념이 없으며, AI가 부동산 도메인의 관행을 반영하여 추가                                                    |
| 경매당 중개사 1회 입찰 제한      | 동일 경매에 같은 중개사가 중복 입찰하지 못하도록 `alreadyBid` 검사 추가                                        | 근거 없음 — PD에는 입찰 횟수에 대한 제한이 명시되지 않았으나, AI가 역경매의 공정성을 위해 추가                                                    |
| 첫 입찰 시 OPEN→ACTIVE 전이 | 경매에 첫 번째 입찰이 들어오면 자동으로 상태를 OPEN에서 ACTIVE로 변경                                          | 부분적 근거 있음 — PD에 "입찰 대기(Open) → 입찰 진행 중(Active)" 전이는 명시되어 있으나, 전이 트리거(첫 입찰)는 AI가 결정                           |
| Mock 데이터 구체적 내용       | 사용자 7명(매수2, 매도2, 중개3), 매물 8건의 구체적 이름·주소·가격·설명 등                                       | 부분적 근거 있음 — PD에 "사용자 3~~5명, 매물 5~~10건"이라 범위만 제시, 구체적 데이터(강남구 역삼동 등)는 AI가 생성                                  |
| 가격 포맷 (억/만원)          | `Property.formatPrice()`에서 10000만원 이상일 때 "X억 Y만원" 형식으로 변환                             | 근거 없음 — PD에는 가격 표시 형식에 대한 요구사항이 없으며, AI가 한국 부동산 관행을 반영                                                       |
| 비밀번호 평문 저장            | 암호화 없이 비밀번호를 String 필드에 평문으로 저장하고, 로그인 시 `equals()`로 비교                               | 근거 없음 — PD에 보안 요구사항이 없어 AI가 학습/프로토타입 수준으로 판단하여 단순 구현                                                         |
| ConcurrentHashMap 사용  | 인메모리 저장소 구현에 일반 HashMap이 아닌 `ConcurrentHashMap`을 선택                                   | 부분적 근거 있음 — PD에 "인메모리 자료구조(List, Map 등)"로 명시되어 있고, 동시성 제어 요구사항에 대응하여 AI가 스레드 안전한 구현체를 선택                     |


# **Part C. 코드 관찰 후 느낀 점**

## **Q1. AI가 PD 근거 없이 스스로 결정한 항목 중 가장 의외였던 것은 무엇입니까?**

**중개사 담당지역(region) 필드의 추가**가 가장 의외였다. PD에는 중개사의 담당 지역이라는 개념 자체가 없음에도 불구하고, AI가 부동산 업계의 실무 관행(중개사가 특정 지역을 전담)을 반영하여 User 모델에 `region` 필드를 자체적으로 추가하였다. 이는 단순히 명세를 구현하는 것을 넘어, AI가 도메인 지식을 활용해 설계를 보강한 사례이다.

또한 **일정 충돌 블록 시간을 60분으로 결정한 것**도 주목할 만하다. PD에는 "겹치는 시간대"라고만 되어 있어 동일 시각만 검사할 수도 있었으나, AI가 부동산 방문의 현실적 소요 시간을 고려하여 60분이라는 구체적 수치를 설정하였다. 이는 실제 서비스에서는 요구사항 명세서에 반드시 포함해야 할 비즈니스 룰이지만, PD만으로는 결정할 수 없는 항목이다.

## **Q2. AI가 코드를 생성할 때 가장 부족했던 정보는 무엇이라고 생각합니까?**

1. **Use Case Description(유스케이스 상세 기술서)의 부재:** PD는 기능의 개요와 상태 전이만 기술하고 있어, 각 기능의 구체적 실행 단계(Step-by-step scenario)가 없다. 예를 들어, "매수자가 매물 검색 후 예약"이라는 흐름에서 중개사를 어떤 기준으로 선택하는지, 예약 가능한 시간대를 어떻게 제시하는지 등의 상세 흐름이 명세되지 않아 AI가 스스로 결정해야 했다.
2. **비즈니스 룰의 구체적 수치:** 일정 충돌의 시간 블록(60분), 별점 범위(1~5), 입찰 횟수 제한 등 구체적인 수치나 제한 조건이 PD에 부족하여, AI가 "합리적으로 보이는" 값을 임의로 설정하였다. 실제 프로젝트에서는 이러한 수치가 명확히 정의되어야 한다.
3. **대안 흐름(Alternative Flow) 및 예외 시나리오:** PD에 "거절"이라는 단어는 있으나, 거절 후의 상태(REJECTED)나 처리 흐름은 명시되지 않았다. 마찬가지로, 매물에 이미 경매가 진행 중일 때 중복 경매 생성이 가능한지, 예약이 거절되었을 때 재예약이 가능한지 등의 대안 흐름 정보가 부재하여 AI가 자체적으로 판단하였다.
4. **Operator/Admin 역할의 부재:** 시스템 전체를 관리하는 운영자 역할에 대한 정의가 없어, 사용자·매물·경매 데이터의 전체 관리 기능이 구현되지 않았다. 실제 서비스에서는 필수적인 기능이지만 PD에 없어 생략되었다.

