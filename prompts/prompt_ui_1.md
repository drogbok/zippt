# ZIP-PT GUI 프롬프트 — Java Swing 기반 UI 전환

## 개요

현재 ZIP-PT 프로젝트는 콘솔(텍스트) 기반 UI로 동작하고 있다.
테스트와 시연의 편의를 위해 **Java Swing 기반 GUI**로 전환한다.
외부 프레임워크 없이 **순수 Java(Swing/AWT)** 만 사용한다.

> 기존 `model`, `enums`, `repository`, `service`, `data` 패키지는 그대로 유지하고,
> **`ui` 패키지만 교체·확장**하여 GUI를 구현한다.

---

## 1. 전체 구조

```
com.zippt
├── Main.java              ← GUI 모드로 시작하도록 수정
├── data/                   (기존 유지)
├── enums/                  (기존 유지)
├── model/                  (기존 유지)
├── repository/             (기존 유지)
├── service/                (기존 유지)
└── ui/
    ├── MainFrame.java          ← 전체 윈도우 (JFrame)
    ├── LoginPanel.java         ← 로그인 / 회원가입 화면
    ├── buyer/
    │   ├── BuyerMainPanel.java     ← 매수자 대시보드
    │   ├── PropertySearchPanel.java ← 매물 검색 (필터 + 목록)
    │   ├── PropertyDetailDialog.java ← 매물 상세 팝업
    │   ├── ReservationPanel.java    ← 예약 신청 / 내 예약 목록
    │   └── ReviewPanel.java         ← 후기 작성
    ├── seller/
    │   ├── SellerMainPanel.java     ← 매도자 대시보드
    │   ├── PropertyManagePanel.java ← 매물 등록 / 수정 / 삭제
    │   ├── AuctionManagePanel.java  ← 역경매 생성 / 내 역경매 목록
    │   └── BidReviewDialog.java     ← 입찰 확인 및 낙찰 팝업
    ├── agent/
    │   ├── AgentMainPanel.java      ← 중개사 대시보드
    │   ├── ReservationManagePanel.java ← 예약 확정 / 거절 / 방문완료
    │   ├── AuctionListPanel.java    ← 역경매 목록 조회
    │   └── BidPanel.java            ← 입찰 참여 / 내 입찰 목록
    └── common/
        ├── StyleConstants.java      ← 색상, 폰트, 여백 등 디자인 토큰
        └── ComponentFactory.java    ← 재사용 버튼·테이블·입력 필드 팩토리
```

---

## 2. 디자인 가이드

### 2.1 색상 팔레트

| 용도 | 색상 (Hex) | 설명 |
|------|-----------|------|
| Primary | `#2563EB` | 파란색 계열 — 주요 버튼, 강조 |
| Primary Dark | `#1D4ED8` | 호버·눌림 상태 |
| Secondary | `#F59E0B` | 황금색 — 역경매·낙찰 강조 |
| Background | `#F8FAFC` | 밝은 회색 — 전체 배경 |
| Surface | `#FFFFFF` | 카드·패널 배경 |
| Text Primary | `#1E293B` | 본문 텍스트 |
| Text Secondary | `#64748B` | 보조 텍스트 |
| Success | `#16A34A` | 확정·완료 상태 |
| Danger | `#DC2626` | 거절·삭제 |
| Border | `#E2E8F0` | 테두리·구분선 |

### 2.2 폰트

- 제목: `맑은 고딕`, Bold, 18–22px
- 본문: `맑은 고딕`, Plain, 13–14px
- 버튼: `맑은 고딕`, Bold, 13px

### 2.3 레이아웃 원칙

- `MainFrame`은 `CardLayout`으로 화면 전환 (로그인 ↔ 역할별 대시보드)
- 각 대시보드는 **좌측 사이드바 내비게이션 + 우측 콘텐츠 영역** 구조
- 사이드바: 역할 아이콘(텍스트 대체 가능), 메뉴 버튼 목록, 하단에 로그아웃
- 콘텐츠 영역: `CardLayout`으로 메뉴별 패널 전환
- 테이블은 `JTable` + `JScrollPane`, 줄무늬 배경(zebra striping) 적용
- 입력 폼은 `GridBagLayout` 또는 `GroupLayout`으로 라벨-필드 정렬
- 모든 패널에 적절한 `EmptyBorder` 여백 (최소 16px)

---

## 3. 화면별 상세 요구사항

### 3.1 로그인·회원가입 (`LoginPanel`)

- 중앙 정렬 카드 형태
- 상단: ZIP-PT 로고 텍스트 (큰 글씨 + 서브 타이틀 "부동산 매칭 플랫폼")
- **로그인 탭**: 아이디, 비밀번호 입력 → 로그인 버튼
- **회원가입 탭**: 아이디, 비밀번호, 이름, 전화번호, 역할(콤보박스: 매수자/매도자/중개사) 입력
  - 역할이 중개사일 때만 "담당 지역" 필드 표시
- 로그인 실패 시 빨간색 에러 라벨 표시 (다이얼로그 대신)
- 회원가입 성공 시 자동으로 로그인 탭으로 전환 + 성공 메시지

### 3.2 매수자 화면

#### 대시보드 (`BuyerMainPanel`)
- 사이드바 메뉴: 매물 검색 | 내 예약 | 후기 작성
- 상단 환영 메시지: "{이름}님 환영합니다 (매수자)"

#### 매물 검색 (`PropertySearchPanel`)
- 상단 필터 바: 지역(텍스트), 최소~최대 가격, 최소~최대 면적, 유형(콤보박스), 검색 버튼, 초기화 버튼
- 하단 결과 테이블: ID, 주소, 지역, 면적(㎡), 가격(만원), 유형
- 행 더블클릭 또는 "상세" 버튼 → `PropertyDetailDialog` 팝업

#### 매물 상세 (`PropertyDetailDialog`)
- 모달 다이얼로그
- 매물 정보 전체 표시 (주소, 지역, 면적, 가격, 유형, 설명, 등록일, 매도자명)
- 하단: "방문 예약 신청" 버튼 → 예약 폼으로 이동하거나 인라인 입력
  - 예약 시 중개사 선택(콤보박스: 해당 지역 중개사 목록) + 예약 일시(날짜·시간)

#### 내 예약 (`ReservationPanel`)
- 테이블: 예약 ID, 매물 주소, 중개사명, 예약 일시, 상태(색상 배지)
- 상태 배지 색상: 대기(회색), 확정(파랑), 거절(빨강), 방문완료(초록), 후기완료(금색)

#### 후기 작성 (`ReviewPanel`)
- "방문 완료" 상태인 예약만 목록에 표시
- 선택 후 별점(1~5, 라디오 또는 콤보박스) + 후기 내용(JTextArea) 입력
- 작성 완료 시 목록에서 제거되고 성공 메시지 표시

### 3.3 매도자 화면

#### 대시보드 (`SellerMainPanel`)
- 사이드바 메뉴: 매물 관리 | 역경매 관리

#### 매물 관리 (`PropertyManagePanel`)
- 상단: "매물 등록" 버튼
- 테이블: 내 매물 목록 (ID, 주소, 지역, 면적, 가격, 유형, 등록일)
- 행 선택 후 "수정" / "삭제" 버튼
- 등록·수정: 다이얼로그 폼 (주소, 지역, 면적, 가격, 유형 콤보박스, 설명)
- 삭제: 확인 다이얼로그 후 삭제

#### 역경매 관리 (`AuctionManagePanel`)
- 상단: "역경매 생성" 버튼 → 매물 선택(콤보박스: 내 매물) + 요구사항 입력
- 테이블: 내 역경매 목록 (ID, 매물 주소, 상태, 입찰 수, 생성일)
- 행 선택 후 "입찰 확인" 버튼 → `BidReviewDialog`
- `BidReviewDialog`: 해당 역경매의 입찰 목록 테이블 (중개사명, 수수료율, 조건) + "낙찰" 버튼

### 3.4 중개사 화면

#### 대시보드 (`AgentMainPanel`)
- 사이드바 메뉴: 예약 관리 | 역경매 조회 | 내 입찰

#### 예약 관리 (`ReservationManagePanel`)
- 테이블: 나에게 배정된 예약 목록 (ID, 매수자명, 매물 주소, 예약 일시, 상태)
- 상태별 액션 버튼:
  - 대기(PENDING) → "확정" / "거절"
  - 확정(CONFIRMED) → "방문 완료"
- 버튼 클릭 즉시 상태 갱신 + 테이블 리프레시

#### 역경매 조회 (`AuctionListPanel`)
- 진행 중(OPEN/ACTIVE) 역경매 테이블: ID, 매물 주소, 매도자명, 상태, 요구사항
- 행 선택 후 "입찰 참여" → 수수료율(%) 입력 + 조건(텍스트) 입력 다이얼로그

#### 내 입찰 (`BidPanel`)
- 내가 참여한 입찰 테이블: 역경매 ID, 매물 주소, 수수료율, 조건, 역경매 상태

---

## 4. 공통 UX 요구사항

- 모든 입력 검증은 클라이언트 측에서 수행하고, 오류 시 해당 필드 옆 또는 하단에 빨간색 메시지 표시
- 성공 알림은 상단 또는 하단에 초록색 토스트/라벨로 2~3초 표시 후 사라짐 (또는 간단한 JOptionPane 허용)
- 테이블 데이터는 해당 패널이 포커스될 때마다 자동 갱신
- 윈도우 크기: 기본 1100×750, 최소 900×600, 리사이즈 가능
- 닫기(X) 버튼 클릭 시 확인 다이얼로그 후 종료

---

## 5. 기술 제약

- **순수 Java**: Swing + AWT만 사용 (JavaFX, 외부 라이브러리 금지)
- **JDK 17** 호환
- 기존 `service`, `repository`, `model`, `enums`, `data` 패키지는 수정하지 않음
- `ConsoleUI`, `BuyerMenu`, `SellerMenu`, `AgentMenu`는 삭제하지 않고 보존 (GUI와 병행 가능하도록)
- `Main.java`에서 GUI 모드로 시작하도록 수정 (기존 콘솔 모드는 `--console` 인자로 유지 가능)
- 모든 서비스 호출은 **EDT(Event Dispatch Thread)** 에서 안전하게 처리

---

## 6. 구현 순서 (권장)

1. `StyleConstants`, `ComponentFactory` — 디자인 토큰·공통 컴포넌트
2. `MainFrame` + `LoginPanel` — 윈도우 셸과 로그인 흐름
3. `BuyerMainPanel` + 하위 패널 — 매수자 기능
4. `SellerMainPanel` + 하위 패널 — 매도자 기능
5. `AgentMainPanel` + 하위 패널 — 중개사 기능
6. 통합 테스트 — Mock 데이터로 전체 흐름 검증
