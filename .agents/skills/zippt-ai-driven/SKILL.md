---
name: zippt-ai-driven
description: Use when working on the ZIP-PT AI-Driven COMET UML assignment, especially questions about L2/L3/L4/SA meaning, branch selection, WS2 outputs, NFR-based SA, benchmark evidence, or presentation/report summaries for this repository.
---

# ZIP-PT AI-Driven

## First checks

1. Answer in Korean.
2. Check the current branch with `git status --short --branch`.
3. If the user asks for the latest WS2/SA/presentation view, use `codex/analysis` unless they specify another branch.
4. If the user asks for a stage-specific view, use these branches:
   - `codex/l2`: L2 baseline
   - `codex/l3-l4`: L3+L4 stage
   - `codex/l3-l4-sa`: SA stage
   - `codex/analysis`: latest analysis, docs, benchmark, presentation prep

## Core interpretation

- L3 Static Modeling means "what domain concepts exist": Entity classes, attributes, and relationships.
- L4 Object Structuring means "who owns which responsibility and where it runs": Client/Server, Interface, Control, Entity, Business Logic.
- SA means "what quality/constraint/policy is added without overturning L3/L4": in the current ZIP-PT work, SA combines Data Dictionary, Business Rule, and NFR, with stronger emphasis on performance and concurrency.

## Main reference files

- `docs/ai-driven/README.md`: repository-level AI-Driven map.
- `docs/ai-driven/ws2/AI_DRIVEN_STAGE_GUIDE.md`: concise guide for L3/L4/SA meaning, result summary, branch/file references.
- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_StaticModeling.md`: L3 output.
- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_ObjectStructuring.md`: L4 output.
- `docs/ai-driven/ws2/outputs/WS2_ZIP-PT_집피티_SA.md`: NFR-based SA.
- `docs/ai-driven/ws2/analysis/L2_vs_L3L4_Comparison_참고용.md`: L2 vs L3+L4 comparison.
- `docs/ai-driven/ws2/analysis/L3L4_vs_L3L4SA_Comparison.md`: L3+L4 vs NFR SA comparison.

## Code references

- `src/main/java/com/zippt/l2`: L2 baseline.
- `src/main/java/com/zippt/l3l4`: L3+L4 generated code.
- `src/main/java/com/zippt/l3l4sa`: L3+L4+SA generated code.
- `src/main/java/com/zippt/benchmark/SaNfrBenchmark.java`: console benchmark comparing L3+L4 and SA.
- `src/main/java/com/zippt/benchmark/ConcurrencyBenchmark.java`: console benchmark comparing L2, L3+L4, and SA under concurrent duplicate bid requests.

## Current SA focus

The current SA combines Data Dictionary, Business Rule, and NFR items into 10 augmentation items:

- DD-1: auction deadline range, current time + 1 hour through 90 days.
- DD-2: commission rate range, 0-10% with max scale 2.
- DD-3: winner criteria weights, total 1.0 and priority-specific thresholds.
- BR-1: one active bid per auction and agent.
- BR-2: no bid submission after deadline.
- BR-3: winner selection only after close and only once.
- NFR-P1: property search responsiveness through `propertiesByRegion`.
- NFR-P2: auction/agent bid lookup efficiency through `bidsByAuction` and `bidsByAgent`.
- NFR-C1: concurrent duplicate bid atomicity through `SubmitBidManager` and `DataStore.lockFor()`.
- NFR-C2: concurrent winner selection consistency through `SelectWinnerManager` and `DataStore.lockFor()`.

Use this framing: L2 already had concurrent bidding/winner-selection scenarios; L3/L4 created responsibility structure; SA made value ranges, domain action rules, performance, and concurrency criteria explicit and testable.

## Verification

Maven may not be on PATH. Use JDK 17 directly if needed:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src\main\java
New-Item -ItemType Directory -Force out\javac | Out-Null
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d out\javac $files.FullName
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -Xmx2g -cp out\javac com.zippt.benchmark.SaNfrBenchmark 1000000
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -Xmx2g -cp out\javac com.zippt.benchmark.ConcurrencyBenchmark 100
```

Do not present benchmark numbers as production performance guarantees. Present them as controlled toy-dataset evidence that SA changed code structure and execution path.

For the impact-focused presentation, use this concurrency framing:

- L2 may generate some synchronized code, but the check-then-save duplicate-bid path is still vulnerable when validation and persistence are not in the same critical section.
- L3/L4 moves the responsibility into `SubmitBidManager` and `DataStore.lockFor()`.
- SA makes the quality target explicit: under concurrent duplicate bid requests, exactly one request succeeds and the rest are rejected.
