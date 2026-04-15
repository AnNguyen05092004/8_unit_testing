package com.doan2025.webtoeic.unitTesting.coursePurchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.doan2025.webtoeic.constants.enums.EPaymentMethod;
import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.domain.CartItem;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.OrderDetail;
import com.doan2025.webtoeic.domain.Orders;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchOrderDto;
import com.doan2025.webtoeic.dto.response.OrderResponse;
import com.doan2025.webtoeic.dto.response.StatisticOrderResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.CartItemRepository;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.OrderDetailRepository;
import com.doan2025.webtoeic.repository.OrderRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.impl.OrderServiceImpl;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@gmail.com");

        course = Course.builder()
                .id(200L)
                .title("TOEIC Intensive")
                .price(699_000L)
                .build();

        lenient().when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        lenient().when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
    }

    @Test
    void createOrderByCartItem_TC_UT_ORDER_001_shouldCreateOrderAndOrderDetailThenDeleteCartItem() {
        // TC-UT-ORDER-001: Tạo đơn từ cart item thành công và phải xóa cart item đã mua.
        CartItem cartItem = new CartItem();
        cartItem.setId(300L);
        cartItem.setUser(student);
        cartItem.setCourse(course);

        Orders savedOrder = Orders.builder()
                .id(500L)
                .user(student)
                .paymentMethod(EPaymentMethod.VN_PAY)
                .status(EStatusOrder.PENDING)
                .totalAmount(course.getPrice())
                .build();

        OrderDetail savedDetail = OrderDetail.builder()
                .id(501L)
                .orders(savedOrder)
                .course(course)
                .priceAtPurchase(course.getPrice())
                .build();

        OrderResponse expected = OrderResponse.builder().id(savedOrder.getId()).build();

        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(savedDetail);
        when(convertUtil.convertOrderToDto(request, savedOrder, savedDetail)).thenReturn(expected);

        OrderResponse actual = orderService.createOrderByCartItem(request, cartItem.getId());

        assertSame(expected, actual);

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(EStatusOrder.PENDING, orderCaptor.getValue().getStatus());
        assertEquals(EPaymentMethod.VN_PAY, orderCaptor.getValue().getPaymentMethod());
        assertEquals(course.getPrice(), orderCaptor.getValue().getTotalAmount());

        verify(cartItemRepository).deleteById(cartItem.getId());
    }

    @Test
    void createOrderByCartItem_TC_UT_ORDER_002_shouldThrowWhenOrderAlreadyExists() {
        // TC-UT-ORDER-002: Nếu đã có order chưa hủy cho khóa học thì không cho tạo lại.
        CartItem cartItem = new CartItem();
        cartItem.setId(301L);
        cartItem.setUser(student);
        cartItem.setCourse(course);

        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> orderService.createOrderByCartItem(request, cartItem.getId()));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).save(any(Orders.class));
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
        verify(cartItemRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void createOrderByCourseID_TC_UT_ORDER_003_shouldCreatePendingOrderForDirectBuy() {
        // TC-UT-ORDER-003: Mua ngay từ course card phải tạo đơn PENDING đúng giá khóa học.
        Orders savedOrder = Orders.builder()
                .id(900L)
                .user(student)
                .paymentMethod(EPaymentMethod.VN_PAY)
                .status(EStatusOrder.PENDING)
                .totalAmount(course.getPrice())
                .build();

        OrderDetail savedDetail = OrderDetail.builder()
                .id(901L)
                .orders(savedOrder)
                .course(course)
                .priceAtPurchase(course.getPrice())
                .build();

        OrderResponse expected = OrderResponse.builder().id(savedOrder.getId()).build();

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(savedDetail);
        when(convertUtil.convertOrderToDto(request, savedOrder, savedDetail)).thenReturn(expected);

        OrderResponse actual = orderService.createOrderByCourseID(request, course.getId());

        assertSame(expected, actual);

        ArgumentCaptor<OrderDetail> detailCaptor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailRepository).save(detailCaptor.capture());
        assertEquals(course, detailCaptor.getValue().getCourse());
        assertEquals(course.getPrice(), detailCaptor.getValue().getPriceAtPurchase());
    }

    @Test
    void createOrderByCourseID_TC_UT_ORDER_004_shouldThrowWhenStudentAlreadyEnrolled() {
        // TC-UT-ORDER-004: Nếu đã enroll khóa học thì không được tạo order mới.
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> orderService.createOrderByCourseID(request, course.getId()));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).save(any(Orders.class));
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
    }

    @Test
    void getStatisticOrder_TC_UT_ORDER_005_shouldReturnCorrectStatistics() {
        // TC-UT-ORDER-005: getStatisticOrder phải gọi đủ các repository và trả đúng số liệu thống kê.
        BigDecimal total      = BigDecimal.valueOf(10);
        BigDecimal cancelled  = BigDecimal.valueOf(2);
        BigDecimal completed  = BigDecimal.valueOf(7);
        BigDecimal pending    = BigDecimal.valueOf(1);
        BigDecimal purchases  = BigDecimal.valueOf(3_000_000);

        when(orderRepository.countOrders(null, student.getEmail())).thenReturn(total);
        when(orderRepository.countOrders(EStatusOrder.CANCELLED, student.getEmail())).thenReturn(cancelled);
        when(orderRepository.countOrders(EStatusOrder.COMPLETED, student.getEmail())).thenReturn(completed);
        when(orderRepository.countOrders(EStatusOrder.PENDING, student.getEmail())).thenReturn(pending);
        when(orderRepository.totalPurchases(student.getEmail())).thenReturn(purchases);

        StatisticOrderResponse result = orderService.getStatisticOrder(request);

        assertEquals(total,     result.getTotalOrders());
        assertEquals(cancelled, result.getCancelledOrders());
        assertEquals(completed, result.getCompletedOrders());
        assertEquals(pending,   result.getPendingOrders());
        assertEquals(purchases, result.getTotalPurchases());
    }

    @Test
    void cancelOrder_TC_UT_ORDER_006_shouldMarkOrderAsCancelled() {
        // TC-UT-ORDER-006: cancelOrder phải cập nhật trạng thái CANCELLED và gọi save.
        Orders pendingOrder = Orders.builder()
                .id(600L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(699_000L)
                .build();
        when(orderRepository.findById(600L)).thenReturn(Optional.of(pendingOrder));

        orderService.cancelOrder(request, List.of(600L));

        assertEquals(EStatusOrder.CANCELLED, pendingOrder.getStatus());
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void cancelOrder_TC_UT_ORDER_007_shouldThrowWhenUserIsNotOrderOwner() {
        // TC-UT-ORDER-007: Student không được hủy đơn hàng của người dùng khác.
        User owner = new User();
        owner.setId(99L);
        owner.setEmail("owner@gmail.com");

        Orders ownerOrder = Orders.builder()
                .id(601L)
                .user(owner)
                .status(EStatusOrder.PENDING)
                .build();
        when(orderRepository.findById(601L)).thenReturn(Optional.of(ownerOrder));

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> orderService.cancelOrder(request, List.of(601L)));

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        verify(orderRepository, never()).save(any(Orders.class));
    }

    @Test
    void createOrderByCartItem_TC_UT_ORDER_008_shouldThrowWhenCartItemUserAlreadyEnrolled() {
        // TC-UT-ORDER-008: Không tạo order từ cart nếu user đã enroll khóa học (nhánh enrollment check).
        CartItem cartItem = new CartItem();
        cartItem.setId(302L);
        cartItem.setUser(student);
        cartItem.setCourse(course);

        when(cartItemRepository.findById(302L)).thenReturn(Optional.of(cartItem));
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> orderService.createOrderByCartItem(request, 302L));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).save(any(Orders.class));
    }

    @Test
    void getOwnOrders_TC_UT_ORDER_009_shouldReturnPageOfOrderResponses() {
        // TC-UT-ORDER-009: getOwnOrders phải convert từng order thành response và trả về PageImpl đúng.
        Orders order = Orders.builder().id(700L).user(student).build();
        OrderDetail detail = OrderDetail.builder().id(701L).orders(order).course(course).build();
        OrderResponse expectedResponse = OrderResponse.builder().id(700L).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Orders> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
        SearchOrderDto dto = new SearchOrderDto();

        when(orderRepository.findOwnOrders(dto, student.getEmail(), pageable)).thenReturn(ordersPage);
        when(orderDetailRepository.findByOrderId(700L)).thenReturn(Optional.of(detail));
        when(convertUtil.convertOrderToDto(request, order, detail)).thenReturn(expectedResponse);

        Page<OrderResponse> result = orderService.getOwnOrders(request, dto, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
    }

    @Test
    void createOrderByCartItem_TC_UT_ORDER_010_shouldThrowWhenCartItemNotFound() {
        // TC-UT-ORDER-010: Khi cart item không tồn tại phải ném exception mà không tạo order.
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class,
                () -> orderService.createOrderByCartItem(request, 999L));

        verify(orderRepository, never()).save(any(Orders.class));
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
    }

    @Test
    void createOrderByCourseID_TC_UT_ORDER_011_shouldThrowWhenCourseNotFound() {
        // TC-UT-ORDER-011: Khi khóa học không tồn tại phải ném exception mà không tạo order.
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class,
                () -> orderService.createOrderByCourseID(request, 999L));

        verify(orderRepository, never()).save(any(Orders.class));
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
    }

    @Test
        void cancelOrder_TC_UT_ORDER_012_specSaysCompletedOrderCannotBeCancelled() {
                // TC-UT-ORDER-012: Theo quy tắc nghiệp vụ (Usecase 2.2.4 Post-condition "Mua khóa học
        // thành công" là trạng thái cuối cùng), đơn hàng COMPLETED đã thanh toán không được phép hủy.
        // Code không kiểm tra trạng thái trong cancelOrder: tiếp tục set CANCELLED và save → SAI spec
        // → TEST NÀY SẼ FAIL.
        Orders completedOrder = Orders.builder()
                .id(700L)
                .user(student)
                .status(EStatusOrder.COMPLETED)
                .totalAmount(699_000L)
                .build();
        when(orderRepository.findById(700L)).thenReturn(Optional.of(completedOrder));

        // Spec: không được hủy đơn đã thanh toán → phải ném exception
        // Actual: code không check trạng thái, đặt CANCELLED và save bình thường → assertThrows FAIL
        assertThrows(WebToeicException.class,
                () -> orderService.cancelOrder(request, List.of(700L)));
    }

    @Test
        void cancelOrder_TC_UT_ORDER_013_specSaysAlreadyCancelledOrderCannotBeCancelledAgain() {
                // TC-UT-ORDER-013: Đơn hàng đã ở trạng thái CANCELLED không thể hủy lần nữa (double-cancel).
        // Code không có guard: vẫn gọi orderRepository.save với trạng thái CANCELLED lần hai → SAI spec
        // → TEST NÀY SẼ FAIL.
        Orders cancelledOrder = Orders.builder()
                .id(701L)
                .user(student)
                .status(EStatusOrder.CANCELLED)
                .totalAmount(699_000L)
                .build();
        when(orderRepository.findById(701L)).thenReturn(Optional.of(cancelledOrder));

        // Spec: không được hủy lại đơn đã hủy → phải ném exception
        // Actual: code cho phép, save CANCELLED lần hai → assertThrows FAIL
        assertThrows(WebToeicException.class,
                () -> orderService.cancelOrder(request, List.of(701L)));
    }
}
