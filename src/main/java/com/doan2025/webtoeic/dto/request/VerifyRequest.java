package com.doan2025.webtoeic.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyRequest {
    private String email;
    private Integer otp;
    private String newPassword;
    private String confirmPassword;
}
