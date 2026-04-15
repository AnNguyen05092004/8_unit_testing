package com.doan2025.webtoeic.constants.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EGender {
    MALE(1), FEMALE(2), OTHER(3);

    private final Integer value;

    EGender(Integer value) {
        this.value = value;
    }

    @JsonValue
    public String getName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }

}
