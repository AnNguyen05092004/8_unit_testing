package com.doan2025.webtoeic.constants.enums;

import lombok.Getter;
@Getter
public enum ERole {
    MANAGER(1, "MANAGER"),
    CONSULTANT(2, "CONSULTANT"),
    TEACHER(3, "TEACHER"),
    STUDENT(4, "STUDENT");

    private final Integer value;

    private final String code;

    ERole(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public int getValue() {return value;}

    public static ERole fromValue(Integer value) {
        for (ERole role : ERole.values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid ERole value: " + value);
    }


}
