package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.EPaymentMethod;
import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Double totalAmount;
    private EPaymentMethod paymentMethod;
    private String transactionCode;
    private Date createdAt;
    private Date updatedAt;
    private EStatusOrder status;
    private OrderDetailResponse detail;
}
