package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.CartItem;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.response.CartItemResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.CartItemService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final OrderDetailRepository orderDetailRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void addToCart(HttpServletRequest httpServletRequest, Long idCourse) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Course course = courseRepository.findById(idCourse)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));
        if (cartItemRepository.existsByCourseAndUser(course, user)) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.CART_ITEM);
        }
        if (orderDetailRepository.existsByUserAndCourse(user.getEmail(), course.getId())) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER);
        }
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT);
        }
        CartItem cartItem = new CartItem();
        cartItem.setCourse(course);
        cartItem.setUser(user);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeFromCart(HttpServletRequest httpServletRequest, Long idCartItem) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        CartItem cartItem = cartItemRepository.findById(idCartItem)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CART_ITEM));
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        cartItemRepository.deleteById(idCartItem);
    }

    @Override
    public List<CartItemResponse> getInCart(HttpServletRequest httpServletRequest) {
        String email = jwtUtil.getEmailFromToken(httpServletRequest);
        List<CartItemResponse> result = new ArrayList<>();
        for (CartItem cartItem : cartItemRepository.findByEmailUser(email)) {
            result.add(convertUtil.convertCartItemToDto(httpServletRequest, cartItem));
        }
        return result;
    }
}
