package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticOrderResponse {
    private BigDecimal totalOrders;
    private BigDecimal totalPurchases;
    private BigDecimal completedOrders;
    private BigDecimal pendingOrders;
    private BigDecimal cancelledOrders;
}
