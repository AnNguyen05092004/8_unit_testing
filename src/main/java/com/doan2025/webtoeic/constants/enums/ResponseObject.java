package com.doan2025.webtoeic.constants.enums;

import lombok.Getter;

@Getter
public enum ResponseObject {
    ROLE, USER, PASSWORD, TOKEN, POST, CATEGORY, TITLE, CONTENT, PRICE, COURSE, LESSON, CART_ITEM, ORDER,
    ENROLLMENT, CLASS, STATUS, STATISTIC, SCHEDULE, ROOM, ATTENDANCE,
    USERNAME, EMAIL, IDENTITY_NUMBER, CODE, ID, FILE, URL, AMOUNT, PAYMENT, NOTIFICATION, SUBMIT,
    SCORE_SCALE, RANGE_TOPIC, QUESTION, ANSWER, EXPLANATION, BANK, QUIZ,

    REGISTER, LOGIN, LOGOUT, REFRESH_TOKEN, VERIFY;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
