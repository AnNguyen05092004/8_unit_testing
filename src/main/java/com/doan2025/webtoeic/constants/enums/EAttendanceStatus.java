package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EAttendanceStatus {
    PRESENT(1), ABSENT(2), LATE(3);
    private final Integer value;

    EAttendanceStatus(Integer value) {
        this.value = value;
    }

    public static EAttendanceStatus fromValue(Integer value) {
        for (EAttendanceStatus category : EAttendanceStatus.values()) {
            if (category.getValue() == value) {
                return category;
            }
        }
        throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CATEGORY);
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public String getName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
