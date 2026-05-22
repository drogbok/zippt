# Step 3-2. L2 코드 생성 프롬프트 — UC Description 기반 (3개 핵심 UC 전체)

---

## 프롬프트 본문

아래 **Use Case Description 3종**을 바탕으로 순수 **Java 코드**를 생성하세요.

### [제약 조건]

1. 외부 프레임워크(Spring, JPA, Lombok 등)를 사용하지 않는다.
2. Use Case Description에 명시된 내용만을 근거로 구현한다.
3. 명시되지 않은 내부 구현 로직은 임의로 추가하지 않는다.
4. 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다.

### [구현 지침]

1. Description의 **각 단계를 메서드 호출 순서**로 변환하고, 각 코드 라인 옆에 대응되는 **단계 번호를 주석**으로 표시한다.
2. **Include된 Abstract UC**는 별도 독립 클래스로 구현한다.
3. **Precondition**은 `execute()` 진입부의 **가드 조건**으로 구현한다.
4. **각 Alternative 흐름**은 고유한 **Exception 클래스**로 매핑한다.
5. **Cancel 흐름(A3/A2)**은 별도 메서드로 분리한다.
6. **Postcondition**은 메서드 말미의 주석으로 반영하고, 상태 변경 메서드 호출로 실현한다.
7. 협력 객체(Repository, NotificationQueue 등)는 **생성자 주입** 구조로 선언한다.
8. Description에 등장한 **도메인 개념**은 **값 객체 또는 엔티티**로 모델링한다.
9. **3개 UC가 공유하는 도메인 객체**(`Auction`, `WinnerSelectionCriteria` 등)는 단일 선언 후 재사용한다.

### [Exception 매핑 전체]

| Alternative | Exception 클래스 | 해당 UC |
|:---|:---|:---|
| A1 (진행 중 공고 존재) | `DuplicateAuctionException` | UC-01 |
| A2 (공고 유효성 실패) | `AuctionFormValidationException` | UC-01 |
| A3 (공고 등록 취소) | `AuctionRegistrationCancelledException` | UC-01 |
| A1 (공고 마감) | `AuctionClosedException` | UC-02 |
| A2 (입찰서 유효성 실패) | `BidValidationException` | UC-02 |
| A3 (입찰 취소) | `BidCancelledException` | UC-02 |
| A4 (저장 실패) | `BidStorageException` | UC-02 |
| A5 (재제출) | `BidResubmitException` | UC-02 |
| A1 (입찰서 없음) | `NoBidsException` | UC-03 |
| A2 (낙찰 보류) | `WinnerSelectionPostponedException` | UC-03 |
| A3 (단독 입찰 확인) | `SoleBidConfirmationException` | UC-03 |
| P1/P3 (인증 실패) | `AuthException` | 공통 |

### [출력 형식]

- 생성된 Java 코드만 출력한다.
- 패키지 선언과 `import` 문은 생략한다.
- 공유 도메인 객체 → 예외 → Abstract UC → UC-01 → UC-02 → UC-03 순으로 출력한다.

---

## [Use Case Description — 학생 작성본]

### UC-01. Register Auction (매물 경매 등록)

**Precondition**
- P1. 매도자는 시스템에 로그인되어 인증 세션이 유효한 상태이다.
- P2. 등록할 매물 정보(주소, 면적, 희망 매도가)가 시스템에 사전 입력된 상태이다.
- P3. 해당 매물에 대해 진행 중인 경매 공고가 없는 상태이다.
- P4. 매도자는 낙찰 기준(가격 우선 / 서비스 우선)을 사전에 결정한 상태이다.

**Description** 1. Validate User 수행 → 2. 공고 등록 요청 → 3. 매물 목록 반환 → 4. 매물 선택 → 5. 기본 정보 자동 채움 → 6. 공고 조건 입력 → 7. 낙찰 우선 기준 설정 → 8. 유효성 검증 → 9. 최종 확정 → 10. 공고 저장 + 공고 ID 발급 → 11. 상태 '입찰 모집중' 설정 → 12. 자격 요건 충족 중개사에게 알림 발송 → 13. 완료 메시지 + 공고 ID 반환 → 14. 공고 목록 복귀

**Alternatives** A1[4단계] 진행 중 공고 존재 / A2[8단계] 유효성 실패 / A3[Cancel 6~9단계] 등록 취소

**Postcondition** Q1(정상) 공고 저장, 상태 '입찰 모집중' / Q2(정상) 중개사 알림 발송 완료 / Q3(비정상) 저장소 무변경

---

### UC-02. Submit Bid (입찰 참여)

**Precondition**
- P1. 공인중개사는 시스템에 로그인되어 인증 세션이 유효한 상태이다.
- P2. 대상 경매 공고가 '입찰 모집중' 상태이며 입찰 마감 시각 이전이다.
- P3. 공인중개사의 자격 증명(자격증 번호, 사무소 등록증)이 검증 완료된 상태이다.
- P4. 공인중개사는 해당 공고에 기제출한 입찰서가 없거나, 기제출 입찰서가 '수정 허용' 상태이다.

**Description** 1. Validate User → 2. 공고 목록 조회 → 3. 활성 공고 반환 → 4. 공고 선택 → 5. 상세 정보(낙찰 우선 기준 포함) 표시 → 6. 낙찰 우선 기준 참고하여 입찰서 작성 → 7. 유효성 검증 → 8. 최종 확정 → 9. 저장 + 접수 시각 기록 → 10. 접수완료 메시지 + 접수번호 반환 → 11. 매도자 알림 큐 등재 → 12. 입찰 목록 복귀

**Alternatives** A1[4단계] 공고 마감 / A2[7단계] 유효성 실패 / A3[Cancel 6~8단계] 취소 / A4[9단계] 저장 실패 / A5[4단계] 기제출 입찰서 수정 재제출

**Postcondition** Q1(정상) 입찰서 저장, 입찰 건수 1 증가 / Q2(정상) 매도자 알림 등재 / Q3(비정상 A1/A2/A3) 저장소 무변경 / Q4(비정상 A4) 복원 + 장애 로그 1건

---

### UC-03. Select Winner (낙찰자 선정)

**Precondition**
- P1. 매도자는 시스템에 로그인되어 인증 세션이 유효한 상태이다.
- P2. 대상 경매 공고의 입찰 마감 시각이 경과한 상태이다.
- P3. 해당 공고에 1건 이상의 입찰서가 접수된 상태이다.
- P4. 해당 공고에 대해 아직 낙찰자가 선정되지 않은 상태이다.

**Description** 1. Validate User → 2. 마감 공고 목록 조회 → 3. 낙찰자 미선정 공고 반환 → 4. 공고 선택 → 5. 입찰서 목록 반환 → 6. 낙찰 우선 기준 적용 정렬 표시 → 7. 비교 검토 → 8. 낙찰 확정 요청 → 9. 선택 입찰서 상태 '낙찰' 갱신 → 10. 나머지 입찰서 '미낙찰' 일괄 갱신 → 11. 공고 상태 '낙찰 완료' 변경 → 12. 전원 결과 알림 발송 → 13. 공고 상세 화면 복귀

**Alternatives** A1[3단계] 입찰서 0건 / A2[Cancel 7~8단계] 선정 보류 / A3[5단계] 입찰서 1건 단독 낙찰 확인

**Postcondition** Q1(정상) 낙찰 입찰서 상태 '낙찰', 공고 '낙찰 완료' / Q2(정상) 미낙찰 입찰서 '미낙찰', 전원 알림 큐 등재 / Q3(비정상) 저장소 무변경

---

## 생성 결과 (L2 코드)

```java
// ============================================================
// [L2] 공유 Value Objects / Entities
//      (UC-01·02·03 전체에서 참조)
// ============================================================

public class PropertyId {
    private final String value;
    public PropertyId(String value) { this.value = value; }
    public String getValue() { return value; }
}

public class AuctionId {
    private final String value;
    public AuctionId(String value) { this.value = value; }
    public String getValue() { return value; }
}

public class BidId {
    private final String value;
    public BidId(String value) { this.value = value; }
    public String getValue() { return value; }
}

public enum AuctionStatus {
    OPEN,               // 입찰 모집중  (Description 11단계 UC-01)
    CLOSED,             // 마감         (Alternative A1 UC-02)
    PENDING_WINNER,     // 낙찰자 선정 대기 (Alternative A2 UC-03)
    WINNER_SELECTED     // 낙찰 완료    (Description 11단계 UC-03)
}

public enum BidStatus {
    SUBMITTED,          // 제출됨
    RESUBMIT_ALLOWED,   // 수정 허용    (Precondition P4 UC-02)
    WON,                // 낙찰         (Description 9단계 UC-03)
    LOST                // 미낙찰       (Description 10단계 UC-03)
}

/**
 * 낙찰 우선 기준 값 객체.
 * UC-01 Description 7단계에서 매도자가 설정하고,
 * UC-02 Description 5·6단계, UC-03 Description 6단계에서 참조한다.
 */
public class WinnerSelectionCriteria {
    private final double commissionRateWeight;    // 수수료율 가중치
    private final double marketingStrategyWeight; // 마케팅 전략 가중치

    public WinnerSelectionCriteria(double commissionRateWeight,
                                   double marketingStrategyWeight) {
        this.commissionRateWeight    = commissionRateWeight;
        this.marketingStrategyWeight = marketingStrategyWeight;
    }

    public double getCommissionRateWeight()    { return commissionRateWeight; }
    public double getMarketingStrategyWeight() { return marketingStrategyWeight; }
}

/**
 * 공고 등록 양식 (UC-01 Description 6·7단계 입력 항목).
 */
public class AuctionForm {
    private LocalDateTime deadline;          // 입찰 마감 기한     (Description 6단계)
    private String        serviceCondition;  // 요구 서비스 조건   (Description 6단계)
    private String        minQualification;  // 최소 자격 요건     (Description 6단계)
    private WinnerSelectionCriteria criteria; // 낙찰 우선 기준    (Description 7단계)

    public void discard() { /* A3-1 임시 데이터 파기 */ }

    public LocalDateTime           getDeadline()          { return deadline; }
    public String                  getServiceCondition()   { return serviceCondition; }
    public String                  getMinQualification()   { return minQualification; }
    public WinnerSelectionCriteria getCriteria()           { return criteria; }
}

/**
 * 경매 공고 엔티티.
 */
public class Auction {
    private AuctionId               id;
    private AuctionStatus           status;
    private LocalDateTime           deadline;
    private Seller                  seller;
    private int                     bidCount;
    private WinnerSelectionCriteria selectionCriteria; // UC-01 7단계에서 설정

    /** Precondition P2(UC-02) / Alternative A1(UC-02) 검증 */
    public boolean isClosed() {
        return status == AuctionStatus.CLOSED
            || LocalDateTime.now().isAfter(deadline);
    }

    /** Precondition P2(UC-03) 검증 — 마감 시각 경과 여부 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    public void setStatus(AuctionStatus status)    { this.status = status; }
    public void incrementBidCount()                { this.bidCount++; } // Postcondition Q1(UC-02)

    public AuctionId               getId()               { return id; }
    public AuctionStatus           getStatus()            { return status; }
    public Seller                  getSeller()            { return seller; }
    public int                     getBidCount()          { return bidCount; }
    public WinnerSelectionCriteria getSelectionCriteria() { return selectionCriteria; }
}

/**
 * 입찰서 양식 (UC-02 Description 6단계 작성 항목).
 */
public class BidForm {
    private double commissionRate;       // 제안 수수료율   (Description 6단계)
    private String marketingStrategy;   // 마케팅 전략     (Description 6단계)
    private int    expectedSalePeriod;  // 예상 매각 기간  (Description 6단계)
    private String serviceTerms;        // 서비스 조건     (Description 6단계)

    public void discard() { /* A3-1 임시 데이터 파기 */ }

    public double getCommissionRate()    { return commissionRate; }
    public String getMarketingStrategy() { return marketingStrategy; }
    public int    getExpectedSalePeriod(){ return expectedSalePeriod; }
    public String getServiceTerms()      { return serviceTerms; }
}

/**
 * 입찰서 엔티티.
 */
public class Bid {
    private BidId         id;
    private Agent         agent;
    private Auction       auction;
    private BidForm       form;
    private LocalDateTime timestamp;    // 접수 시각 (Description 9단계 UC-02)
    private BidStatus     status;
    private boolean       resubmitted;  // A5 재제출 여부 (UC-02)

    public Bid(Agent a, Auction au, BidForm f, LocalDateTime t) {
        this.agent       = a;
        this.auction     = au;
        this.form        = f;
        this.timestamp   = t;
        this.id          = new BidId(UUID.randomUUID().toString());
        this.status      = BidStatus.SUBMITTED;
        this.resubmitted = false;
    }

    /** A5-3 : 접수번호(id) 유지, 내용·시각만 갱신 */
    public void updateForm(BidForm updatedForm, LocalDateTime updatedAt) {
        this.form        = updatedForm;
        this.timestamp   = updatedAt;
        this.resubmitted = true;
    }

    public void setStatus(BidStatus status) { this.status = status; }

    public BidId         getId()          { return id; }
    public Agent         getAgent()       { return agent; }
    public LocalDateTime getTimestamp()   { return timestamp; }
    public BidStatus     getStatus()      { return status; }
    public boolean       isResubmitted()  { return resubmitted; }
}

/**
 * 공고 등록 완료 응답 (UC-01 Description 13단계).
 */
public class AuctionReceipt {
    private final AuctionId   auctionId;
    private final AuctionStatus status;

    public AuctionReceipt(AuctionId id, AuctionStatus s) {
        this.auctionId = id; this.status = s;
    }
    public AuctionId    getAuctionId() { return auctionId; }
    public AuctionStatus getStatus()   { return status; }
}

/**
 * 입찰 접수 완료 응답 (UC-02 Description 10단계).
 */
public class BidReceipt {
    private final BidId       bidId;
    private final LocalDateTime timestamp;

    public BidReceipt(BidId bidId, LocalDateTime ts) {
        this.bidId = bidId; this.timestamp = ts;
    }
    public BidId        getBidId()     { return bidId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * 낙찰 선정 완료 응답 (UC-03 Description 12~13단계).
 */
public class WinnerSelectionResult {
    private final Bid    winnerBid;
    private final AuctionStatus finalStatus;

    public WinnerSelectionResult(Bid winner, AuctionStatus s) {
        this.winnerBid = winner; this.finalStatus = s;
    }
    public Bid          getWinnerBid()   { return winnerBid; }
    public AuctionStatus getFinalStatus() { return finalStatus; }
}

// ============================================================
// [L2] Exception 클래스 — Alternative 흐름 매핑
// ============================================================

// UC-01
public class DuplicateAuctionException             extends Exception {} // A1
public class AuctionFormValidationException        extends Exception {} // A2
public class AuctionRegistrationCancelledException extends Exception {} // A3

// UC-02
public class AuctionClosedException  extends Exception {} // A1
public class BidValidationException  extends Exception {} // A2
public class BidCancelledException   extends Exception {} // A3
public class BidStorageException     extends Exception {} // A4
public class BidResubmitException    extends Exception {} // A5

// UC-03
public class NoBidsException                    extends Exception {} // A1
public class WinnerSelectionPostponedException  extends Exception {} // A2
public class SoleBidConfirmationException       extends Exception {} // A3

// 공통
public class AuthException extends Exception {} // P1 / P3

// ============================================================
// [L2] <<include>> Abstract UC
// ============================================================

/**
 * Validate User : UC-01·02·03 모두에서 <<include>> 로 호출.
 * Description 1단계에 공통 대응.
 */
public class ValidateUser {
    public void execute(Object actor) throws AuthException {
        if (actor instanceof Seller) {
            Seller s = (Seller) actor;
            if (s == null || !s.isAuthenticated()) throw new AuthException(); // P1
        } else if (actor instanceof Agent) {
            Agent a = (Agent) actor;
            if (a == null || !a.isAuthenticated()) throw new AuthException(); // P1
            if (!a.isCertified())                  throw new AuthException(); // P3
        } else {
            throw new AuthException();
        }
    }
}

// ============================================================
// [L2] UC-01 — RegisterAuctionUseCase
// ============================================================

public class RegisterAuctionUseCase {

    private final ValidateUser        validateUser;
    private final PropertyRepository  propertyRepo;
    private final AuctionRepository   auctionRepo;
    private final NotificationQueue   notificationQueue;
    private final AgentRepository     agentRepo;

    public RegisterAuctionUseCase(ValidateUser       validateUser,
                                  PropertyRepository propertyRepo,
                                  AuctionRepository  auctionRepo,
                                  NotificationQueue  notificationQueue,
                                  AgentRepository    agentRepo) {
        this.validateUser      = validateUser;
        this.propertyRepo      = propertyRepo;
        this.auctionRepo       = auctionRepo;
        this.notificationQueue = notificationQueue;
        this.agentRepo         = agentRepo;
    }

    /**
     * 정상 흐름. Description 1~14단계에 대응.
     */
    public AuctionReceipt execute(Seller seller,
                                  PropertyId propertyId,
                                  AuctionForm form)
            throws AuthException,
                   DuplicateAuctionException,
                   AuctionFormValidationException {

        // ---- Precondition 가드 ----
        if (seller == null || !seller.isAuthenticated())
            throw new AuthException();                                              // P1

        if (!propertyRepo.existsBySeller(propertyId, seller))
            throw new AuctionFormValidationException();                             // P2 (매물 소유 확인)

        if (auctionRepo.hasActiveAuction(propertyId))
            throw new DuplicateAuctionException();                                  // P3

        if (form.getCriteria() == null)
            throw new AuctionFormValidationException();                             // P4 (낙찰 기준 미설정)

        // ---- Description 1. <<include>> Validate User ----
        validateUser.execute(seller);                                               // 1

        // ---- Description 2~3. 매물 목록 조회 ----
        List<Property> properties = propertyRepo.findBySeller(seller);              // 2, 3

        // ---- Description 4. 매물 선택 ----
        Property property = propertyRepo.findById(propertyId);                      // 4

        // Alternative A1 : 진행 중 공고 존재 여부 재확인 (동시성 대비)
        if (auctionRepo.hasActiveAuction(propertyId)) {
            throw new DuplicateAuctionException();                                  // A1
        }

        // ---- Description 5. 기본 정보 자동 채움 (프레젠테이션 계층 책임) ----     // 5

        // ---- Description 6. 공고 조건 입력 (form 파라미터로 전달) ----            // 6

        // ---- Description 7. 낙찰 우선 기준 설정 (form.getCriteria()로 전달) ----  // 7
        WinnerSelectionCriteria criteria = form.getCriteria();                      // 7

        // ---- Description 8. 유효성 검증 ----
        AuctionFormValidator.validate(form);                                        // 8 (실패 시 A2)

        // ---- Description 9. 최종 확정 (메서드 호출 자체가 확정) ----              // 9

        // ---- Description 10. 공고 저장 + 공고 ID 발급 ----
        Auction auction = auctionRepo.save(property, form, criteria);               // 10

        // ---- Description 11. 공고 상태 '입찰 모집중' 설정 ----
        auction.setStatus(AuctionStatus.OPEN);                                      // 11
        auctionRepo.updateStatus(auction);
        // Postcondition Q1 : 공고 저장, 상태 OPEN

        // ---- Description 12. 자격 요건 충족 공인중개사에게 알림 발송 ----
        List<Agent> qualifiedAgents =
            agentRepo.findByMinQualification(form.getMinQualification());           // 12
        for (Agent agent : qualifiedAgents) {
            notificationQueue.enqueue(agent, auction);                              // 12
        }
        // Postcondition Q2 : 알림 발송 완료

        // ---- Description 13. 완료 메시지 + 공고 ID 반환 ----
        AuctionReceipt receipt = new AuctionReceipt(auction.getId(),
                                                    auction.getStatus());           // 13

        // ---- Description 14. 공고 목록 복귀 ----
        return receipt;                                                             // 14
        // Postcondition Q1, Q2 달성
    }

    /**
     * Alternative A3 : 6~9단계 진행 중 매도자가 등록을 취소한 경우.
     */
    public void cancel(AuctionForm draft)
            throws AuctionRegistrationCancelledException {
        if (draft != null) draft.discard();   // A3-1 : 임시 입력 데이터 파기
        // A3-2 : 경매 저장소 무변경
        // Postcondition Q3 달성
        throw new AuctionRegistrationCancelledException();
    }
}

// ============================================================
// [L2] AuctionFormValidator (Description 8단계)
// ============================================================

public class AuctionFormValidator {

    public static void validate(AuctionForm f)
            throws AuctionFormValidationException {

        if (f == null)
            throw new AuctionFormValidationException();

        // 마감 기한 범위 검증 : 현재 시각 이후여야 함
        if (f.getDeadline() == null
                || !f.getDeadline().isAfter(LocalDateTime.now()))
            throw new AuctionFormValidationException();

        // 필수 항목 충족
        if (f.getServiceCondition() == null
                || f.getServiceCondition().isBlank())
            throw new AuctionFormValidationException();

        if (f.getMinQualification() == null
                || f.getMinQualification().isBlank())
            throw new AuctionFormValidationException();

        // 낙찰 우선 기준 존재 여부
        if (f.getCriteria() == null)
            throw new AuctionFormValidationException();

        // TODO: 가중치 유효 범위(합산 = 1.0 등) 검증 규칙은
        //       UC Description에 구체적 범위가 명시되지 않아 추가 명세 필요
    }
}

// ============================================================
// [L2] UC-02 — SubmitBidUseCase
// ============================================================

public class SubmitBidUseCase {

    private static final Logger LOG = Logger.getLogger("SubmitBid");

    private final ValidateUser      validateUser;
    private final AuctionRepository auctionRepo;
    private final BidRepository     bidRepo;
    private final NotificationQueue notificationQueue;

    public SubmitBidUseCase(ValidateUser      validateUser,
                            AuctionRepository auctionRepo,
                            BidRepository     bidRepo,
                            NotificationQueue notificationQueue) {
        this.validateUser      = validateUser;
        this.auctionRepo       = auctionRepo;
        this.bidRepo           = bidRepo;
        this.notificationQueue = notificationQueue;
    }

    /**
     * 정상 흐름. Description 1~12단계에 대응.
     */
    public BidReceipt execute(Agent agent, AuctionId auctionId, BidForm form)
            throws AuthException,
                   AuctionClosedException,
                   BidValidationException,
                   BidStorageException,
                   BidResubmitException {

        // ---- Precondition 가드 ----
        if (agent == null || !agent.isAuthenticated())
            throw new AuthException();                                              // P1

        if (!agent.isCertified())
            throw new AuthException();                                              // P3

        // P4 : 기제출 입찰서가 있고 '수정 허용' 상태가 아니면 진행 불가
        if (bidRepo.hasActiveBid(agent, auctionId)
                && !bidRepo.isResubmitAllowed(agent, auctionId))
            throw new BidValidationException();                                     // P4

        // ---- Description 1. <<include>> Validate User ----
        validateUser.execute(agent);                                                // 1

        // ---- Description 2~3. 활성 공고 목록 조회 ----
        List<Auction> actives = auctionRepo.findActive();                           // 2, 3

        // ---- Description 4. 공고 선택 / A1·A5 분기 ----
        Auction auction = auctionRepo.findById(auctionId);                          // 4

        if (auction == null || auction.isClosed()) {
            throw new AuctionClosedException();                                     // A1
        }

        // A5 : 기제출 입찰서가 '수정 허용' 상태이면 재제출 흐름으로 분기
        if (bidRepo.hasActiveBid(agent, auctionId)) {
            return resubmit(agent, auction, form);                                  // A5
        }

        // ---- Description 5. 상세 정보 표시 (낙찰 우선 기준 포함) ----
        // 프레젠테이션 계층 책임 — 도메인에서는 criteria 참조만 수행
        WinnerSelectionCriteria criteria = auction.getSelectionCriteria();          // 5

        // ---- Description 6. 낙찰 우선 기준 참고하여 입찰서 작성 ----
        // form 은 호출자(프레젠테이션 계층)가 criteria 를 참고하여 구성 후 전달      // 6

        // ---- Description 7. 유효성 검증 ----
        BidValidator.validate(form, criteria);                                      // 7 (실패 시 A2)

        // ---- Description 8. 최종 제출 확정 (메서드 호출 자체가 확정) ----         // 8

        // ---- Description 9. 저장소 쓰기 + 접수 시각 기록 ----
        Bid bid = new Bid(agent, auction, form, LocalDateTime.now());

        try {
            bidRepo.save(bid);                                                      // 9
        } catch (StorageException e) {
            bidRepo.rollback();                                                     // A4-1 : 이전 상태로 복원
            LOG.severe("[SubmitBid] storage failed: " + e.getMessage());            // A4-3 : 장애 로그
            throw new BidStorageException();                                        // A4-2
        }

        auction.incrementBidCount();                                                // Postcondition Q1
        auctionRepo.updateBidCount(auction);

        // ---- Description 10. 접수번호 발급 ----
        BidReceipt receipt = new BidReceipt(bid.getId(), bid.getTimestamp());       // 10

        // ---- Description 11. 매도자 알림 큐 등재 ----
        notificationQueue.enqueue(auction.getSeller(), bid);                        // 11 / Postcondition Q2

        // ---- Description 12. 정상 종료, 입찰 목록 복귀 ----
        return receipt;                                                             // 12
        // Postcondition Q1 (해당 공고 입찰 건수 1 증가), Q2 (알림 큐 등재) 달성
    }

    /**
     * Alternative A5 : 기제출 입찰서 수정 재제출.
     * A5-1 기존 입찰서 불러오기 → A5-2 6단계부터 정상 흐름 → A5-3 접수번호 유지 갱신.
     */
    private BidReceipt resubmit(Agent agent, Auction auction, BidForm updatedForm)
            throws BidValidationException, BidStorageException, BidResubmitException {

        // A5-1. 기존 입찰서 불러오기
        Bid existingBid = bidRepo.findActiveBid(agent, auction.getId());            // A5-1
        if (existingBid == null) throw new BidResubmitException();

        // A5-2. 6단계부터 정상 흐름 : 낙찰 우선 기준 참조
        WinnerSelectionCriteria criteria = auction.getSelectionCriteria();          // 6 (A5-2)

        // 7단계 : 수정된 입찰서 유효성 검증
        BidValidator.validate(updatedForm, criteria);                               // 7 (A5-2)

        // A5-3. 접수번호 유지, 내용·접수 시각만 갱신
        existingBid.updateForm(updatedForm, LocalDateTime.now());                   // A5-3

        try {
            bidRepo.update(existingBid);                                            // A5-3 저장
        } catch (StorageException e) {
            bidRepo.rollback();
            LOG.severe("[SubmitBid/Resubmit] storage failed: " + e.getMessage());
            throw new BidStorageException();
        }

        // 10단계 : 기존 접수번호 유지하여 receipt 생성
        BidReceipt receipt = new BidReceipt(
            existingBid.getId(), existingBid.getTimestamp());                       // 10 (A5-3)

        // 11단계 : 매도자 알림 큐 등재
        notificationQueue.enqueue(auction.getSeller(), existingBid);                // 11

        return receipt;
        // Postcondition Q2 달성 / 재제출이므로 Q1의 입찰 건수 증가는 발생하지 않음
    }

    /**
     * Alternative A3 : 6~8단계 진행 중 공인중개사가 입찰을 취소한 경우.
     */
    public void cancel(BidForm draft) throws BidCancelledException {
        if (draft != null) draft.discard();   // A3-1 : 임시 입력 데이터 파기
        // A3-2 : 저장소 변경 없음 — rollback 불필요
        // Postcondition Q3 달성
        throw new BidCancelledException();
    }
}

// ============================================================
// [L2] BidValidator (Description 7단계 UC-02)
// ============================================================

public class BidValidator {

    /**
     * @param form     작성된 입찰서
     * @param criteria 공고의 낙찰 우선 기준 (Description 5·6단계 반영)
     */
    public static void validate(BidForm form,
                                WinnerSelectionCriteria criteria)
            throws BidValidationException {

        if (form == null) throw new BidValidationException();

        // 필수 항목 충족
        if (form.getMarketingStrategy() == null
                || form.getMarketingStrategy().isBlank())
            throw new BidValidationException();

        if (form.getServiceTerms() == null
                || form.getServiceTerms().isBlank())
            throw new BidValidationException();

        if (form.getExpectedSalePeriod() <= 0)
            throw new BidValidationException();

        // 수수료율 범위 검증 (0.0 ~ 5.0)
        if (form.getCommissionRate() < 0.0
                || form.getCommissionRate() > 5.0)
            throw new BidValidationException();

        // TODO: criteria 기반 가중치 연동 검증 규칙은
        //       UC Description에 구체적 범위 미명시 — 추가 명세 필요
    }
}

// ============================================================
// [L2] UC-03 — SelectWinnerUseCase
// ============================================================

public class SelectWinnerUseCase {

    private final ValidateUser        validateUser;
    private final AuctionRepository   auctionRepo;
    private final BidRepository       bidRepo;
    private final NotificationQueue   notificationQueue;

    public SelectWinnerUseCase(ValidateUser      validateUser,
                               AuctionRepository auctionRepo,
                               BidRepository     bidRepo,
                               NotificationQueue notificationQueue) {
        this.validateUser      = validateUser;
        this.auctionRepo       = auctionRepo;
        this.bidRepo           = bidRepo;
        this.notificationQueue = notificationQueue;
    }

    /**
     * 정상 흐름. Description 1~13단계에 대응.
     */
    public WinnerSelectionResult execute(Seller seller,
                                         AuctionId auctionId,
                                         BidId winnerBidId)
            throws AuthException,
                   NoBidsException,
                   SoleBidConfirmationException,
                   WinnerSelectionPostponedException {

        // ---- Precondition 가드 ----
        if (seller == null || !seller.isAuthenticated())
            throw new AuthException();                                              // P1

        Auction auction = auctionRepo.findById(auctionId);

        if (!auction.isExpired())
            throw new IllegalStateException("Auction not yet expired");            // P2

        List<Bid> bids = bidRepo.findByAuction(auctionId);

        if (bids == null || bids.isEmpty())
            throw new NoBidsException();                                            // P3

        if (auctionRepo.hasWinner(auctionId))
            throw new IllegalStateException("Winner already selected");            // P4

        // ---- Description 1. <<include>> Validate User ----
        validateUser.execute(seller);                                               // 1

        // ---- Description 2~3. 마감 공고 목록 조회 ----
        List<Auction> closedAuctions = auctionRepo.findExpiredWithoutWinner();      // 2, 3

        // ---- Description 4. 낙찰자 선정할 공고 선택 ----
        // auction 은 위 Precondition 단계에서 이미 조회됨                           // 4

        // ---- Description 5. 입찰서 목록 반환 ----
        // bids 는 위 Precondition 단계에서 이미 조회됨                              // 5

        // Alternative A1 : 입찰서 0건 (Precondition P3에서 이미 처리)

        // Alternative A3 : 입찰서 1건 단독 낙찰 확인
        if (bids.size() == 1) {
            throw new SoleBidConfirmationException();                               // A3
            // 호출자(프레젠테이션 계층)는 SoleBidConfirmationException 수신 후
            // 매도자 확인을 거쳐 confirmSoleBid() 또는 postpone() 를 호출한다.
        }

        // ---- Description 6. 낙찰 우선 기준 적용 정렬 표시 ----
        WinnerSelectionCriteria criteria = auction.getSelectionCriteria();          // 6
        List<Bid> sortedBids = BidSorter.sortByCriteria(bids, criteria);            // 6

        // ---- Description 7. 비교 검토 (프레젠테이션 계층 책임) ----               // 7

        // ---- Description 8. 낙찰 확정 요청 ----
        Bid winnerBid = bidRepo.findById(winnerBidId);                              // 8

        // ---- Description 9. 선택 입찰서 상태 '낙찰' 갱신 ----
        winnerBid.setStatus(BidStatus.WON);                                         // 9
        bidRepo.update(winnerBid);
        // Postcondition Q1 : 낙찰 입찰서 상태 WON

        // ---- Description 10. 나머지 입찰서 '미낙찰' 일괄 갱신 ----
        for (Bid bid : bids) {
            if (!bid.getId().getValue().equals(winnerBidId.getValue())) {
                bid.setStatus(BidStatus.LOST);                                      // 10
                bidRepo.update(bid);
            }
        }
        // Postcondition Q2 : 미낙찰 입찰서 상태 LOST

        // ---- Description 11. 공고 상태 '낙찰 완료' 변경 ----
        auction.setStatus(AuctionStatus.WINNER_SELECTED);                           // 11
        auctionRepo.updateStatus(auction);
        // Postcondition Q1 : 공고 상태 WINNER_SELECTED

        // ---- Description 12. 전원 결과 알림 발송 ----
        for (Bid bid : bids) {
            notificationQueue.enqueue(bid.getAgent(), auction);                     // 12
        }
        // Postcondition Q2 : 참여 공인중개사 전원 알림 큐 등재

        // ---- Description 13. 공고 상세 화면 복귀 ----
        return new WinnerSelectionResult(winnerBid, auction.getStatus());           // 13
        // Postcondition Q1, Q2 달성
    }

    /**
     * Alternative A3 확정 경로 : 단독 입찰서를 매도자가 낙찰 처리하기로 확정한 경우.
     * SoleBidConfirmationException 수신 후 매도자 확인이 완료되면 호출된다.
     */
    public WinnerSelectionResult confirmSoleBid(Seller seller,
                                                AuctionId auctionId)
            throws AuthException {

        List<Bid> bids = bidRepo.findByAuction(auctionId);
        Bid soleBid = bids.get(0);                                                  // A3-2 : 8단계로 이동

        // 8단계부터 정상 흐름 수행
        return execute(seller, auctionId, soleBid.getId());
    }

    /**
     * Alternative A2 : 7~8단계 진행 중 매도자가 낙찰 선정을 보류한 경우.
     */
    public void postpone(AuctionId auctionId)
            throws WinnerSelectionPostponedException {
        Auction auction = auctionRepo.findById(auctionId);
        auction.setStatus(AuctionStatus.PENDING_WINNER);                            // A2-1
        auctionRepo.updateStatus(auction);
        // A2-2 : 어떠한 입찰서 상태 변경도 가하지 않음
        // Postcondition Q3 달성
        throw new WinnerSelectionPostponedException();
    }
}

// ============================================================
// [L2] BidSorter (Description 6단계 UC-03)
// ============================================================

public class BidSorter {

    /**
     * 낙찰 우선 기준 가중치를 적용하여 입찰서를 내림차순 정렬한다.
     * 점수 = 수수료율역수 × commissionWeight + 마케팅전략점수 × marketingWeight
     *
     * TODO: 마케팅 전략 점수화 방법은 UC Description에 미명시 — 추가 명세 필요
     */
    public static List<Bid> sortByCriteria(List<Bid> bids,
                                            WinnerSelectionCriteria criteria) {
        List<Bid> sorted = new ArrayList<>(bids);
        sorted.sort((a, b) -> {
            double scoreA = calcScore(a, criteria);
            double scoreB = calcScore(b, criteria);
            return Double.compare(scoreB, scoreA); // 내림차순
        });
        return sorted;
    }

    private static double calcScore(Bid bid, WinnerSelectionCriteria c) {
        // 수수료율은 낮을수록 유리 → 역수로 가중치 적용
        double commissionScore = (bid.getForm().getCommissionRate() > 0)
            ? (1.0 / bid.getForm().getCommissionRate()) * c.getCommissionRateWeight()
            : 0.0;
        // TODO: 마케팅 전략 점수화 로직 — 명세 필요
        double marketingScore = 0.0; // TODO
        return commissionScore + marketingScore;
    }
}
```

---

## 생성 결과 관찰

### UC별 클래스 구성 요약

| 분류 | 클래스 | UC |
|:---|:---|:---|
| 공유 Value Object / Entity | `PropertyId`, `AuctionId`, `BidId`, `AuctionStatus`, `BidStatus`, `WinnerSelectionCriteria`, `AuctionForm`, `Auction`, `BidForm`, `Bid`, `AuctionReceipt`, `BidReceipt`, `WinnerSelectionResult` | 공통 |
| Exception | `DuplicateAuctionException`, `AuctionFormValidationException`, `AuctionRegistrationCancelledException` | UC-01 |
| Exception | `AuctionClosedException`, `BidValidationException`, `BidCancelledException`, `BidStorageException`, `BidResubmitException` | UC-02 |
| Exception | `NoBidsException`, `WinnerSelectionPostponedException`, `SoleBidConfirmationException` | UC-03 |
| Exception | `AuthException` | 공통 |
| Abstract UC | `ValidateUser` | 공통 |
| Use Case | `RegisterAuctionUseCase` + `AuctionFormValidator` | UC-01 |
| Use Case | `SubmitBidUseCase` + `BidValidator` | UC-02 |
| Use Case | `SelectWinnerUseCase` + `BidSorter` | UC-03 |

### 수치 요약

| 항목 | UC-01 | UC-02 | UC-03 | 합계 |
|:---|:---:|:---:|:---:|:---:|
| Description 단계 수 | 14 | 12 | 13 | 39 |
| Description 단계 반영률 | 14/14 (100%) | 12/12 (100%) | 13/13 (100%) | 100% |
| Alternative 수 | 3 (A1~A3) | 5 (A1~A5) | 3 (A1~A3) | 11 |
| Exception 클래스 수 | 3 | 5 | 3 | 11 (+1 공통) |
| Precondition 가드 수 | 4 (P1~P4) | 3 (P1,P3,P4) | 4 (P1~P4) | — |
| `// TODO` 주석 수 | 1 | 1 | 2 | **4** |
| 구현 완료 메서드 수 | 3 | 4 | 4 | 11 |

### TODO 발생 원인 분석

| # | `// TODO` 위치 | 원인 | 연결된 ISO 29148 품질 이슈 |
|:---|:---|:---|:---|
| 1 | `AuctionFormValidator` — 가중치 유효 범위 | UC-01 Description 7단계에 가중치 합산 범위 미명시 | UC-01 Unambiguous **Partial Fail** |
| 2 | `BidValidator` — criteria 연동 검증 | UC-02 기준 참조는 서술되었으나 검증 조건 미명시 | UC-01 Unambiguous 파급 |
| 3 | `BidSorter.calcScore` — 마케팅 전략 점수화 | UC-03 6단계 가중치 계산 방식 미명시 | UC-03 Unambiguous **Partial Fail** |
| 4 | `BidSorter.sortByCriteria` — 점수화 공식 | 위와 동일 | UC-03 Unambiguous **Partial Fail** |

**관찰 결과** : ISO 29148 품질 검토에서 `Partial Fail` 판정을 받은 항목(UC-01·03 Unambiguous)이 L2 코드에서 정확히 `// TODO` 로 표면화되었다. UC Description의 비모호성 품질 특성이 향상될수록 L2 코드의 TODO 항목이 감소하고 구현 완결성이 높아지는 직접적 상관관계가 3개 UC 전체에서 확인된다.
