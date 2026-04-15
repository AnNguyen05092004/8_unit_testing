package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    @Builder.Default
    private int code = ResponseCode.SUCCESS.getCode();
    @Builder.Default
    private String message = ResponseCode.SUCCESS.getMessage();
    private T data;

    public void setMessage(ResponseCode responseCode, ResponseObject responseObject) {
        this.message = responseCode.getMessage().replace("{entity}", responseObject.toString());
    }
    public void setMessage(ResponseCode responseCode) {
        this.message = responseCode.getMessage();
    }
    // Phương thức tĩnh chung để tạo ApiResponse với dữ liệu tùy chọn
    public static <T> ApiResponse<T> of(ResponseCode responseCode, ResponseObject responseObject, T data) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage().replace("{entity}", responseObject.toString()))
                .data(data)
                .build();
    }
}
