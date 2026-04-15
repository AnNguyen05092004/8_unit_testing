package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum EStatusOrder {
    PENDING(1), COMPLETED(2), FAILED(3), CANCELLED(4);
    private final Integer value;

    EStatusOrder(Integer value) {
        this.value = value;
    }

    public static EStatusOrder fromValue(Integer value) {
        for (EStatusOrder item : EStatusOrder.values()) {
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
