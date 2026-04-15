package com.doan2025.webtoeic.constants.enums;

import lombok.Getter;

@Getter
public enum EScoreScale {
    M1(1L, "Muc 1", 0, 5),
    M2(2L, "Muc 2", 6, 10),
    M3(3L, "Muc 3", 11, 15),
    M4(4L, "Muc 4", 16, 20),
    M5(5L, "Muc 5", 21, 25),
    ;

    private final Long value;
    private final String code;
    private final Integer fromScore;
    private final Integer toScore;

    EScoreScale(Long value, String code, Integer fromScore, Integer toScore) {
        this.value = value;
        this.code = code;
        this.fromScore = fromScore;
        this.toScore = toScore;
    }

    public static EScoreScale fromValue(Long value) {
        for (EScoreScale item : EScoreScale.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    public long getValue() {
        return value;
    }
}
