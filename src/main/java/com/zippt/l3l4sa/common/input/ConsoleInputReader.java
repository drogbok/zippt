package com.zippt.l3l4sa.common.input;

import java.util.List;

public class ConsoleInputReader {
    public int readIntWithRetry(String fieldName, List<String> scriptedInputs, int min, int max) {
        for (String rawInput : scriptedInputs) {
            try {
                int value = Integer.parseInt(rawInput);
                if (value < min || value > max) {
                    System.out.println("[SA 입력 복구] " + fieldName + " 범위 오류: " + rawInput
                            + " -> " + min + "~" + max + " 사이 값을 다시 입력받습니다.");
                    continue;
                }
                System.out.println("[SA 입력 복구] " + fieldName + " 정상 입력: " + value);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[SA 입력 복구] " + fieldName + " 숫자 형식 오류: " + rawInput
                        + " -> 같은 단계에서 다시 입력받습니다.");
            }
        }
        throw new IllegalArgumentException(fieldName + " 입력 복구 실패");
    }
}
