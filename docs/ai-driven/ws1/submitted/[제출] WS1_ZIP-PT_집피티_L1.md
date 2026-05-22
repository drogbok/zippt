# Step 3-1. L1 코드 생성 프롬프트 — UC Diagram 기반

---

## 프롬프트 본문

아래 **Use Case Diagram 정보만을** 바탕으로 순수 **Java 코드**를 생성하세요.

### \[제약 조건\]

1. 외부 프레임워크(Spring, JPA, Lombok 등)를 사용하지 않는다.  
2. Use Case Diagram에 명시된 정보 이외의 내용은 임의로 추가하지 않는다.  
3. 내부 구현 로직을 추론하여 채우지 않는다.  
4. 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다.

### \[구현 지침\]

1. 각 **Actor는 독립 클래스**로 생성한다 (필드·메서드는 Diagram에 없으면 추가하지 않는다).  
2. \*\*Abstract UC는 `abstract class`\*\*로 생성한다.  
3. 각 **Concrete UC는 클래스**로 생성하고 `execute()` 메서드를 선언한다.  
4. `<<include>>` 관계는 **필드 선언**으로만 표현한다 (호출 로직은 Diagram에 없으므로 `// TODO`).  
5. `<<extend>>` 관계는 Extension 클래스 내에서 **Base UC를 필드로 참조**하되 확장 조건은 `// TODO`로 남긴다.  
6. 메서드 시그니처의 파라미터·반환 타입은 Diagram에 없으므로 **인자 없음 / void**로 선언한다.

### \[Use Case Diagram 정보\]

#### Actor (Human)

- Buyer (매수자)  
- Seller (매도자)  
- Agent (공인중개사)

#### Use Case (System Boundary: 집피티 시스템)

| 서브시스템 | Use Case | 종류 |
| :---- | :---- | :---- |
| 계정 및 인증 | ValidateUser (사용자 인증 수행) | `<<abstract>>` |
| 계정 및 인증 | RegisterAccount (계정 등록) | Concrete |
| 계정 및 인증 | LoginSystem (시스템 로그인) | Concrete |
| 계정 및 인증 | ManageProfile (프로필 관리) | Concrete |
| 매물 탐색 및 검색 | SearchPropertyOnMap (매물 지도 탐색) | Concrete |
| 매물 탐색 및 검색 | FilterPropertyDetail (매물 상세 검색) | Concrete |
| 역경매 시스템 | RegisterAuction (매물 경매 등록) | Concrete |
| 역경매 시스템 | SubmitBid (입찰 참여) | Concrete |
| 역경매 시스템 | SelectWinner (낙찰자 선정) | Concrete |
| 예약 및 사후 관리 | RequestVisit (방문 예약 신청) | Concrete |
| 예약 및 사후 관리 | ManageVisit (예약 확정 관리) | Concrete |
| 예약 및 사후 관리 | ConfirmVisit (임장 완료 확인) | Concrete |
| 예약 및 사후 관리 | RegisterReview (후기 평판 등록) | Concrete |

#### UC 관계

- `<<include>>` : RegisterAccount → ValidateUser  
- `<<include>>` : LoginSystem    → ValidateUser  
- `<<include>>` : SelectWinner   → ValidateUser  
- `<<include>>` : ManageVisit    → ValidateUser  
- `<<include>>` : ManageVisit    → RequestVisit  
- `<<include>>` : RegisterReview → ConfirmVisit  
- `<<extend>>`  : SelectWinner   → SubmitBid (Base)  
- `<<extend>>`  : FilterPropertyDetail → SearchPropertyOnMap (Base)

#### Actor ↔ Use Case 연결

| Actor | 연결 UC |
| :---- | :---- |
| Buyer | RegisterAccount, LoginSystem, ManageProfile, SearchPropertyOnMap, FilterPropertyDetail, RequestVisit, RegisterReview |
| Seller | RegisterAccount, LoginSystem, ManageProfile, RegisterAuction, SelectWinner |
| Agent | RegisterAccount, LoginSystem, ManageProfile, SubmitBid, ManageVisit, ConfirmVisit |

### \[출력 형식\]

- 생성된 Java 코드만 출력한다.  
- 패키지 선언(`package`)과 `import` 문은 생략한다.  
- 모든 클래스를 하나의 코드 블록에 이어서 출력한다.

---

## 생성 결과 (L1 코드)

// \============================================================

// \[L1\] Actors

// \============================================================

public class Buyer {

    // TODO: Diagram에 속성/메서드 정보 없음

}

public class Seller {

    // TODO: Diagram에 속성/메서드 정보 없음

}

public class Agent {

    // TODO: Diagram에 속성/메서드 정보 없음

}

// \============================================================

// \[L1\] Abstract Use Case

// \============================================================

public abstract class ValidateUser {

    public abstract void execute();

    // TODO: Diagram 수준에서는 인증 로직을 추론할 수 없음

}

// \============================================================

// \[L1\] Concrete Use Cases — 계정 및 인증

// \============================================================

public class RegisterAccount {

    private ValidateUser validateUser;   // \<\<include\>\>

    public void execute() {

        // TODO: Diagram에 호출 순서/상세 로직 없음

    }

}

public class LoginSystem {

    private ValidateUser validateUser;   // \<\<include\>\>

    public void execute() {

        // TODO

    }

}

public class ManageProfile {

    public void execute() {

        // TODO

    }

}

// \============================================================

// \[L1\] Concrete Use Cases — 매물 탐색 및 검색

// \============================================================

public class SearchPropertyOnMap {

    public void execute() {

        // TODO

    }

}

public class FilterPropertyDetail {

    private SearchPropertyOnMap baseUseCase;   // \<\<extend\>\> Base

    public void execute() {

        // TODO: 확장 조건 Diagram에 없음

    }

}

// \============================================================

// \[L1\] Concrete Use Cases — 역경매 시스템

// \============================================================

public class RegisterAuction {

    public void execute() {

        // TODO

    }

}

public class SubmitBid {

    public void execute() {

        // TODO: 입찰 상세 로직은 Diagram 수준에서 추론 불가

    }

}

public class SelectWinner {

    private ValidateUser validateUser;   // \<\<include\>\>

    private SubmitBid baseUseCase;       // \<\<extend\>\> Base

    public void execute() {

        // TODO: 확장 조건 및 낙찰 로직 Diagram에 없음

    }

}

// \============================================================

// \[L1\] Concrete Use Cases — 예약 및 사후 관리

// \============================================================

public class RequestVisit {

    public void execute() {

        // TODO

    }

}

public class ManageVisit {

    private ValidateUser validateUser;   // \<\<include\>\>

    private RequestVisit requestVisit;   // \<\<include\>\>

    public void execute() {

        // TODO

    }

}

public class ConfirmVisit {

    public void execute() {

        // TODO

    }

}

public class RegisterReview {

    private ConfirmVisit confirmVisit;   // \<\<include\>\>

    public void execute() {

        // TODO

    }

}

---

## 생성 결과 관찰

| 항목 | 수치 / 특징 |
| :---- | :---- |
| 생성 클래스 수 | 16개 (Actor 3 \+ Abstract UC 1 \+ Concrete UC 12\) |
| 구현 완료 메서드 수 | 0개 (모든 execute()가 TODO) |
| 파라미터·반환 타입 정보 | 없음 (모두 `void execute()`) |
| 예외 클래스 | 없음 |
| Precondition 가드 | 없음 |
| `// TODO` 주석 수 | 15개 이상 |

**관찰 결과** : Use Case Diagram 수준의 명세는 **클래스 구조와 관계의 뼈대**만 결정하며, 실제 행동을 규정하지 못한다. 결과적으로 생성된 코드는 실행 불가능한 "껍데기" 수준이다. 이는 Step 3-2(L2)의 결과와 대비된다.  
