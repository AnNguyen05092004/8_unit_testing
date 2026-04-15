package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OverviewResponse {
    private Long total;
    private Long overScore;
    private BigDecimal overScorePercent;
    private Long underScore;
    private BigDecimal underScorePercent;
}
