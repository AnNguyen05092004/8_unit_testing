package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> createVNPayPayment(@RequestParam("orderId") Long orderId, HttpServletRequest request) {
        return ApiResponse.of(ResponseCode.SUCCESS, ResponseObject.ORDER, paymentService.createVNPayPayment(orderId, request));
    }

    @GetMapping("/return")
    public RedirectView handleVNPayReturn(HttpServletRequest request) {
        return paymentService.handleVNPayReturn(request);
    }
}
