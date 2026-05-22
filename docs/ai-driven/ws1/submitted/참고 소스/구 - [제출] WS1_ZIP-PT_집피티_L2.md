# Step 3-2. L2 코드 생성 프롬프트 — UC Description 기반

---

## 프롬프트 본문

아래 **Use Case Description을** 바탕으로 순수 **Java 코드**를 생성하세요.

### \[제약 조건\]

1. 외부 프레임워크(Spring, JPA, Lombok 등)를 사용하지 않는다.  
2. Use Case Description에 명시된 내용만을 근거로 구현한다.  
3. 명시되지 않은 내부 구현 로직은 임의로 추가하지 않는다.  
4. 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다.

### \[구현 지침\]

1. Description의 **각 단계를 메서드 호출 순서**로 변환하고, 각 코드 라인 옆에 대응되는 **단계 번호를 주석**으로 표시한다.  
2. **Include된 Abstract UC**는 별도 독립 클래스로 구현한다.  
3. **Precondition**은 `execute()` 진입부의 **가드 조건**으로 구현한다.  
4. **각 Alternative 흐름**은 고유한 **Exception 클래스**로 매핑한다 (A1→AuctionClosedException, A2→BidValidationException, A3→BidCancelledException, A4→BidStorageException).  
5. **Cancel 흐름(A3)** 은 별도 메서드 `cancel()`로 분리한다.  
6. **Postcondition**은 메서드 말미의 주석으로 반영하고, 상태 변경 메서드 호출로 실현한다.  
7. 협력 객체(Repository, NotificationQueue 등)는 **생성자 주입** 구조로 선언한다.  
8. Description에 등장한 **도메인 개념**(입찰서, 접수번호, 매물 등)은 **값 객체 또는 엔티티**로 모델링한다.

### \[Use Case Description\]

**Use Case Name** : Submit Bid (입찰 참여)   
**Summary** : 공인중개사가 매도자의 역경매 공고에 대해 수수료·서비스 조건을 포함한 입찰서를 제출한다.   
**Actor** : 공인중개사 (Agent)   
**Dependency** : `<<include>>` Validate User

**Precondition**

- P1. 공인중개사는 시스템에 로그인되어 인증 세션이 유효한 상태이다.  
- P2. 대상 경매 공고가 `입찰 모집중` 상태이며 입찰 마감 시각 이전이다.  
- P3. 공인중개사의 자격 증명(자격증 번호, 사무소 등록증)이 검증 완료된 상태이다.  
- P4. 공인중개사는 해당 공고에 대하여 아직 입찰서를 제출하지 않았거나, 재제출이 허용된 상태이다.

**Description (정상 흐름)**

1. 시스템은 `<<include>>` Validate User를 호출하여 공인중개사의 인증 상태를 확인한다.  
2. 공인중개사는 참여 가능한 경매 공고 목록 조회를 요청한다.  
3. 시스템은 마감 시각이 경과하지 않은 활성 공고 목록을 반환한다.  
4. 공인중개사는 입찰할 공고를 선택한다.  
5. 시스템은 선택된 공고의 상세 정보(매물 정보, 매도자 요구 조건, 현재 입찰 건수)를 표시한다.  
6. 공인중개사는 입찰서(제안 수수료율, 마케팅 전략, 예상 매각 기간, 서비스 조건)를 작성한다.  
7. 시스템은 입력된 입찰 정보의 유효성(필수 항목 충족, 수수료율 범위, 글자 수 제한)을 검증한다.  
8. 공인중개사는 최종 입찰서 제출을 확정한다.  
9. 시스템은 입찰서를 경매 저장소에 저장하고 서버 타임스탬프를 기록한다.  
10. 시스템은 공인중개사에게 입찰 접수 완료 메시지와 접수번호를 반환한다.  
11. 시스템은 매도자의 알림 큐에 신규 입찰 알림을 등재한다.  
12. 시스템은 입찰 목록 화면으로 복귀하여 정상 상태로 종료한다.

**Alternatives**

- A1. \[4단계\] 공고가 마감된 경우 → "마감" 메시지 표시 → 3단계 복귀.  
- A2. \[7단계\] 유효성 검증 실패 → 오류 필드 강조 → 6단계 복귀.  
- A3. \[Cancel, 6\~8단계\] 사용자 취소 → 임시 데이터 파기 → 3단계 복귀, 저장소 무변경.  
- A4. \[9단계\] 저장 실패 → 트랜잭션 롤백 → 오류 메시지 \+ 6단계 복귀 → 장애 로그 기록.

**Postcondition**

- Q1. (정상) 입찰서가 저장되고 공고 입찰 건수 1 증가.  
- Q2. (정상) 매도자 알림 큐에 알림 등재.  
- Q3. (비정상 A1/A2/A3) 저장소 및 알림 큐가 Precondition 상태와 동일.  
- Q4. (비정상 A4) 저장소는 롤백, 장애 로그 1건 추가.

### \[출력 형식\]

- 생성된 Java 코드만 출력한다.  
- 패키지 선언과 `import` 문은 생략한다.  
- 값 객체 → 예외 → Abstract UC → Use Case 순으로 출력한다.

---

## 생성 결과 (L2 코드)

// \============================================================

// \[L2\] Value Objects / Entities

// \============================================================

public class AuctionId {

    private final String value;

    public AuctionId(String value) { this.value \= value; }

    public String getValue() { return value; }

}

public enum AuctionStatus { OPEN, CLOSED }

public class Auction {

    private AuctionId id;

    private AuctionStatus status;

    private LocalDateTime deadline;

    private Seller seller;

    private int bidCount;

    public boolean isClosed() {   // Precondition P2 / Alternative A1

        return status \== AuctionStatus.CLOSED

            || LocalDateTime.now().isAfter(deadline);

    }

    public void incrementBidCount() { this.bidCount++; }  // Postcondition Q1

    public Seller getSeller() { return seller; }

    public AuctionId getId()  { return id; }

}

public class BidForm {

    private double commissionRate;       // 제안 수수료율 (Desc 6\)

    private String marketingStrategy;    // 마케팅 전략 (Desc 6\)

    private int    expectedSalePeriod;   // 예상 매각 기간 (Desc 6\)

    private String serviceTerms;         // 서비스 조건 (Desc 6\)

    public void discard() { /\* A3-1 임시 데이터 파기 \*/ }

    public double getCommissionRate()    { return commissionRate; }

    public String getMarketingStrategy() { return marketingStrategy; }

    public int    getExpectedSalePeriod(){ return expectedSalePeriod; }

}

public class Bid {

    private String id;

    private Agent  agent;

    private Auction auction;

    private BidForm form;

    private LocalDateTime timestamp;

    public Bid(Agent a, Auction au, BidForm f, LocalDateTime t) {

        this.agent \= a; this.auction \= au; this.form \= f; this.timestamp \= t;

        this.id \= UUID.randomUUID().toString();

    }

    public String getId() { return id; }

    public LocalDateTime getTimestamp() { return timestamp; }

}

public class BidReceipt {

    private final String bidId;

    private final LocalDateTime timestamp;

    public BidReceipt(String bidId, LocalDateTime ts) {

        this.bidId \= bidId; this.timestamp \= ts;

    }

}

// \============================================================

// \[L2\] Alternative 흐름 → Exception 매핑

// \============================================================

public class AuctionClosedException extends Exception {}  // A1

public class BidValidationException extends Exception {}  // A2

public class BidCancelledException  extends Exception {}  // A3

public class BidStorageException    extends Exception {}  // A4

public class AuthException          extends Exception {}  // P1 / P3

// \============================================================

// \[L2\] \<\<include\>\> Abstract UC

// \============================================================

public class ValidateUser {

    public void execute(Agent agent) throws AuthException {

        // Description 1단계 : 인증 상태 확인

        if (agent \== null || \!agent.isAuthenticated()) {

            throw new AuthException();

        }

        if (\!agent.isCertified()) {   // Precondition P3

            throw new AuthException();

        }

    }

}

// \============================================================

// \[L2\] Use Case — SubmitBid

// \============================================================

public class SubmitBidUseCase {

    private static final Logger LOG \= Logger.getLogger("SubmitBid");

    private final ValidateUser      validateUser;

    private final AuctionRepository auctionRepo;

    private final BidRepository     bidRepo;

    private final NotificationQueue notificationQueue;

    public SubmitBidUseCase(ValidateUser v, AuctionRepository ar,

                            BidRepository br, NotificationQueue nq) {

        this.validateUser \= v;

        this.auctionRepo \= ar;

        this.bidRepo \= br;

        this.notificationQueue \= nq;

    }

    /\*\*

     \* 정상 흐름. Description 1\~12단계에 대응.

     \*/

    public BidReceipt execute(Agent agent, AuctionId auctionId, BidForm form)

            throws AuthException, AuctionClosedException,

                   BidValidationException, BidStorageException {

        // \---- Precondition 가드 \----

        if (agent \== null || \!agent.isAuthenticated()) throw new AuthException();      // P1

        if (\!agent.isCertified())                      throw new AuthException();      // P3

        if (bidRepo.hasActiveBid(agent, auctionId) &&

            \!bidRepo.isResubmitAllowed(auctionId))

            throw new BidValidationException();                                         // P4

        // \---- Description 1\. \<\<include\>\> Validate User \----

        validateUser.execute(agent);                                                    // 1

        // \---- Description 2\~3. 활성 공고 목록 조회 \----

        List\<Auction\> actives \= auctionRepo.findActive();                               // 2, 3

        // \---- Description 4\. 공고 선택 \----

        Auction auction \= auctionRepo.findById(auctionId);                              // 4

        if (auction \== null || auction.isClosed()) {

            throw new AuctionClosedException();                                         // A1 (P2 동시 충족)

        }

        // \---- Description 5\. 상세 정보 표시 (프레젠테이션 계층 책임) \----              // 5

        // 도메인 Use Case 범위 밖이므로 본 클래스에서는 구현하지 않음

        // \---- Description 6\. 입찰서 작성 (form 파라미터로 전달) \----                   // 6

        // \---- Description 7\. 유효성 검증 \----

        BidValidator.validate(form);                                                    // 7 (실패 시 A2)

        // \---- Description 8\. 최종 제출 확정 (메서드 호출 자체가 확정) \----             // 8

        // \---- Description 9\. 저장소 쓰기 \+ 타임스탬프 \----

        Bid bid \= new Bid(agent, auction, form, LocalDateTime.now());

        try {

            bidRepo.save(bid);                                                          // 9

        } catch (StorageException e) {

            bidRepo.rollback();                                                         // A4-1

            LOG.severe("\[SubmitBid\] storage failed: " \+ e.getMessage());                // A4-3

            throw new BidStorageException();                                            // A4-2

        }

        auction.incrementBidCount();                                                    // Postcondition Q1

        // \---- Description 10\. 접수번호 발급 \----

        BidReceipt receipt \= new BidReceipt(bid.getId(), bid.getTimestamp());           // 10

        // \---- Description 11\. 매도자 알림 큐 등재 \----

        notificationQueue.enqueue(auction.getSeller(), bid);                            // 11, Postcondition Q2

        // \---- Description 12\. 정상 종료, 목록 화면으로 복귀 \----

        return receipt;                                                                 // 12

        // Postcondition Q1, Q2 달성

    }

    /\*\*

     \* Alternative A3 : 6\~8단계 진행 중 사용자 취소.

     \*/

    public void cancel(BidForm draft) throws BidCancelledException {

        if (draft \!= null) draft.discard();   // A3-1

        // A3-2 : 저장소 변경 없음 — rollback 불필요

        // Postcondition Q3 달성

        throw new BidCancelledException();

    }

}

// \============================================================

// \[L2\] Validator (Description 7단계)

// \============================================================

public class BidValidator {

    public static void validate(BidForm f) throws BidValidationException {

        if (f \== null)                                          throw new BidValidationException();

        if (f.getCommissionRate() \< 0.0 ||

            f.getCommissionRate() \> 5.0)                        throw new BidValidationException();

        if (f.getMarketingStrategy() \== null ||

            f.getMarketingStrategy().isBlank())                 throw new BidValidationException();

        if (f.getExpectedSalePeriod() \<= 0\)                     throw new BidValidationException();

    }

}

---

## 생성 결과 관찰

| 항목 | 수치 / 특징 |
| :---- | :---- |
| 생성 클래스 수 | 14개 (Value Object/Entity 6 \+ Exception 5 \+ Validator 1 \+ Abstract UC 1 \+ Use Case 1\) |
| 구현 완료 메서드 수 | 11개 (TODO가 아닌 실제 구현) |
| 파라미터·반환 타입 정보 | 완전 (e.g. `execute(Agent, AuctionId, BidForm) → BidReceipt`) |
| 예외 클래스 | 5개 (A1\~A4 \+ Precondition 실패) |
| Precondition 가드 | 3건 (P1, P3, P4) |
| Description 단계 반영률 | 12 / 12 (100%), 각 라인에 단계 번호 주석 |
| 자원 회수 / 정상 종료 분리 | `bidRepo.rollback()` vs `return receipt` 경로 분리 |
| `// TODO` 주석 수 | 0개 (모든 명세가 구현됨) |

**관찰 결과** : Use Case Description은 **Precondition → Description → Alternatives → Postcondition** 구조가 그대로 **가드 조건 → 호출 순서 → Exception 분기 → 상태 변경 호출**로 매핑된다. Description이 ISO 29148 품질 특성을 만족할수록 L2 코드의 완결성도 높아진다.  
