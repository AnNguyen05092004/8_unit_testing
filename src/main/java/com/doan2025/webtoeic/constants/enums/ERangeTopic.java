package com.doan2025.webtoeic.constants.enums;

import lombok.Getter;

@Getter
public enum ERangeTopic {
    VTVF(1L, "Verb Tense / Verb Forms", "Thì động từ / Dạng động từ"),
    SVA(2L, "Subject – Verb Agreement", "Sự hòa hợp giữa chủ ngữ và động từ"),
    NOUN(3L, "Nouns", "Danh từ"),
    PN(4L, "Pronouns", "Đại từ"),
    AA(5L, "Adjectives & Adverbs", "Tính từ và trạng từ"),
    AD(6L, "Articles & Determiners", "Mạo từ và từ hạn định"),
    PP(7L, "Prepositions", "Giới từ"),
    CC(8L, "Conjunctions / Connectors", "Liên từ và từ nối"),
    COMP(9L, "Comparisons", "Cấu trúc so sánh"),
    CS(10L, "Conditional Sentences", "Câu điều kiện"),
    RCEC(11L, "Relative Clauses & Embedded Clauses", "Mệnh đề quan hệ, mệnh đề danh ngữ"),
    PV(12L, "Passive Voice", "Câu bị động"),
    GI(13L, "Gerunds & Infinitives", "Danh động từ (V-ing) và động từ nguyên mẫu (to V)"),
    WF(14L, "Word Forms", "Chọn dạng đúng của từ loại");

    private final Long value;
    private final String title;
    private final String description;

    ERangeTopic(Long value, String title, String description) {
        this.value = value;
        this.title = title;
        this.description = description;
    }

    public static ERangeTopic fromValue(Long value) {
        for (ERangeTopic item : ERangeTopic.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    public long getValue() {
        return value;
    }
}
