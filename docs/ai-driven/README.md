# AI-Driven 과제 자료 정리

이 폴더는 AI 방법론 과제 자료와 산출물을 워크시트 단위로 분리해서 보관한다.

## 구조

- `ws1/submitted/`: WS1 제출본과 제출 당시 참고 자료
- `ws1/references/code-snapshots/`: WS1 단계별 생성 코드 스냅샷
- `ws2/inputs/`: WS2 작업에 사용할 L0/L1/L2 입력 명세와 baseline 메모
- `ws2/outputs/`: WS2 제출용 L3 Static Modeling, L4 Object Structuring, SA 문서
- `ws2/analysis/`: L2 vs L3+L4, L3+L4 vs L3+L4+SA 비교 분석 자료
- `ws2/presentation/`: WS2 발표자료

## 코드 위치 기준

- 현재 애플리케이션 원본/기존 코드는 `src/main/java/com/zippt` 아래에 둔다.
- WS2의 비교용 생성 코드는 Java 패키지 단위로 분리한다.
  - `src/main/java/com/zippt/l2`: L2-v2 baseline 코드
  - `src/main/java/com/zippt/l3l4`: L3+L4 명세 기반 생성 코드 예정
  - `src/main/java/com/zippt/l3l4sa`: L3+L4+SA 명세 기반 생성 코드 예정

