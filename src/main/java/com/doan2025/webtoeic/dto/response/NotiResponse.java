package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.ENotiType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotiResponse {
    private Long id;

    private String title;

    private String content;

    private Long objectId;

    private ENotiType notiType;

    private Date createdAt;

    private Boolean isRead;


}
