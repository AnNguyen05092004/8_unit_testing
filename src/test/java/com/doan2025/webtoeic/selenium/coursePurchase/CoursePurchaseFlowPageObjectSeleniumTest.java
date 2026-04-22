package com.doan2025.webtoeic.selenium;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.doan2025.webtoeic.selenium.pages.CartPage;
import com.doan2025.webtoeic.selenium.pages.CoursesPage;
import com.doan2025.webtoeic.selenium.pages.LoginPage;
import com.doan2025.webtoeic.selenium.pages.OrderStatusPage;
import com.doan2025.webtoeic.selenium.pages.OrdersPage;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * E2E test theo Page Object Model (POM).
 *
 * Mục tiêu nghiệp vụ:
 * 1) Sinh viên mua khóa học từ giỏ hàng.
 * 2) Sau thanh toán thành công, khóa học xuất hiện trong danh sách đã mua.
 * 3) Lịch sử đơn hàng có chứa đơn vừa tạo.
 */
@EnabledIfSystemProperty(named = "selenium.e2e", matches = "true")
class CoursePurchaseFlowPageObjectSeleniumTest {

    private static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:5173");
    private static final String STUDENT_EMAIL = System.getProperty("e2e.student.email", "student@gmail.com");
    private static final String STUDENT_PASSWORD = System.getProperty("e2e.student.password", "abcd@1234");
    private static final long SLOW_MILLIS = Long.getLong("e2e.slowMillis", 500L);

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Cấu hình Chrome cho cả local demo (headed) và CI (headless).
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("e2e.headless", "false"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,900");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @AfterEach
    void tearDown() {
        // Luôn đóng browser để tránh rò rỉ session giữa các test case.
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Page Object E2E: purchase course from cart and verify order history")
    void shouldPurchaseCourseFromCartAndVerifyOrderHistoryWithPageObjects() {
        // TT-SEL-001 (tool report): happy path mua khoa hoc tu gio hang den thanh toan thanh cong.
        // Muc tieu de doi chieu voi tai lieu: don hang tao thanh cong, lich su don hang va khoa hoc da mua duoc dong bo.
        // Khởi tạo các Page Object: mỗi class đại diện cho một màn hình/chức năng.
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // ==== Arrange: đăng nhập và chuẩn hóa trạng thái ban đầu ====
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        // Dọn giỏ trước để test luôn bắt đầu từ trạng thái sạch.
        cartPage.clearCartIfNeeded();

        // Mở trang khóa học và thêm khóa học đầu tiên có thể mua.
        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();

        // Đi theo đúng hành vi người dùng: mở mini cart rồi chuyển sang trang cart đầy đủ.
        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();

        // ==== Assert dữ liệu trong giỏ ====
        // Kiểm tra title/price/total phải khớp dữ liệu khóa học vừa thêm.
        CartPage.CartSnapshot snapshot = cartPage.readCartSnapshot(selected.title());
        Assertions.assertEquals(selected.title(), snapshot.title());
        Assertions.assertEquals(selected.price(), snapshot.price());
        Assertions.assertEquals(selected.price(), snapshot.total());

        // ==== Act: tạo đơn hàng từ giỏ ====
        cartPage.clickBuyNowInCartItem(selected.title());
        // Xử lý cả 2 tình huống: modal thành công hoặc đã tự điều hướng sang orders.
        cartPage.handleBuyNowOutcome();

        // Chờ orders sẵn sàng và xác nhận có đơn ở trạng thái pending cho khóa học này.
        ordersPage.waitOrdersPageReady();
        ordersPage.assertHasPendingOrderForCourse(selected.title());

        // Lấy orderId để mô phỏng callback thanh toán thành công từ cổng VNPay.
        String orderId = ordersPage.extractOrderIdByCourseTitle(selected.title());
        ordersPage.clickPaymentForCourse(selected.title());

        // ==== Act: mô phỏng callback thành công và xác thực sau thanh toán ====
        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();
        // Dừng lại để quan sát trang kết quả thanh toán
        statusPage.pause(SLOW_MILLIS);
        // Bấm "Xem khóa học đã mua" trực tiếp trên trang kết quả thanh toán
        statusPage.clickViewPurchasedCourses();
        // Assert khóa học xuất hiện với tag "Đã mua" -> xác nhận thanh toán có hiệu lực.
        ordersPage.assertCourseAppearsInMyCourses(selected.title());

        // Dừng lại để quan sát trang khóa học đã mua
        ordersPage.pause(SLOW_MILLIS);
        // Kiểm tra lịch sử đơn hàng có đơn vừa mua (truy vết giao dịch).
        ordersPage.openOrders();
        ordersPage.assertOrderVisibleByTitle(selected.title());
    }

    @Test
    @DisplayName("Page Object E2E: open failed payment status page")
    void shouldShowFailedPaymentStatusOnOrderStatusPage() {
        // TT-SEL-002 (tool report): mo trang ket qua thanh toan that bai va xac nhan UI hien thi dung.
        // Muc tieu de doi chieu voi tai lieu: trang hien thong diep that bai ro rang va khong bi vo giao dien.
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        statusPage.openMockFailed();
        statusPage.assertFailedVisible();
    }

    @Test
    @DisplayName("Page Object E2E: remove own cart item from full cart")
    void shouldRemoveOwnCartItemFromFullCart() {
        // TT-SEL-005 (tool report): xoa cart item cua chinh student trong full cart.
        // Muc tieu de doi chieu voi tai lieu: item bien mat, badge ve 0 va tong tien cap nhat ve 0.
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);

        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();

        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();
        CartPage.CartSnapshot snapshot = cartPage.readCartSnapshot(selected.title());
        Assertions.assertEquals(selected.title(), snapshot.title());

        cartPage.clickRemoveInCartItem(selected.title());
        cartPage.confirmRemoveCourse(selected.title());

        cartPage.assertCourseAbsentFromCart(selected.title());
        Assertions.assertEquals(0, cartPage.readCartBadgeCount(),
            "Cart badge should become 0 after removing the only cart item");
        cartPage.assertCartTotalZero();
    }

    @Test
    @DisplayName("Page Object E2E: add course to cart and verify full-cart snapshot")
    void shouldAddCourseToCartAndVerifySnapshot() {
        // TT-SEL-006 (tool report): them khoa hoc vao gio va xac thuc snapshot full cart.
        // Muc tieu de doi chieu voi tai lieu: title, price, total va badge phai khop voi khoa hoc vua them.
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);

        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();

        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();

        CartPage.CartSnapshot snapshot = cartPage.readCartSnapshot(selected.title());
        Assertions.assertEquals(selected.title(), snapshot.title(),
            "Course title in cart should match selected course");
        Assertions.assertEquals(selected.price(), snapshot.price(),
            "Course price in cart should match selected course price");
        Assertions.assertEquals(selected.price(), snapshot.total(),
            "Cart total should equal selected course price for single-item cart");
        Assertions.assertEquals(1, cartPage.readCartBadgeCount(),
            "Cart badge should show exactly one item after first add-to-cart");
    }

}
