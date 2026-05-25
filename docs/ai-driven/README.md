# AI-Driven 과제 자료 정리

이 폴더는 ZIP-PT AI-Driven 과제의 발표 회차별 산출물과 비교 분석 자료를 보관한다.

## 폴더 구조

- `ws1/submitted/`: 이전 발표 제출본과 참고 자료
- `ws1/references/code-snapshots/`: L1/L2 코드 스냅샷
- `ws2/inputs/`: WS2 작업에 사용한 입력 명세와 baseline 메모
- `ws2/outputs/`: L3 Static Modeling, L4 Object Structuring, SA 문서
- `ws2/analysis/`: L2 vs L3+L4, L3+L4 vs L3+L4+SA 비교 분석
- `ws2/presentation/`: 발표자료 배치용 폴더

## 코드 위치

- `src/main/java/com/zippt/l2`: L2 Use Case Description 기반 baseline 코드
- `src/main/java/com/zippt/l3l4`: L3+L4 명세 기반 생성 코드
- `src/main/java/com/zippt/l3l4sa`: L3+L4+SA 명세 기반 생성 코드
- `src/main/java/com/zippt/benchmark/SaNfrBenchmark.java`: L3+L4와 SA의 NFR 적용 결과를 콘솔에서 비교하는 실행 코드
- `src/main/java/com/zippt/benchmark/ConcurrencyBenchmark.java`: L2, L3+L4, L3+L4+SA의 동시 중복 입찰 처리 결과를 비교하는 실행 코드

## 단계별 의미

- L3 Static Modeling: 도메인에 무엇이 존재하는지 고정한다. Entity, 속성, 관계를 명확히 하여 AI가 임의로 도메인 개념을 만들어내는 범위를 줄인다.
- L4 Object Structuring: 누가 어떤 책임을 갖고 어디에 배치되는지 고정한다. Client/Server, Interface, Control, Entity, Business Logic 경계를 명확히 한다.
- SA Specification Augmentation: L3/L4 구조를 뒤집지 않고, 그 구조 안에서 부족했던 값 범위, 업무 규칙, 품질 기준을 보강한다. 이번 산출물에서는 L2부터 존재한 동시성 시나리오를 SA에서 검증 가능한 품질 요구로 구체화했다.

## 이번 SA의 핵심

이번 SA는 Data Dictionary, Business Rule, 비기능 요구사항(NFR)을 합쳐 총 10개 항목으로 구성한다.

- DD-1 Data Dictionary: 경매 마감기한은 현재 + 1시간 이후, 최대 90일 이내
- DD-2 Data Dictionary: 중개 수수료율은 0~10%, 소수점 2자리 이하
- DD-3 Data Dictionary: 낙찰 기준 가중치는 합계 1.0 및 우선순위별 범위 충족
- BR-1 Business Rule: 동일 경매에 동일 중개사는 활성 입찰 1건만 가능
- BR-2 Business Rule: 마감 이후 입찰 불가
- BR-3 Business Rule: 낙찰은 마감 이후 1회만 가능
- NFR-P1 Performance: 매물 지역 검색을 전체 순회에서 지역 인덱스 조회로 변경
- NFR-P2 Performance: 경매/중개사별 입찰 조회를 전체 순회에서 식별자 인덱스 조회로 변경
- NFR-C1 Concurrency: 동시 입찰 요청에서도 동일 경매/동일 중개사 활성 입찰은 1건만 허용
- NFR-C2 Concurrency: 동시 낙찰 선택 요청에서도 최종 낙찰은 정확히 1회만 성공

요약하면 L3/L4가 구조와 책임을 만든다면, SA는 그 책임을 어떤 품질 기준으로 수행해야 하는지 보여준다.

## 동시성 확장 비교

최종 발표에서 운영 서비스에 가까운 임팩트를 보여주기 위해, L2부터 존재했던 동시 입찰 시나리오를 별도 벤치마크로 비교한다.

```text
java ... com.zippt.benchmark.ConcurrencyBenchmark 100
```

- L2 baseline: 유스케이스 흐름은 있으나 중복 검증과 저장이 같은 임계 영역에 묶이지 않아 동시 요청에서 중복 입찰이 남을 수 있다.
- L3+L4: `SubmitBidManager` 책임으로 `auctionId` 단위 처리 지점이 구조화된다.
- L3+L4+SA: 동시 요청 원자성을 품질 기준으로 명시하고, 성공 1건/거부 N-1건을 검증한다.

## 브랜치 기준

- `codex/l2`: L2 baseline
- `codex/l3-l4`: L3+L4 명세 기반 코드 단계
- `codex/l3-l4-sa`: SA 적용 코드 단계
- `codex/analysis`: 분석 문서, 벤치마크, 발표 준비용 최신 작업 단계

특정 단계 산출물을 볼 때는 해당 브랜치로 이동하고, 최신 발표 준비 자료는 `codex/analysis`를 기준으로 본다.
