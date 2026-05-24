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
- SA means "what quality/constraint/policy is added without overturning L3/L4": in the current ZIP-PT work, SA is NFR-based rather than Data Dictionary-based.

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

## Current SA focus

The current SA is NFR-based:

- NFR-P1: property search responsiveness through `propertiesByRegion`.
- NFR-P2: auction/bid lookup efficiency through `bidsByAuction` and `bidsByAgent`.
- NFR-U1: console input recovery through `ConsoleInputReader`.
- NFR-O1: operation observability through `OperationLog`.

Use this framing: L3/L4 created responsibility structure; SA added quality criteria that changed lookup paths, input recovery, and observability.

## Verification

Maven may not be on PATH. Use JDK 17 directly if needed:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src\main\java
New-Item -ItemType Directory -Force out\javac | Out-Null
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d out\javac $files.FullName
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -Xmx2g -cp out\javac com.zippt.benchmark.SaNfrBenchmark 1000000
```

Do not present benchmark numbers as production performance guarantees. Present them as controlled toy-dataset evidence that SA changed code structure and execution path.

