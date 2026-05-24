# L3+L4 vs L3+L4+SA 비교 분석

## 0. 분석 기준

- L3+L4 코드: `src/main/java/com/zippt/l3l4`
- L3+L4+SA 코드: `src/main/java/com/zippt/l3l4sa`
- SA 문서: `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`
- 추가 명세 유형: 비기능 요구사항(NFR)
- 비교 목적: L3/L4 구조에 성능, 사용성, 관찰가능성, 신뢰성, 테스트 용이성 요구를 추가했을 때 AI 생성 코드가 어떤 구조적 차이를 만드는지 관찰한다.

---

## 1. 요약 결론

L3+L4 단계는 Client/Server, Interface/Control/Entity/Business Logic 분리를 통해 책임 구조를 만들었다. 그러나 조회 성능, 결과 크기 제어, 입력 오류 복구, 처리 결과 추적 같은 품질 조건은 명시되어 있지 않아, 구현은 여전히 단순 전체 순회나 예외 발생 중심으로 남아 있었다.

SA 단계에서는 새 기능을 추가하지 않고 기존 L3/L4 객체 위에 10개의 NFR을 추가했다. 그중 6개는 콘솔 테스트로 직접 확인했다.

핵심 변화:

1. `Property.region` 기반 검색이 전체 순회에서 `propertiesByRegion` 인덱스 조회로 바뀌었다.
2. `Bid.auctionId`, `Bid.agentId` 기반 조회가 전체 Bid 순회에서 식별자 인덱스 조회로 바뀌었다.
3. 검색 결과가 많을 때 필요한 개수만 반환하는 제한 조회가 추가되었다.
4. 콘솔 입력 오류가 즉시 예외로 끝나지 않고, 같은 단계에서 재입력을 받는 `ConsoleInputReader`로 분리되었다.
5. 검색 유스케이스 실행 결과가 `OperationLog`로 기록되어 처리 결과를 추적할 수 있게 되었다.

핵심 메시지:

> L3/L4가 "누가 무엇을 책임지는가"를 정했다면, SA NFR은 "그 책임을 어떤 품질 기준으로 수행해야 하는가"를 구체화했다. 이 차이는 단순 validation보다 조회 구조, 결과 제한, 입력 복구 흐름, 처리 로그처럼 눈에 보이는 코드 구조 변화로 나타났다.

---

## 2. 적용한 NFR 10개

| ID | 분류 | 내용 | 코드 변화 |
|---|---|---|---|
| NFR-P1 | 성능 | 지역 조건 매물 검색은 전체 순회 대신 지역별 조회 구조 사용 | `propertiesByRegion`, `findPropertyIdsByRegion()` |
| NFR-P2 | 성능 | 경매별 입찰 조회는 auctionId 인덱스 사용 | `bidsByAuction`, `findBidsByAuction()` |
| NFR-P3 | 성능 | 중개사별 입찰 이력 조회는 agentId 인덱스 사용 | `bidsByAgent`, `findBidsByAgent()` |
| NFR-P4 | 성능/자원 | 검색 결과가 많을 때 필요한 개수만 반환 | `findPropertyIdsByRegion(region, limit)` |
| NFR-U1 | 사용성 | 잘못된 콘솔 입력은 같은 단계 재입력으로 복구 | `ConsoleInputReader` |
| NFR-U2 | 사용성 | 오류 메시지는 필드명, 오류 원인, 허용 범위를 포함 | `ConsoleInputReader` 메시지 |
| NFR-O1 | 관찰가능성 | 주요 유스케이스 결과를 로그로 추적 | `OperationLog`, `operationLogs()` |
| NFR-R1 | 신뢰성 | 인덱스 조회 결과는 내부 컬렉션을 직접 노출하지 않음 | `new ArrayList<>(...)` 반환 |
| NFR-M1 | 유지보수성 | 기존 L3/L4 패키지 구조를 유지하며 확장 | `l3l4sa` 내부 보강 |
| NFR-T1 | 테스트 용이성 | 동일 데이터셋으로 L3+L4와 SA를 비교 실행 | `SaNfrBenchmark` |

---

## 3. 대표 테스트 6개

| 테스트 | 검증 NFR | 비교 방식 |
|---|---|---|
| TEST-1 매물 지역 검색 | NFR-P1 | L3+L4 전체 properties 순회 vs SA region 인덱스 |
| TEST-2 경매별 입찰 조회 | NFR-P2 | L3+L4 전체 bids 순회 vs SA auctionId 인덱스 |
| TEST-3 중개사별 입찰 조회 | NFR-P3 | L3+L4 전체 bids 순회 vs SA agentId 인덱스 |
| TEST-4 검색 결과 상한 | NFR-P4 | 전체 순회 후 limit vs 인덱스에서 limit |
| TEST-5 처리 로그 | NFR-O1 | 결과 반환만 vs OperationLog 기록 |
| TEST-6 입력 오류 복구 | NFR-U1/U2 | NumberFormatException vs 재입력 복구 |

---

## 4. 코드 구조 변화

### 4.1 매물 검색

L3+L4:

```java
store.properties().stream()
        .filter(property -> command.conditionInput() == null
                || command.conditionInput().region() == null
                || command.conditionInput().region().equals(property.getRegion()))
        .map(property -> property.getPropertyId())
        .toList();
```

L3+L4+SA:

```java
propertiesByRegion.computeIfAbsent(property.getRegion(), ignored -> new ArrayList<>()).add(property);
store.findPropertyIdsByRegion(region);
```

### 4.2 경매/중개사별 입찰 조회

L3+L4:

```java
store.bids().stream()
        .filter(bid -> bid.belongsToAuction(auctionId))
        .toList();
```

L3+L4+SA:

```java
bidsByAuction.computeIfAbsent(bid.getAuctionId(), ignored -> new ArrayList<>()).add(bid);
bidsByAgent.computeIfAbsent(bid.getAgentId(), ignored -> new ArrayList<>()).add(bid);
store.findBidsByAuction(auctionId);
store.findBidsByAgent(agentId);
```

### 4.3 검색 결과 상한

L3+L4:

```java
store.properties().stream()
        .filter(...)
        .map(...)
        .limit(limit)
        .toList();
```

L3+L4+SA:

```java
store.findPropertyIdsByRegion(region, limit);
```

### 4.4 입력 오류 복구

L3+L4:

```text
잘못된 숫자 입력 -> NumberFormatException
```

L3+L4+SA:

```text
잘못된 숫자 입력 -> 오류 원인 안내 -> 같은 단계 재입력 -> 정상 값 수락
```

### 4.5 처리 결과 추적

L3+L4:

```text
검색 결과만 반환
```

L3+L4+SA:

```java
new OperationLog("SEARCH_PROPERTY", buyerId, region, "SUCCESS(...)", elapsedNanos)
```

---

## 5. 비교 테스트 결과

테스트 클래스:

```text
src/main/java/com/zippt/benchmark/SaNfrBenchmark.java
```

실행 예시:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src\main\java
New-Item -ItemType Directory -Force out\javac | Out-Null
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d out\javac $files.FullName
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -Xmx2g -cp out\javac com.zippt.benchmark.SaNfrBenchmark 1000000
```

### 5.1 결과 해석 방법

- 수치는 운영 성능 보장이 아니라 동일 toy dataset에서 명세 차이가 코드 구조와 실행 경로에 만든 차이를 보여주는 실험 결과이다.
- 같은 100만 건이어도 결과 배수는 같을 필요가 없다. 인덱스 조회 후 후처리 비용, 매칭 건수, 결과 변환 여부가 다르기 때문이다.
- 특히 경매별/중개사별 입찰 조회는 인덱스에서 1건 리스트를 바로 복사하므로 매물 검색보다 차이가 크게 나타날 수 있다.

### 5.2 1,000,000건 실행 결과

| 테스트 | L3+L4 | SA | 관찰 |
|---|---:|---:|---|
| TEST-1 매물 지역 검색 | 92.543 ms | 4.860 ms | 약 19.0배 차이 |
| TEST-2 경매별 입찰 조회 | 62.675 ms | 0.042 ms | 약 1506.6배 차이 |
| TEST-3 중개사별 입찰 이력 조회 | 73.229 ms | 0.055 ms | 약 1343.7배 차이 |
| TEST-4 검색 결과 상한 | 6.936 ms | 1.140 ms | 약 6.1배 차이 |
| TEST-5 처리 로그 | 결과만 반환 | `OperationLog` 1건 생성 | 관찰가능성 차이 |
| TEST-6 입력 오류 복구 | `NumberFormatException` | 재입력 후 `3` 수락 | 사용성 차이 |

---

## 6. 발표자료용 핵심 문장

> Data Dictionary 기반 SA는 validation 변화가 중심이라 예시를 그대로 따른 느낌이 강했다. 그래서 본 프로젝트에서는 NFR 기반 SA를 적용하여 성능, 사용성, 관찰가능성 요구가 코드 구조를 어떻게 바꾸는지 관찰했다.

> L3+L4만으로도 `SearchPropertyManager`와 `AuctionQueryControl`은 분리되었지만, 검색과 조회 방식은 전체 순회에 머물렀다.

> 성능 NFR을 추가하자 AI 생성 코드는 `propertiesByRegion`, `bidsByAuction`, `bidsByAgent` 같은 인덱스 구조를 추가했고, 같은 테스트에서 전체 순회와 인덱스 조회의 차이가 콘솔 출력으로 확인되었다.

> 사용성 NFR을 추가하자 잘못된 콘솔 입력이 예외로 종료되는 대신 같은 단계에서 재입력되는 구조가 생겼다.

> 관찰가능성 NFR을 추가하자 유스케이스 결과가 `OperationLog`로 남아, 단순 결과 반환을 넘어 처리 과정을 추적할 수 있게 되었다.

---

## 7. 잔여 한계

- 현재 코드는 외부 DB 없이 in-memory 구조만 사용한다.
- 따라서 성능 결과는 운영 성능 보장이 아니라 코드 생성 결과의 구조적 차이를 보여주는 실험 결과이다.
- 인덱스는 저장 시점에 함께 갱신되지만, 삭제/상태 변경까지 고려한 완전한 인덱스 무결성 구현은 후속 확장 대상이다.
- `ConsoleInputReader`는 scripted input 기반 테스트 가능 구조로 작성했으며, 실제 UI 메뉴 전체에 연결하려면 Client UI 코드 확장이 필요하다.
