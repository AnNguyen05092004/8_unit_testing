package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum EClassStatus {
    PLANNING(1), ONGOING(2), COMPLETED(3), CANCELLED(4);

    private final Integer value;

    EClassStatus(Integer value) {
        this.value = value;
    }

    public static EClassStatus fromValue(Integer value) {
        for (EClassStatus item : EClassStatus.values()) {
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
