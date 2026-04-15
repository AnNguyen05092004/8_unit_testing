package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum EQuizStatus {
    PRIVATE(1), OWNER(2), PUBLIC(3);
    private final Integer value;

    EQuizStatus(Integer value) {
        this.value = value;
    }

    public static EQuizStatus fromValue(Integer value) {
        for (EQuizStatus item : EQuizStatus.values()) {
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
