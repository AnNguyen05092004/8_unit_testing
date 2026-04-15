package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.response.CartItemResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CartItemService {
    void addToCart(HttpServletRequest httpServletRequest, Long idCourse);

    void removeFromCart(HttpServletRequest httpServletRequest, Long idCartItem);

    List<CartItemResponse> getInCart(HttpServletRequest httpServletRequest);
}
