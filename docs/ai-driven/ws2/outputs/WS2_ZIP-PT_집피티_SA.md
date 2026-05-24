# WS2_ZIP-PT_집피티_SA

## 0. Specification Augmentation 개요

- 대상 시스템: 집피티(ZIP-PT)
- 단계: Part C Specification Augmentation
- 선택한 추가 명세 유형: 비기능 요구사항(NFR)
- 연결 대상:
  - L3 Static Modeling의 `Property`, `Bid`, `Auction`, `Buyer`, `Agent` Entity
  - L4 Object Structuring의 `SearchPropertyManager`, `AuctionQueryControl`, `DataStore`, Client UI 흐름

### 0.1 NFR을 선택한 이유

L3+L4 산출물은 도메인 객체와 Client/Server 책임을 분리했지만, 검색 응답성, 조회 효율, 입력 오류 복구, 처리 결과 추적 같은 품질 조건은 충분히 구체화하지 않았다.

이번 SA는 새 유스케이스를 추가하기보다, 기존 L3+L4 구조 안에서 다음 질문을 관찰한다.

> 비기능 요구사항을 추가하면 AI 생성 코드는 단순 기능 구현을 넘어 저장 구조, 조회 방식, UI 입력 흐름, 처리 로그 구조를 다르게 생성하는가?

---

## 1. 추가 비기능 요구사항 10개

| ID | 분류 | 요구사항 | L3/L4 연결 지점 | 코드 반영 기대 |
|---|---|---|---|---|
| NFR-P1 | 성능 | 지역 조건 매물 검색은 전체 매물 목록을 매번 순회하지 않고 지역별 조회 구조를 사용할 수 있어야 한다. | `Property.region`, `SearchPropertyManager`, `DataStore` | `propertiesByRegion`, `findPropertyIdsByRegion()` |
| NFR-P2 | 성능 | 특정 경매의 입찰 목록은 전체 Bid 목록 순회 없이 `auctionId` 기준으로 조회할 수 있어야 한다. | `Bid.auctionId`, `AuctionQueryControl`, `DataStore` | `bidsByAuction`, `findBidsByAuction()` |
| NFR-P3 | 성능 | 특정 중개사의 입찰 이력은 전체 Bid 목록 순회 없이 `agentId` 기준으로 조회할 수 있어야 한다. | `Bid.agentId`, `DataStore` | `bidsByAgent`, `findBidsByAgent()` |
| NFR-P4 | 성능/자원 | 검색 결과가 많을 때 필요한 개수만 반환할 수 있어야 한다. | `Property.region`, 검색 결과 | `findPropertyIdsByRegion(region, limit)` |
| NFR-U1 | 사용성 | 콘솔 입력에서 잘못된 값이 들어와도 프로그램은 종료되지 않고 같은 단계에서 재입력을 받을 수 있어야 한다. | `client.ui`, `client.control` | `ConsoleInputReader`, 입력 retry 정책 |
| NFR-U2 | 사용성 | 입력 오류 메시지는 어떤 필드가 왜 실패했는지 사용자가 이해할 수 있게 표시되어야 한다. | 콘솔 입력 흐름 | 필드명, 오류 종류, 허용 범위 출력 |
| NFR-O1 | 관찰가능성 | 주요 유스케이스 실행 결과는 작업명, 행위자, 대상, 결과, 소요시간으로 추적할 수 있어야 한다. | `SearchPropertyManager`, `DataStore` | `OperationLog`, `operationLogs()` |
| NFR-R1 | 신뢰성 | 조회 인덱스 결과는 외부 코드가 내부 저장 구조를 직접 변경하지 못하도록 방어적 복사로 반환되어야 한다. | `DataStore` 조회 메서드 | `new ArrayList<>(...)` 반환 |
| NFR-M1 | 유지보수성 | 비기능 보강은 L3/L4의 `client/common/server` 구조를 뒤집지 않고 기존 책임 객체 안에 배치되어야 한다. | 전체 패키지 구조 | `l3l4sa` 내부에 보강 |
| NFR-T1 | 테스트 용이성 | SA 적용 결과는 동일 데이터셋에서 L3+L4와 비교 실행할 수 있어야 한다. | benchmark 실행 코드 | `SaNfrBenchmark` |

---

## 2. 대표 테스트 선정

10개 NFR 중 발표에서 직접 실행 결과로 보여줄 항목은 6개로 선정한다.

| 테스트 | 검증 NFR | 선정 이유 |
|---|---|---|
| TEST-1 | NFR-P1 | 매물 검색 전체 순회와 지역 인덱스 조회 차이가 명확함 |
| TEST-2 | NFR-P2 | 경매별 입찰 조회의 전체 순회 제거가 명확함 |
| TEST-3 | NFR-P3 | 중개사별 입찰 이력 조회도 같은 인덱스 전략으로 개선됨 |
| TEST-4 | NFR-P4 | 결과 상한을 통해 검색 결과 크기 제어를 보여줌 |
| TEST-5 | NFR-O1 | 성능 수치 외에 처리 로그 구조가 생겼음을 보여줌 |
| TEST-6 | NFR-U1/U2 | 입력 오류 복구와 사용자 메시지 개선을 보여줌 |

NFR-R1, NFR-M1, NFR-T1은 코드 구조와 테스트 가능성의 보조 근거로 분석에 포함한다.

---

## 3. L3+L4 대비 기대 변화

### 3.1 매물 검색 응답성

L3+L4 baseline:

```text
store.properties().stream()
    .filter(property -> region.equals(property.getRegion()))
```

SA 적용 후:

```text
propertiesByRegion.computeIfAbsent(region, ...).add(property)
findPropertyIdsByRegion(region)
```

의미:

- L3+L4는 `Property`와 `SearchPropertyManager`를 분리했지만, 검색 방식은 전체 순회에 머문다.
- SA는 "검색 응답성"을 명시하여 `DataStore`가 조회 인덱스를 함께 유지하도록 만든다.

### 3.2 경매/중개사별 입찰 조회 효율

L3+L4 baseline:

```text
store.bids().stream()
    .filter(bid -> bid.belongsToAuction(auctionId))
```

SA 적용 후:

```text
bidsByAuction.computeIfAbsent(auctionId, ...).add(bid)
bidsByAgent.computeIfAbsent(agentId, ...).add(bid)
findBidsByAuction(auctionId)
findBidsByAgent(agentId)
```

의미:

- L3+L4는 `AuctionQueryControl`을 만들었지만, 조회 비용에 대한 정책은 없다.
- SA는 `auctionId`, `agentId`가 단순 속성이 아니라 조회 기준이라는 점을 명시한다.

### 3.3 결과 상한

L3+L4 baseline:

```text
전체 검색 결과를 만든 뒤 필요한 만큼 사용
```

SA 적용 후:

```text
findPropertyIdsByRegion(region, limit)
```

의미:

- 검색 결과가 많아지는 상황에서 결과 크기 제한을 조회 책임에 포함한다.
- 이는 대규모 서비스 성능 보장이 아니라, toy dataset에서도 관찰 가능한 자원 사용 정책이다.

### 3.4 콘솔 입력 오류 복구성

L3+L4 baseline:

```text
Integer.parseInt(input)
// 잘못된 입력이면 NumberFormatException 발생
```

SA 적용 후:

```text
ConsoleInputReader.readIntWithRetry("평점", inputs, 1, 5)
```

의미:

- 새 기능을 추가하는 것이 아니라 기존 콘솔 입력 흐름의 실패 처리를 구체화한다.
- 사용자는 잘못된 값을 입력해도 같은 단계에서 다시 시도할 수 있다.

### 3.5 처리 결과 관찰가능성

L3+L4 baseline:

```text
검색 결과만 반환
```

SA 적용 후:

```text
OperationLog("SEARCH_PROPERTY", buyerId, region, "SUCCESS(...)", elapsedNanos)
```

의미:

- 기능 결과뿐 아니라 어떤 작업이 어떤 대상에 대해 수행되었는지 추적할 수 있다.
- 발표에서는 `operationLogs().size()`와 로그 필드로 SA 적용 여부를 확인할 수 있다.

---

## 4. SA 적용 프롬프트

```text
너는 COMET UML 기반 AI-Driven 개발 방법론의 L3+L4+Specification Augmentation 단계에 따라 Java 코드를 생성한다.

입력 산출물:
1. L3 Static Modeling 문서
2. L4 Object Structuring 문서
3. Specification Augmentation - NFR 문서

요구사항:
- 기존 L3+L4 코드 구조(client/common/server)는 유지한다.
- 새 유스케이스를 추가하지 않고, 기존 검색/조회/입력/처리 추적 흐름에 비기능 요구사항을 반영한다.
- NFR-P1: 지역 조건 매물 검색은 전체 매물 순회 대신 지역별 인덱스 조회를 사용할 수 있게 한다.
- NFR-P2: 경매별 입찰 조회는 auctionId 인덱스 조회를 사용할 수 있게 한다.
- NFR-P3: 중개사별 입찰 이력 조회는 agentId 인덱스 조회를 사용할 수 있게 한다.
- NFR-P4: 검색 결과가 많을 때 필요한 개수만 제한 반환할 수 있게 한다.
- NFR-U1/U2: 콘솔 입력 오류는 프로그램 종료가 아니라 같은 단계 재입력으로 복구하고, 필드명과 허용 범위를 포함한 메시지를 출력한다.
- NFR-O1: 주요 유스케이스 성공/실패 결과는 OperationLog로 기록한다.
- NFR-R1: 조회 결과는 내부 인덱스를 직접 노출하지 않도록 방어적 복사로 반환한다.
- NFR-M1: L3/L4의 client/common/server 구조를 유지한다.
- NFR-T1: L3+L4 baseline과 SA 결과를 같은 데이터셋에서 비교 실행할 수 있는 테스트 코드를 제공한다.
- 외부 DB나 프레임워크를 추가하지 않고 순수 Java in-memory 구조로 차이를 관찰할 수 있게 한다.

관찰 목표:
- L3+L4 코드 대비 전체 순회가 인덱스 조회로 바뀌는지 확인한다.
- 같은 테스트 데이터에서 L3+L4와 SA의 검색/조회 시간이 콘솔에 비교 출력되는지 확인한다.
- 입력 오류 복구와 처리 로그가 코드 구조로 분리되는지 확인한다.
```

---

## 5. 비교 테스트 계획

테스트 실행 클래스:

```text
src/main/java/com/zippt/benchmark/SaNfrBenchmark.java
```

Maven이 없는 환경에서는 JDK 17로 직접 컴파일 후 실행한다.

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src\main\java
New-Item -ItemType Directory -Force out\javac | Out-Null
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d out\javac $files.FullName
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -Xmx2g -cp out\javac com.zippt.benchmark.SaNfrBenchmark 1000000
```

### 5.1 TEST-1 매물 지역 검색

| 항목 | 내용 |
|---|---|
| 목적 | L3+L4 전체 순회와 SA 지역 인덱스 조회 비교 |
| 데이터 | N개 매물 중 마지막 1건만 `TARGET_REGION` |
| L3+L4 | `properties().stream().filter(...)` |
| SA | `findPropertyIdsByRegion(region)` |

### 5.2 TEST-2 경매별 입찰 조회

| 항목 | 내용 |
|---|---|
| 목적 | L3+L4 전체 Bid 순회와 SA auctionId 인덱스 조회 비교 |
| 데이터 | N개 입찰 중 마지막 1건만 `auction-target` |
| L3+L4 | `bids().stream().filter(...)` |
| SA | `findBidsByAuction(auctionId)` |

### 5.3 TEST-3 중개사별 입찰 이력 조회

| 항목 | 내용 |
|---|---|
| 목적 | L3+L4 전체 Bid 순회와 SA agentId 인덱스 조회 비교 |
| 데이터 | N개 입찰 중 마지막 1건만 `agent-target` |
| L3+L4 | `bids().stream().filter(...)` |
| SA | `findBidsByAgent(agentId)` |

### 5.4 TEST-4 검색 결과 상한

| 항목 | 내용 |
|---|---|
| 목적 | 많이 매칭되는 지역에서 필요한 개수만 반환하는지 비교 |
| 데이터 | `REGION_7`에 다수 매칭, limit 20 |
| L3+L4 | 전체 순회 후 limit |
| SA | 지역 인덱스에서 limit 적용 |

### 5.5 TEST-5 처리 로그

| 항목 | 내용 |
|---|---|
| 목적 | 유스케이스 결과가 OperationLog로 기록되는지 확인 |
| 확인값 | operationName, actorId, targetId, result, elapsedNanos |

### 5.6 TEST-6 콘솔 입력 오류 복구

| 항목 | 내용 |
|---|---|
| 목적 | 잘못된 콘솔 입력에 대한 복구 정책 비교 |
| 입력 | `abc`, `0`, `3` |
| L3+L4 | `NumberFormatException` 발생 |
| SA | 오류 메시지 출력 후 같은 단계 재입력, 최종 `3` 수락 |

---

## 6. Part C 분석에서 사용할 관찰 항목

| 관찰 항목 | 측정 방법 |
|---|---|
| 검색 시간 변화 | `SaNfrBenchmark` TEST-1 elapsed time 비교 |
| 입찰 조회 시간 변화 | `SaNfrBenchmark` TEST-2, TEST-3 elapsed time 비교 |
| 결과 상한 반영 여부 | TEST-4 콘솔 메시지와 결과 건수 확인 |
| 입력 오류 복구 여부 | TEST-6 콘솔 메시지와 최종 입력값 확인 |
| 관찰가능성 반영 여부 | TEST-5 `OperationLog` 필드 확인 |
| 구조 유지 여부 | `l3l4sa`가 기존 `client/common/server` 구조 안에서 확장되었는지 확인 |

