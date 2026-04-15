package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

public interface PaymentService {
    PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest request);

    RedirectView handleVNPayReturn(HttpServletRequest request);
}
