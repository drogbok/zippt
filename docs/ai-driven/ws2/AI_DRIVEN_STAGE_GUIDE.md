# ZIP-PT AI-Driven 단계별 해석 가이드

## 1. 한 줄 요약

ZIP-PT 실험은 명세 수준이 올라갈수록 AI 생성 코드가 어떻게 달라지는지 보기 위한 비교이다.

- L2: Use Case Description의 절차가 코드 순서로 반영된다.
- L3: 도메인 개념이 Entity와 속성으로 고정된다.
- L4: 객체 책임과 실행 위치가 Client/Server, Interface, Control, Entity, BL로 분리된다.
- SA: L3/L4가 만든 구조 안에 품질 기준을 추가하여 코드 생성의 불확실성을 줄인다.

## 2. L3가 주로 시사하는 것

L3 Static Modeling의 핵심은 "무엇이 존재하는가"이다.

ZIP-PT에서는 다음 변화가 중요하다.

- `Auction`, `Bid`, `Property`, `Reservation`, `Review` 같은 핵심 Entity가 명시된다.
- `WinnerSelectionCriteria`, `AuctionCondition`, `BidProposal`, `AgentCredential`처럼 Use Case 흐름에 흩어질 수 있는 개념이 독립 객체로 고정된다.
- AI가 단순 파라미터나 임시 DTO로 처리할 수 있었던 도메인 명사가 추적 가능한 코드 단위가 된다.

주요 참고 파일:

- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_StaticModeling.md`
- `src/main/java/com/zippt/l3l4/server/domain`
- `src/main/java/com/zippt/l3l4sa/server/domain`

## 3. L4가 주로 시사하는 것

L4 Object Structuring의 핵심은 "누가 책임지는가"와 "어디에서 실행되는가"이다.

ZIP-PT에서는 다음 변화가 중요하다.

- `client`, `server`, `common` 패키지로 실행 위치가 분리된다.
- `BuyerInterface`, `SellerInterface`, `AgentInterface`는 외부 사용자와 맞닿는 Interface Object 역할을 한다.
- `AuctionClientControl`, `AuctionLifecycleControl`, `AuctionQueryControl`은 흐름과 상태 전이를 제어한다.
- `SubmitBidManager`, `RegisterAuctionManager`, `SelectWinnerManager`는 비즈니스 로직을 담당한다.
- L2의 `SubmitBidUseCase`처럼 하나의 클래스에 인증, 검증, 저장, 알림, 상태 변경이 몰리던 구조가 분산된다.

주요 참고 파일:

- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_ObjectStructuring.md`
- `src/main/java/com/zippt/l3l4/client`
- `src/main/java/com/zippt/l3l4/server/control`
- `src/main/java/com/zippt/l3l4/server/service`

## 4. SA가 주로 시사하는 것

SA Specification Augmentation의 핵심은 "L3/L4 구조를 유지하면서 어떤 품질 기준을 추가할 것인가"이다.

이번 산출물에서는 Data Dictionary 대신 NFR을 선택했다. 이유는 Data Dictionary가 validation 강화로만 보일 수 있어서, 교수님이 보기 원하는 "무엇이 바뀌었는가"를 더 직접적으로 보여주기 위해서이다.

적용한 NFR:

- NFR-P1: 매물 검색 응답성
- NFR-P2: 경매/입찰 조회 효율
- NFR-U1: 콘솔 입력 오류 복구성
- NFR-O1: 처리 결과 관찰가능성

SA는 새 기능을 추가하는 단계가 아니다. 기존 `Property`, `Bid`, `SearchPropertyManager`, `AuctionQueryControl`, `DataStore`가 품질 기준을 반영하도록 구조를 보강하는 단계로 해석한다.

주요 참고 파일:

- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`
- `docs/ai-driven/ws2/analysis/L3L4_vs_L3L4SA_Comparison.md`
- `src/main/java/com/zippt/l3l4sa/server/service/DataStore.java`
- `src/main/java/com/zippt/l3l4sa/server/service/SearchPropertyManager.java`
- `src/main/java/com/zippt/l3l4sa/server/control/AuctionQueryControl.java`
- `src/main/java/com/zippt/l3l4sa/common/input/ConsoleInputReader.java`
- `src/main/java/com/zippt/l3l4sa/server/service/OperationLog.java`

## 5. 결과 요약

L3+L4 baseline은 책임 구조는 분리되었지만, 검색과 조회는 여전히 전체 순회에 가깝다.

SA 적용 후에는 다음 코드 변화가 생겼다.

- `propertiesByRegion` 인덱스 추가
- `bidsByAuction`, `bidsByAgent` 인덱스 추가
- `findPropertyIdsByRegion(region)` 추가
- `findBidsByAuction(auctionId)`, `findBidsByAgent(agentId)` 추가
- `ConsoleInputReader`를 통한 입력 오류 복구
- `OperationLog`를 통한 처리 결과 기록

벤치마크 실행 클래스:

```text
src/main/java/com/zippt/benchmark/SaNfrBenchmark.java
```

검증된 1,000,000건 실행 결과:

| 테스트 | L3+L4 | SA | 요약 |
|---|---:|---:|---|
| 매물 지역 검색 | 61.660 ms | 6.157 ms | 약 10.0배 차이 |
| 경매별 입찰 조회 | 56.431 ms | 0.037 ms | 약 1508.8배 차이 |
| 입력 오류 복구 | 예외 발생 | 재입력 후 복구 | 사용성 차이 확인 |

이 수치는 운영 성능 보장이 아니라, 동일한 toy dataset에서 명세 차이가 코드 구조와 실행 경로에 만든 차이를 보여주는 실험 결과이다.

## 6. 발표용 핵심 문장

> L3는 도메인 개념을 고정하고, L4는 객체 책임과 실행 위치를 고정한다. SA는 그 구조를 다시 설계하는 것이 아니라, 구조 안에서 요구되는 품질 기준을 명시한다.

> Data Dictionary 기반 SA는 validation 강화에 집중되지만, NFR 기반 SA는 검색 인덱스, 입력 복구, 처리 로그처럼 코드 구조 변화가 더 명확하게 관찰된다.

> 따라서 이번 실험의 핵심은 "요구사항을 더 많이 줬더니 코드가 길어졌다"가 아니라, "AI가 임의로 처리하던 품질 판단을 명세가 대체하면서 코드 구조가 달라졌다"는 점이다.

## 7. 브랜치와 사용 기준

- `codex/l2`: L2 baseline만 확인할 때 사용
- `codex/l3-l4`: L3+L4 구조만 확인할 때 사용
- `codex/l3-l4-sa`: SA 적용 코드까지 확인할 때 사용
- `codex/analysis`: 최신 분석 문서와 벤치마크까지 확인할 때 사용

사용자가 별도 브랜치를 지정하지 않고 "최신 기준", "WS2", "SA 결과"라고 말하면 `codex/analysis`를 우선 기준으로 본다.

