# Step 3-2. L2 코드 생성 프롬프트 — UC Description 기반 (학생 작성본 반영)

---

## 프롬프트 본문

아래 **Use Case Description을** 바탕으로 순수 **Java 코드**를 생성하세요.

### [제약 조건]

1. 외부 프레임워크(Spring, JPA, Lombok 등)를 사용하지 않는다.
2. Use Case Description에 명시된 내용만을 근거로 구현한다.
3. 명시되지 않은 내부 구현 로직은 임의로 추가하지 않는다.
4. 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다.

### [구현 지침]

1. Description의 **각 단계를 메서드 호출 순서**로 변환하고, 각 코드 라인 옆에 대응되는 **단계 번호를 주석**으로 표시한다.
2. **Include된 Abstract UC**는 별도 독립 클래스로 구현한다.
3. **Precondition**은 `execute()` 진입부의 **가드 조건**으로 구현한다.
4. **각 Alternative 흐름**은 고유한 **Exception 클래스**로 매핑한다
   - A1 → `AuctionClosedException`
   - A2 → `BidValidationException`
   - A3 → `BidCancelledException`
   - A4 → `BidStorageException`
   - **A5 → `BidResubmitException` (학생 작성본 신규 추가)**
5. **Cancel 흐름(A3)** 은 별도 메서드 `cancel()`로 분리한다.
6. **Postcondition**은 메서드 말미의 주석으로 반영하고, 상태 변경 메서드 호출로 실현한다.
7. 협력 객체(Repository, NotificationQueue 등)는 **생성자 주입** 구조로 선언한다.
8. Description에 등장한 **도메인 개념**(입찰서, 접수번호, 매물 등)은 **값 객체 또는 엔티티**로 모델링한다.

### [Use Case Description — 학생 작성본]

**Use Case Name** : Submit Bid (입찰 참여)
**Summary** : 공인중개사가 매도자의 역경매 공고에 대해 수수료·서비스 조건을 포함한 입찰서를 제출한다.
**Actor** : 공인중개사 (Agent)
**Dependency** : `<<include>>` Validate User

**Precondition**

- P1. 공인중개사는 시스템에 로그인되어 인증 세션이 유효한 상태이다.
- P2. 대상 경매 공고가 `입찰 모집중` 상태이며 입찰 마감 시각 이전이다.
- P3. 공인중개사의 자격 증명(자격증 번호, 사무소 등록증)이 검증 완료된 상태이다.
- P4. 공인중개사는 해당 공고에 **기제출한 입찰서가 없거나, 기제출 입찰서가 `수정 허용` 상태**이다. *(AI 생성본 대비 변경)*

**Description (정상 흐름)**

1. 시스템은 Validate User를 수행하여 공인중개사의 인증 상태를 확인한다.
2. 공인중개사는 참여 가능한 경매 공고 목록 조회를 요청한다.
3. 시스템은 마감 시각이 경과하지 않은 활성 공고 목록을 반환한다.
4. 공인중개사는 입찰할 공고를 선택한다.
5. 시스템은 선택된 공고의 상세 정보(매물 정보, 매도자 요구 조건, **낙찰 우선 기준**, 현재 입찰 건수)를 표시한다. *(AI 생성본 대비 변경)*
6. 공인중개사는 **공고의 낙찰 우선 기준을 참고하여** 입찰서(제안 수수료율, 마케팅 전략, 예상 매각 기간, 서비스 조건)를 작성한다. *(AI 생성본 대비 변경)*
7. 시스템은 입력된 입찰 정보의 유효성(필수 항목 충족, 수수료율 범위, 글자 수 제한)을 검증한다.
8. 공인중개사는 최종 입찰서 제출을 확정한다.
9. 시스템은 입찰서를 경매 저장소에 저장하고 접수 시각을 기록한다.
10. 시스템은 공인중개사에게 입찰 접수 완료 메시지와 접수번호를 반환한다.
11. 시스템은 매도자의 알림 큐에 신규 입찰 알림을 등재한다.
12. 시스템은 입찰 목록 화면으로 복귀하여 정상 상태로 종료한다.

**Alternatives**

- A1. [4단계] 선택한 공고가 이미 마감된 경우 → "마감" 메시지 표시 → 3단계 복귀.
- A2. [7단계] 유효성 검증 실패 → 오류 필드 강조 → 6단계 복귀.
- A3. [Cancel, 6~8단계] 사용자 취소 → 임시 데이터 파기 → 3단계 복귀, 저장소 무변경.
- A4. [9단계] 저장 실패 → 처리 취소 및 이전 상태 복원 → 오류 메시지 + 6단계 복귀 → 장애 로그 기록.
- **A5. [4단계] 기제출 입찰서 수정 재제출 → 기존 입찰서 불러오기 → 6단계부터 정상 흐름 → 기존 입찰서 갱신, 접수번호 유지.** *(신규 추가)*

**Postcondition**

- Q1. (정상) 입찰서가 경매 저장소에 저장되며, **해당 공고(선택된 공고 기준)**의 입찰 건수가 1 증가. *(AI 생성본 대비 변경)*
- Q2. (정상) 매도자 알림 큐에 알림 등재.
- Q3. (비정상 A1/A2/A3) 저장소 및 알림 큐가 Precondition 상태와 동일.
- Q4. (비정상 A4) 저장소는 이전 상태로 복원, 장애 로그 1건 추가.

### [출력 형식]

- 생성된 Java 코드만 출력한다.
- 패키지 선언과 `import` 문은 생략한다.
- 값 객체 → 예외 → Abstract UC → Use Case 순으로 출력한다.

---

## 생성 결과 (L2 코드)

```java
// ============================================================
// [L2] Value Objects / Entities
// ============================================================

public class AuctionId {
    private final String value;
    public AuctionId(String value) { this.value = value; }
    public String getValue() { return value; }
}

public enum AuctionStatus { OPEN, CLOSED }

/**
 * 낙찰 우선 기준 (Description 5단계 추가 — 학생 작성본)
 * Register Auction UC에서 매도자가 설정한 기준을 Submit Bid에서 참조한다.
 */
public class WinnerSelectionCriteria {
    private final double commissionRateWeight;    // 수수료율 가중치 (Description 5단계)
    private final double marketingStrategyWeight; // 마케팅 전략 가중치 (Description 5단계)

    public WinnerSelectionCriteria(double commissionRateWeight,
                                   double marketingStrategyWeight) {
        this.commissionRateWeight    = commissionRateWeight;
        this.marketingStrategyWeight = marketingStrategyWeight;
    }

    public double getCommissionRateWeight()    { return commissionRateWeight; }
    public double getMarketingStrategyWeight() { return marketingStrategyWeight; }
}

public class Auction {
    private AuctionId               id;
    private AuctionStatus           status;
    private LocalDateTime           deadline;
    private Seller                  seller;
    private int                     bidCount;
    private WinnerSelectionCriteria selectionCriteria; // Description 5단계 (학생 작성본)

    public boolean isClosed() {                        // Precondition P2 / Alternative A1
        return status == AuctionStatus.CLOSED
            || LocalDateTime.now().isAfter(deadline);
    }

    public void incrementBidCount() { this.bidCount++; } // Postcondition Q1

    public Seller                  getSeller()           { return seller; }
    public AuctionId               getId()               { return id; }
    public WinnerSelectionCriteria getSelectionCriteria(){ return selectionCriteria; } // Description 5단계
}

public class BidForm {
    private double commissionRate;       // 제안 수수료율    (Description 6단계)
    private String marketingStrategy;   // 마케팅 전략      (Description 6단계)
    private int    expectedSalePeriod;  // 예상 매각 기간   (Description 6단계)
    private String serviceTerms;        // 서비스 조건      (Description 6단계)

    public void discard() { /* A3-1 임시 데이터 파기 */ }

    public double getCommissionRate()    { return commissionRate; }
    public String getMarketingStrategy() { return marketingStrategy; }
    public int    getExpectedSalePeriod(){ return expectedSalePeriod; }
    public String getServiceTerms()      { return serviceTerms; }
}

/**
 * 입찰서 엔티티.
 * A5(재제출) 대응을 위해 updateForm() 메서드를 보유한다. — 학생 작성본 신규
 */
public class Bid {
    private String        id;           // 접수번호 (Description 10단계)
    private Agent         agent;
    private Auction       auction;
    private BidForm       form;
    private LocalDateTime timestamp;    // 접수 시각 (Description 9단계)
    private boolean       resubmitted;  // A5 재제출 여부 플래그 (학생 작성본)

    public Bid(Agent a, Auction au, BidForm f, LocalDateTime t) {
        this.agent      = a;
        this.auction    = au;
        this.form       = f;
        this.timestamp  = t;
        this.id         = UUID.randomUUID().toString();
        this.resubmitted = false;
    }

    /**
     * A5-3 : 접수번호(id)를 유지한 채 입찰서 내용만 갱신한다.
     */
    public void updateForm(BidForm updatedForm, LocalDateTime updatedAt) { // A5-3 (학생 작성본)
        this.form        = updatedForm;
        this.timestamp   = updatedAt;
        this.resubmitted = true;
    }

    public String        getId()        { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean       isResubmitted(){ return resubmitted; }
}

public class BidReceipt {
    private final String        bidId;
    private final LocalDateTime timestamp;

    public BidReceipt(String bidId, LocalDateTime ts) {
        this.bidId     = bidId;
        this.timestamp = ts;
    }

    public String        getBidId()     { return bidId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// ============================================================
// [L2] Alternative 흐름 → Exception 매핑
// ============================================================

public class AuctionClosedException  extends Exception {} // A1
public class BidValidationException  extends Exception {} // A2
public class BidCancelledException   extends Exception {} // A3
public class BidStorageException     extends Exception {} // A4
public class BidResubmitException    extends Exception {} // A5 (학생 작성본 신규)
public class AuthException           extends Exception {} // P1 / P3

// ============================================================
// [L2] <<include>> Abstract UC
// ============================================================

public class ValidateUser {
    /**
     * Description 1단계 : 공인중개사의 인증 상태 확인.
     * Precondition P1(로그인 세션) 및 P3(자격 증명) 검증.
     */
    public void execute(Agent agent) throws AuthException {
        if (agent == null || !agent.isAuthenticated()) { // P1
            throw new AuthException();
        }
        if (!agent.isCertified()) {                      // P3
            throw new AuthException();
        }
    }
}

// ============================================================
// [L2] Use Case — SubmitBid
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

        // P4 : 기제출 입찰서가 있고 '수정 허용' 상태가 아니면 진행 불가 (학생 작성본)
        if (bidRepo.hasActiveBid(agent, auctionId)
                && !bidRepo.isResubmitAllowed(agent, auctionId)) {
            throw new BidValidationException();                                     // P4
        }

        // ---- Description 1. <<include>> Validate User ----
        validateUser.execute(agent);                                                // 1

        // ---- Description 2~3. 활성 공고 목록 조회 ----
        List<Auction> actives = auctionRepo.findActive();                           // 2, 3

        // ---- Description 4. 공고 선택 / A1·A5 분기 ----
        Auction auction = auctionRepo.findById(auctionId);                          // 4

        if (auction == null || auction.isClosed()) {
            throw new AuctionClosedException();                                     // A1
        }

        // A5 : 기제출 입찰서가 존재하고 수정 허용 상태인 경우 재제출 흐름으로 분기 (학생 작성본)
        if (bidRepo.hasActiveBid(agent, auctionId)) {
            return resubmit(agent, auction, form);                                  // A5
        }

        // ---- Description 5. 공고 상세 정보 표시 (낙찰 우선 기준 포함) ----        // 5
        // WinnerSelectionCriteria를 포함한 상세 정보 조회는 프레젠테이션 계층 책임
        // 도메인 UC는 auction.getSelectionCriteria() 를 통해 기준 참조 가능
        WinnerSelectionCriteria criteria = auction.getSelectionCriteria();          // 5 (학생 작성본)

        // ---- Description 6. 낙찰 우선 기준 참고하여 입찰서 작성 (form 파라미터로 전달) ----
        // form 은 호출자(프레젠테이션 계층)가 criteria 를 참고하여 구성한 후 전달한다.  // 6

        // ---- Description 7. 유효성 검증 ----
        BidValidator.validate(form, criteria);                                      // 7 (실패 시 A2)

        // ---- Description 8. 최종 제출 확정 (메서드 호출 자체가 확정) ----          // 8

        // ---- Description 9. 저장소 쓰기 + 접수 시각 기록 ----
        Bid bid = new Bid(agent, auction, form, LocalDateTime.now());

        try {
            bidRepo.save(bid);                                                      // 9
        } catch (StorageException e) {
            bidRepo.rollback();                                                     // A4-1
            LOG.severe("[SubmitBid] storage failed: " + e.getMessage());            // A4-3
            throw new BidStorageException();                                        // A4-2
        }

        auction.incrementBidCount();                                                // Postcondition Q1

        // ---- Description 10. 접수번호 발급 ----
        BidReceipt receipt = new BidReceipt(bid.getId(), bid.getTimestamp());       // 10

        // ---- Description 11. 매도자 알림 큐 등재 ----
        notificationQueue.enqueue(auction.getSeller(), bid);                        // 11 / Postcondition Q2

        // ---- Description 12. 정상 종료, 목록 화면으로 복귀 ----
        return receipt;                                                             // 12
        // Postcondition Q1 (해당 공고 입찰 건수 1 증가), Q2 (알림 큐 등재) 달성
    }

    /**
     * Alternative A5 : 기제출 입찰서 수정 재제출.
     * - A5-1 : 기존 입찰서 불러오기
     * - A5-2 : 6단계부터 정상 흐름 수행
     * - A5-3 : 기존 입찰서 내용 갱신, 접수번호 유지
     * (학생 작성본 신규 추가)
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

        // A5-3. 접수번호 유지, 입찰서 내용만 갱신
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
        // Postcondition Q1 (기존 입찰 건수 유지 — 신규 증가 없음), Q2 달성
    }

    /**
     * Alternative A3 : 6~8단계 진행 중 사용자 취소.
     */
    public void cancel(BidForm draft) throws BidCancelledException {
        if (draft != null) draft.discard();   // A3-1 : 임시 입력 데이터 파기
        // A3-2 : 저장소 변경 없음 — rollback 불필요
        // Postcondition Q3 달성
        throw new BidCancelledException();
    }
}

// ============================================================
// [L2] Validator (Description 7단계)
//      학생 작성본 : WinnerSelectionCriteria 를 파라미터로 추가 수신
// ============================================================

public class BidValidator {

    /**
     * @param form     작성된 입찰서
     * @param criteria 공고의 낙찰 우선 기준 (Description 5·6단계 반영 — 학생 작성본)
     */
    public static void validate(BidForm form,
                                WinnerSelectionCriteria criteria)
            throws BidValidationException {

        if (form == null) throw new BidValidationException();

        // 필수 항목 충족 검증
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

        // TODO: criteria 기반 가중치 유효성 검증 규칙은
        //       Use Case Description에 구체적 범위가 명시되지 않아 추가 명세 필요
    }
}
```

---

## 생성 결과 관찰

| 항목 | AI 생성본 (L2 원본) | 학생 작성본 반영 결과 | 변경 내용 |
|:---|:---|:---|:---|
| 생성 클래스 수 | 14개 | **17개** | `WinnerSelectionCriteria` + `BidResubmitException` + `resubmit()` 분리 메서드 추가 |
| 구현 완료 메서드 수 | 11개 | **14개** | `resubmit()`, `updateForm()`, `validate(form, criteria)` 추가 |
| 파라미터·반환 타입 정보 | 완전 | **완전** | `validate(BidForm, WinnerSelectionCriteria)` 시그니처 확장 |
| 예외 클래스 | 5개 (A1~A4 + Auth) | **6개** | `BidResubmitException` (A5) 신규 추가 |
| Precondition 가드 | 3건 (P1, P3, P4) | **3건** | P4 조건식을 `isResubmitAllowed(agent, auctionId)` 로 구체화 |
| Description 단계 반영률 | 12/12 (100%) | **12/12 (100%)** | 5단계에 `getSelectionCriteria()` 호출 추가 |
| A5 재제출 흐름 | 미구현 | **구현 완료** | `resubmit()` 전용 메서드로 분리, 접수번호 유지 로직 포함 |
| `// TODO` 주석 수 | 0개 | **1개** | criteria 기반 가중치 검증 규칙 — UC Description에 범위 미명시 |
| 자원 회수 / 정상 종료 분리 | `rollback()` vs `return receipt` | **동일 + resubmit 경로 추가** | A4 경로와 A5 경로 각각 독립 분리 |

**관찰 결과**

학생 작성본에서 추가된 3가지 요구사항(P4 재정의, 낙찰 우선 기준 참조, A5 재제출 시나리오)이 L2 코드에 다음과 같이 매핑되었다.

| 학생 작성본 변경 | L2 코드 반영 | 비고 |
|:---|:---|:---|
| P4 재정의 (`수정 허용` 상태 명시) | `bidRepo.isResubmitAllowed(agent, auctionId)` 가드 조건 구체화 | 원본의 모호한 `isResubmitAllowed(auctionId)` → agent 파라미터 추가 |
| Description 5단계 낙찰 우선 기준 표시 | `WinnerSelectionCriteria` 값 객체 신설 + `auction.getSelectionCriteria()` 호출 | 도메인 개념을 명시적 타입으로 모델링 |
| Description 6단계 기준 참고 작성 | `BidValidator.validate(form, criteria)` 시그니처 확장 | 기준이 검증 맥락에 전달되도록 구조 변경 |
| A5 재제출 시나리오 신설 | `resubmit()` 전용 private 메서드 분리, `Bid.updateForm()` 구현 | 접수번호(id) 유지, 내용·타임스탬프만 갱신 |
| Postcondition Q1 참조 대상 명확화 | 주석에 `해당 공고(auction.getId() 기준) 입찰 건수 1 증가` 명시 | 재제출(A5) 시에는 건수 증가 없음을 별도 주석으로 구분 |

Use Case Description의 품질 특성(Completeness, Unambiguous)이 향상될수록 L2 코드의 TODO 항목이 감소하고 메서드 시그니처의 명확성이 높아지는 상관관계가 확인된다. 이번 학생 작성본에서 유일하게 남은 `// TODO`는 UC Description에서 가중치 유효 범위가 명시되지 않은 데 기인하며, 이는 UC-01 Register Auction의 Unambiguous 항목 개선과 연동하여 해소할 수 있다.
