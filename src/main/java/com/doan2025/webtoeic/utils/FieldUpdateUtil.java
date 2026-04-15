package com.doan2025.webtoeic.utils;

import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FieldUpdateUtil<T> {
    private final Supplier<T> getter;  // Supplier để lấy giá trị
    // Trả về setter để cập nhật trường
    @Getter
    private final Consumer<T> setter;  // Consumer để cập nhật giá trị
    // Lấy giá trị mới của trường
    @Getter
    private final T newValue;          // Giá trị mới

    public FieldUpdateUtil(Supplier<T> getter, Consumer<T> setter, T newValue) {
        this.getter = getter;
        this.setter = setter;
        this.newValue = newValue;
    }

    // Lấy giá trị hiện tại của trường
    public T getCurrentValue() {
        return getter.get();
    }

    // Kiểm tra và cập nhật nếu cần
    public void updateIfNeeded() {
        if (getCurrentValue() == null ? getNewValue() != null : !getCurrentValue().equals(getNewValue())) {
            getSetter().accept(getNewValue());  // Cập nhật trường
        }
    }
}
