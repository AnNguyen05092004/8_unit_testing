package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum EScheduleStatus {
    ACTIVE(1), CANCELLED(2);
    private final Integer value;

    EScheduleStatus(Integer value) {
        this.value = value;
    }

    public static EScheduleStatus fromValue(Integer value) {
        for (EScheduleStatus item : EScheduleStatus.values()) {
            if (Objects.equals(item.getValue(), value)) {
                return item;
            }
        }
        throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.STATUS);
    }

    @JsonValue
    public String getName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
