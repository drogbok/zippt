# WS2 L2 Baseline Notes

## 기준선 정의

이번 WS2 비교 분석의 L2 baseline은 `src/main/java/com/zippt/l2`에 있는 두 번째 L2 생성 코드로 둔다.

## L2 버전 구분

- L2-v1: 브랜치 `codex-l2-submit-bid`의 커밋 `43b627f`에 기록된 첫 번째 L2 적용본
- L2-v2: 현재 작업트리의 `src/main/java/com/zippt/l2` 패키지에 있는 두 번째 L2 생성 코드

## 비교 기준

L3/L4 이후 코드는 L2-v2와 비교한다. 이후 생성 코드는 다음 위치에 분리해서 둔다.

- L3+L4 코드: `src/main/java/com/zippt/l3l4`
- L3+L4+SA 코드: `src/main/java/com/zippt/l3l4sa`

이렇게 분리하는 이유는 클래스 수, 명세 기반 클래스 수, AI 임의 생성 클래스 수, ECB 분리, Client/Server 분리, 상태 관리, 동시성 처리를 패키지 단위로 직접 비교하기 위해서다.

