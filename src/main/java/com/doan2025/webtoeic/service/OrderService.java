package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchOrderDto;
import com.doan2025.webtoeic.dto.response.OrderResponse;
import com.doan2025.webtoeic.dto.response.StatisticOrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    StatisticOrderResponse getStatisticOrder(HttpServletRequest request);

    Page<OrderResponse> getOwnOrders(HttpServletRequest request, SearchOrderDto dto, Pageable pageable);

    void cancelOrder(HttpServletRequest request, List<Long> id);

    OrderResponse createOrderByCartItem(HttpServletRequest request, Long id);

    OrderResponse createOrderByCourseID(HttpServletRequest request, Long id);
}
