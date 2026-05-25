# ZIP-PT AI-Driven 10분 발표자료 생성 가이드

## 1. 발표 핵심 메시지

이번 발표의 중심 메시지는 다음 한 문장이다.

> L2는 유스케이스 흐름을 코드화했지만 동시 요청 상황에서는 check-then-save 경쟁 조건이 남았다. L3/L4는 책임 경계를 `SubmitBidManager`, `SelectWinnerManager`로 구조화했고, SA는 성능과 동시성 품질 기준을 명시하여 단일 JVM 멀티스레드 테스트에서 thread-safe한 핵심 경로를 확인했다.

발표에서는 "전체 시스템이 완전한 운영 수준"이라고 말하지 않는다. 대신 다음 범위로 정확히 말한다.

> 핵심 경쟁 조건인 동시 입찰과 동시 낙찰 경로는 `auctionId` 단위 임계 영역으로 보호되며, 단일 JVM 기준 멀티스레드 테스트에서 thread-safe하게 동작함을 확인했다.

## 2. 10분 발표 구성

| 시간 | 슬라이드 | 내용 |
|---:|---|---|
| 0:00-1:00 | 1. 문제 제기 | AI-Driven 과제의 관찰 질문: 명세가 구체화되면 코드 품질이 실제로 달라지는가 |
| 1:00-2:00 | 2. 비교 단계 | L2, L3/L4, SA의 역할 차이 |
| 2:00-3:00 | 3. L2 baseline | Use Case 흐름 중심 코드, 동시성 요구가 있어도 검증과 저장이 분리될 수 있음 |
| 3:00-4:00 | 4. L3/L4 변화 | Entity/Control/Manager 분리, 동시성 책임 위치가 `SubmitBidManager`, `SelectWinnerManager`로 이동 |
| 4:00-5:30 | 5. SA 구성 | DD 3개, BR 3개, NFR 4개. NFR은 성능 2개 + 동시성 2개 |
| 5:30-7:00 | 6. 성능 결과 | 지역 검색, 경매별 입찰 조회, 중개사별 입찰 조회에서 전체 순회와 인덱스 조회 비교 |
| 7:00-8:30 | 7. 동시성 결과 | L2는 동시 입찰 100건 모두 저장, L3/L4와 SA는 성공 1건/거부 99건 |
| 8:30-9:30 | 8. 해석 | 단순 코드량 증가가 아니라, 명세가 AI의 임의 판단을 줄이고 품질 기준을 코드로 고정 |
| 9:30-10:00 | 9. 한계와 결론 | 단일 JVM 인메모리 기준이며, 실제 운영에는 DB transaction/unique constraint/분산락 등이 추가 필요 |

## 3. 슬라이드별 권장 내용

### Slide 1. 제목

제목:

```text
AI-Driven Specification 수준에 따른 ZIP-PT 코드 품질 변화
```

부제:

```text
L2 -> L3/L4 -> SA: 성능과 동시성 품질 기준이 코드 구조에 미치는 영향
```

### Slide 2. 실험 질문

핵심 질문:

```text
요구사항을 더 구체적으로 주면 AI 생성 코드는 실제로 더 운영 서비스에 가까워지는가?
```

관찰 기준:

- 도메인 개념이 명확해지는가
- 책임 위치가 분리되는가
- 성능 요구가 실행 경로를 바꾸는가
- 동시 요청에서 상태 일관성이 유지되는가

### Slide 3. 단계별 의미

| 단계 | 의미 | ZIP-PT에서의 관찰 |
|---|---|---|
| L2 | Use Case 흐름 코드화 | Submit Bid 절차 중심 |
| L3 | 도메인 개념 고정 | Auction, Bid, Property, Agent 등 Entity 명확화 |
| L4 | 객체 책임/실행 위치 분리 | Client/Server, Control, Manager, DataStore 분리 |
| SA | 값/규칙/품질 기준 보강 | Data Dictionary, Business Rule, Performance, Concurrency |

### Slide 4. L2의 동시성 허점

핵심 설명:

```text
L2에도 동시성 시나리오는 있었지만, 코드 흐름은 hasActiveBid -> save -> auction lock 순서였다.
검증과 저장이 같은 임계 영역에 묶이지 않으면, 여러 스레드가 동시에 hasActiveBid=false를 보고 모두 저장할 수 있다.
```

보여줄 코드 위치:

- `src/main/java/com/zippt/l2/usecase/SubmitBidUseCase.java`
- `src/main/java/com/zippt/l2/model/Auction.java`

### Slide 5. L3/L4의 구조화

핵심 설명:

```text
L3/L4에서는 동시성 처리가 유스케이스 절차 중 일부가 아니라, SubmitBidManager와 SelectWinnerManager의 책임으로 이동했다.
```

보여줄 코드 위치:

- `src/main/java/com/zippt/l3l4/server/service/SubmitBidManager.java`
- `src/main/java/com/zippt/l3l4/server/service/SelectWinnerManager.java`
- `src/main/java/com/zippt/l3l4/server/service/DataStore.java`

### Slide 6. SA 10개 항목

| 유형 | 항목 |
|---|---|
| Data Dictionary | DD-1 경매 마감기한 범위, DD-2 수수료율 범위, DD-3 낙찰 기준 가중치 |
| Business Rule | BR-1 중복 입찰 제한, BR-2 마감 이후 입찰 불가, BR-3 낙찰 1회 제한 |
| NFR Performance | NFR-P1 지역 검색 응답성, NFR-P2 입찰 조회 효율 |
| NFR Concurrency | NFR-C1 동시 입찰 원자성, NFR-C2 동시 낙찰 일관성 |

### Slide 7. 성능 결과

실행:

```text
com.zippt.benchmark.SaNfrBenchmark 100000
```

최근 검증 결과:

| 테스트 | L3+L4 | SA | 요약 |
|---|---:|---:|---|
| 매물 지역 검색 | 26.082 ms | 6.072 ms | 약 4.3배 차이 |
| 경매별 입찰 조회 | 7.247 ms | 0.036 ms | 약 200.2배 차이 |
| 중개사별 입찰 이력 조회 | 18.581 ms | 0.036 ms | 약 521.9배 차이 |
| 검색 결과 상한 | 5.806 ms | 0.846 ms | 약 6.9배 차이 |

해석:

```text
SA는 단순 validation 추가가 아니라, 전체 순회 기반 조회를 인덱스 조회로 바꾸며 실행 경로를 변화시켰다.
```

### Slide 8. 동시성 결과

실행:

```text
com.zippt.benchmark.ConcurrencyBenchmark 100
```

최근 검증 결과:

| 단계 | 성공 | 거부 | 저장된 동일 입찰 | 해석 |
|---|---:|---:|---:|---|
| L2 baseline | 100 | 0 | 100 | check-then-save 경쟁 조건 노출 |
| L3+L4 | 1 | 99 | 1 | Manager 책임과 auctionId 단위 lock으로 중복 저장 방지 |
| L3+L4+SA | 1 | 99 | 1 | 동시 요청 원자성을 품질 기준으로 명시하고 검증 |

강조 문장:

```text
핵심 경쟁 조건인 동시 입찰과 동시 낙찰 경로는 auctionId 단위 임계 영역으로 보호되며, 단일 JVM 기준 멀티스레드 테스트에서 thread-safe하게 동작함을 확인했다.
```

### Slide 9. 결론

결론 문장:

```text
이번 실험에서 명세 수준이 올라갈수록 AI 생성 코드는 단순 기능 흐름에서 벗어나, 도메인 객체, 책임 경계, 값 검증, 성능 인덱스, 동시성 임계 영역을 포함하는 구조로 변화했다.
```

한계:

```text
현재 결과는 단일 JVM 인메모리 기준이다. 실제 운영 서비스에서는 DB transaction, unique constraint, optimistic/pessimistic locking, 분산락, 장애 복구가 추가로 필요하다.
```

## 4. 발표자료 생성용 NotebookLM 프롬프트

아래 프롬프트를 NotebookLM에 넣고, 관련 문서와 코드 파일을 함께 제공한다.

```text
나는 대학원 AI 방법론 과제 발표자료를 만들고 있다. 주제는 ZIP-PT라는 부동산 역경매 플랫폼을 대상으로, AI-Driven COMET UML 단계별 명세 수준이 AI 생성 코드 품질에 어떤 영향을 주는지 분석하는 것이다.

발표 시간은 10분이다. 청중은 교수님과 수강생이며, 핵심은 "요구사항을 더 구체적으로 주었을 때 실제 코드 구조와 품질이 어떻게 달라지는가"를 설득력 있게 보여주는 것이다.

다음 관점을 중심으로 10분 발표자료 초안을 만들어줘.

1. L2는 Use Case Description 중심 baseline이다. Submit Bid 흐름은 구현되어 있지만, 동시 요청 상황에서 hasActiveBid -> save -> auction lock 순서 때문에 check-then-save 경쟁 조건이 남을 수 있다.
2. L3 Static Modeling은 Auction, Bid, Property, Agent, AuctionCondition, BidProposal, WinnerSelectionCriteria 같은 도메인 개념을 Entity로 고정한다.
3. L4 Object Structuring은 Client/Server, Interface, Control, Entity, Business Logic 책임을 분리한다. 특히 SubmitBidManager와 SelectWinnerManager가 입찰/낙찰 책임을 가진다.
4. SA는 L3/L4 구조를 뒤집지 않고 Data Dictionary, Business Rule, NFR을 추가한다. 최종 SA는 DD 3개, BR 3개, NFR 4개로 구성되며, NFR은 성능 2개와 동시성 2개이다.
5. 성능 NFR에서는 전체 순회 기반 조회가 지역/경매/중개사별 인덱스 조회로 바뀌면서 응답 시간이 줄어든다.
6. 동시성 NFR에서는 핵심 경쟁 조건인 동시 입찰과 동시 낙찰 경로가 auctionId 단위 임계 영역으로 보호되며, 단일 JVM 기준 멀티스레드 테스트에서 thread-safe하게 동작함을 확인했다.
7. ConcurrencyBenchmark 100 결과는 L2 baseline이 성공 100건/저장 100건으로 중복 입찰 문제가 드러났고, L3+L4와 L3+L4+SA는 성공 1건/거부 99건/저장 1건으로 일관성을 유지했다.
8. 결론은 "요구사항이 많아져서 코드가 길어진 것"이 아니라, 명세가 AI의 임의 판단을 줄이고 품질 기준을 코드 구조로 고정했다는 점이다.
9. 단, 현재 결과는 단일 JVM 인메모리 기준이므로 실제 운영 서비스에는 DB transaction, unique constraint, 분산락, 장애 복구가 추가로 필요하다는 한계도 포함해줘.

슬라이드는 9장 내외로 구성해줘.
각 슬라이드마다 제목, 핵심 bullet 3개 이내, 발표자 노트 3~5문장, 넣으면 좋은 표/코드/결과를 제안해줘.
톤은 과장된 마케팅이 아니라, 기술 발표처럼 명확하고 방어 가능하게 작성해줘.
```

## 5. 함께 제공하면 좋은 파일

- `docs/ai-driven/ws2/AI_DRIVEN_STAGE_GUIDE.md`
- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`
- `docs/ai-driven/ws2/analysis/L2_vs_L3L4_Comparison_참고용.md`
- `docs/ai-driven/ws2/analysis/L3L4_vs_L3L4SA_Comparison.md`
- `src/main/java/com/zippt/benchmark/SaNfrBenchmark.java`
- `src/main/java/com/zippt/benchmark/ConcurrencyBenchmark.java`
- `src/main/java/com/zippt/l2/usecase/SubmitBidUseCase.java`
- `src/main/java/com/zippt/l3l4/server/service/SubmitBidManager.java`
- `src/main/java/com/zippt/l3l4sa/server/service/SubmitBidManager.java`
