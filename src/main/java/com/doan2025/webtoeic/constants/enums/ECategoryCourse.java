package com.doan2025.webtoeic.constants.enums;

import com.doan2025.webtoeic.exception.WebToeicException;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ECategoryCourse {

    LISTENING(1),
    SPEAKING(2),
    READING(3),
    WRITING(4);
    private final Integer value;

    ECategoryCourse(Integer value) {
        this.value = value;
    }

    public static ECategoryCourse fromValue(Integer value) {
        for (ECategoryCourse category : ECategoryCourse.values()) {
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
