package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassMemberResponse {
    private Long id;
    private String name;
    private Date joinDate;
    private String email;
    private String phone;
    private Long memberId;
    private String address;
    private String status;
    private String roleInClass;
}
