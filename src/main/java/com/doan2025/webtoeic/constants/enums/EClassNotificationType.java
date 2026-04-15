package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum EClassNotificationType {
    NOTIFICATION(1), EXERCISE(2), EXAM(3);

    private final Integer value;

    EClassNotificationType(Integer value) {
        this.value = value;
    }

    public static EClassNotificationType fromValue(Integer value) {
        for (EClassNotificationType item : EClassNotificationType.values()) {
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
