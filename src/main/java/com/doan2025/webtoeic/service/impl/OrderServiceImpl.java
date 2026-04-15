package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.EPaymentMethod;
import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.SearchOrderDto;
import com.doan2025.webtoeic.dto.response.OrderResponse;
import com.doan2025.webtoeic.dto.response.StatisticOrderResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.OrderService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final JwtUtil jwtUtil;
    private final CartItemRepository cartItemRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ConvertUtil convertUtil;

    @Override
    public StatisticOrderResponse getStatisticOrder(HttpServletRequest request) {
        String email = jwtUtil.getEmailFromToken(request);

        return StatisticOrderResponse.builder()
                .totalOrders(orderRepository.countOrders(null, email))
                .cancelledOrders(orderRepository.countOrders(EStatusOrder.CANCELLED, email))
                .completedOrders(orderRepository.countOrders(EStatusOrder.COMPLETED, email))
                .pendingOrders(orderRepository.countOrders(EStatusOrder.PENDING, email))
                .totalPurchases(orderRepository.totalPurchases(email))
                .build();
    }

    @Override
    public Page<OrderResponse> getOwnOrders(HttpServletRequest request, SearchOrderDto dto, Pageable pageable) {

        String email = jwtUtil.getEmailFromToken(request);

        Page<Orders> orders = orderRepository.findOwnOrders(dto, email, pageable);

        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Orders order : orders) {
            OrderResponse orderResponse = convertUtil.convertOrderToDto(request, order,
                    orderDetailRepository.findByOrderId(order.getId())
                            .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER)));
            orderResponses.add(orderResponse);
        }
        return new PageImpl<>(orderResponses, pageable, orders.getTotalElements());
    }

    @Override
    public void cancelOrder(HttpServletRequest request, List<Long> ids) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        for (Long id : ids) {
            Orders order = orderRepository.findById(id)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.ORDER));
            if (!order.getUser().getEmail().equals(user.getEmail())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
            order.setStatus(EStatusOrder.CANCELLED);
            orderRepository.save(order);
        }
    }

    @Override
    public OrderResponse createOrderByCartItem(HttpServletRequest request, Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.CART_ITEM));
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (orderDetailRepository.existsByUserAndCourse(user.getEmail(), cartItem.getCourse().getId())) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER);
        }
        if (enrollmentRepository.existsByUserAndCourse(user, cartItem.getCourse())) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT);
        }
        Orders order = Orders.builder()
                .paymentMethod(EPaymentMethod.VN_PAY)
                .status(EStatusOrder.PENDING)
                .user(user)
                .totalAmount(cartItem.getCourse().getPrice())
                .build();
        Orders savedOrder = orderRepository.save(order);

        OrderDetail orderDetail = OrderDetail.builder()
                .orders(savedOrder)
                .course(cartItem.getCourse())
                .priceAtPurchase(cartItem.getCourse().getPrice())
                .build();
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        cartItemRepository.deleteById(cartItem.getId());
        return convertUtil.convertOrderToDto(request, savedOrder, savedOrderDetail);
    }

    @Override
    public OrderResponse createOrderByCourseID(HttpServletRequest request, Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));

        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (orderDetailRepository.existsByUserAndCourse(user.getEmail(), course.getId())) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER);
        }
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT);
        }
        Orders order = Orders.builder()
                .paymentMethod(EPaymentMethod.VN_PAY)
                .status(EStatusOrder.PENDING)
                .user(user)
                .totalAmount(course.getPrice())
                .build();
        Orders savedOrder = orderRepository.save(order);

        OrderDetail orderDetail = OrderDetail.builder()
                .orders(savedOrder)
                .course(course)
                .priceAtPurchase(course.getPrice())
                .build();
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);

        return convertUtil.convertOrderToDto(request, savedOrder, savedOrderDetail);
    }
}
