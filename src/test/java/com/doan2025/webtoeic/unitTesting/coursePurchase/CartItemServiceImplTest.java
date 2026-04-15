package com.doan2025.webtoeic.unitTesting.coursePurchase;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.CartItem;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.response.CartItemResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.CartItemRepository;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.OrderDetailRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.impl.CartItemServiceImpl;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@gmail.com");

        course = Course.builder()
                .id(10L)
                .title("TOEIC 650+")
                .price(499_000L)
                .build();

        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
    }

    @Test
    void addToCart_TC_UT_CART_001_shouldSaveCartItemWhenInputIsValid() {
        // TC-UT-CART-001: Add to cart thành công khi user/course hợp lệ và chưa tồn tại dữ liệu trùng.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(cartItemRepository.existsByCourseAndUser(course, student)).thenReturn(false);
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);

        cartItemService.addToCart(request, course.getId());

        ArgumentCaptor<CartItem> cartCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartCaptor.capture());
        assertEquals(student, cartCaptor.getValue().getUser());
        assertEquals(course, cartCaptor.getValue().getCourse());
    }

    @Test
    void addToCart_TC_UT_CART_002_shouldThrowWhenItemAlreadyExistsInCart() {
        // TC-UT-CART-002: Không được add trùng cùng course cho cùng user.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(cartItemRepository.existsByCourseAndUser(course, student)).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeFromCart_TC_UT_CART_003_shouldThrowWhenUserTriesToDeleteForeignCartItem() {
        // TC-UT-CART-003: User không có quyền xóa cart item của tài khoản khác.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        User anotherUser = new User();
        anotherUser.setId(99L);

        CartItem foreignItem = new CartItem();
        foreignItem.setId(777L);
        foreignItem.setUser(anotherUser);
        foreignItem.setCourse(course);

        when(cartItemRepository.findById(foreignItem.getId())).thenReturn(Optional.of(foreignItem));

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> cartItemService.removeFromCart(request, foreignItem.getId()));

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        verify(cartItemRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void getInCart_TC_UT_CART_004_shouldMapAllCartItemsToResponse() {
        // TC-UT-CART-004: Danh sách cart trả về phải được convert đầy đủ từng item.
        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setCourse(course);
        item1.setUser(student);

        CartItem item2 = new CartItem();
        item2.setId(2L);
        item2.setCourse(course);
        item2.setUser(student);

        CartItemResponse response1 = new CartItemResponse();
        response1.setId(1L);

        CartItemResponse response2 = new CartItemResponse();
        response2.setId(2L);

        when(cartItemRepository.findByEmailUser(student.getEmail())).thenReturn(List.of(item1, item2));
        when(convertUtil.convertCartItemToDto(request, item1)).thenReturn(response1);
        when(convertUtil.convertCartItemToDto(request, item2)).thenReturn(response2);

        List<CartItemResponse> result = cartItemService.getInCart(request);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(it -> it.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(it -> it.getId().equals(2L)));
        verify(convertUtil).convertCartItemToDto(request, item1);
        verify(convertUtil).convertCartItemToDto(request, item2);
    }

    @Test
    void addToCart_TC_UT_CART_005_shouldThrowWhenCourseAlreadyInAnExistingOrder() {
        // TC-UT-CART-005: Không được add vào cart nếu khóa học đã có trong order chưa hoàn tất.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(cartItemRepository.existsByCourseAndUser(course, student)).thenReturn(false);
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_TC_UT_CART_006_shouldThrowWhenStudentAlreadyEnrolledInCourse() {
        // TC-UT-CART-006: Không được add vào cart nếu đã enroll khóa học đó.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(cartItemRepository.existsByCourseAndUser(course, student)).thenReturn(false);
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(false);
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        assertEquals(ResponseCode.EXISTED, ex.getResponseCode());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeFromCart_TC_UT_CART_007_shouldDeleteCartItemWhenUserIsOwner() {
        // TC-UT-CART-007: Xóa cart item thành công khi user là chủ sở hữu.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        CartItem ownItem = new CartItem();
        ownItem.setId(888L);
        ownItem.setUser(student);
        ownItem.setCourse(course);

        when(cartItemRepository.findById(888L)).thenReturn(Optional.of(ownItem));

        cartItemService.removeFromCart(request, 888L);

        verify(cartItemRepository).deleteById(888L);
    }

    @Test
    void addToCart_TC_UT_CART_008_shouldThrowWhenUserNotFound() {
        // TC-UT-CART-008: Khi user không tồn tại trong hệ thống phải ném exception.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_TC_UT_CART_009_shouldThrowWhenCourseNotFound() {
        // TC-UT-CART-009: Khi khóa học không tồn tại trong hệ thống phải ném exception.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_TC_UT_CART_010_specSaysCartExceptionButCodeThrowsOrderException() {
        // TC-UT-CART-010: Usecase 2.2.3 Exception 2a chỉ định nghĩa MỘT exception: "Khóa học đã có
        // trong giỏ hàng." (CART_ITEM). Nhưng khi course đã có trong orderDetail, code ném
        // ResponseObject.ORDER thay vì ResponseObject.CART_ITEM → SAI spec → TEST NÀY SẼ FAIL.
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(cartItemRepository.existsByCourseAndUser(course, student)).thenReturn(false);
        when(orderDetailRepository.existsByUserAndCourse(student.getEmail(), course.getId())).thenReturn(true);

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> cartItemService.addToCart(request, course.getId()));

        // Spec Exception 2a: "Khóa học đã có trong giỏ hàng" → phải là CART_ITEM
        // Actual: code ném ResponseObject.ORDER → assertEquals dưới đây FAIL
        assertEquals(ResponseObject.CART_ITEM, ex.getResponseObject());
    }
}
