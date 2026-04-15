package com.doan2025.webtoeic.selenium;

import java.time.Duration;
import java.util.List;

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
 * Regression tests targeting two confirmed bugs found during manual testing:
 *
 * <ul>
 *   <li><b>Bug 1</b> – After buying one course from a multi-item cart via VNPay mock payment,
 *       the purchased course should be removed from the cart. Actual: it remains.</li>
 *   <li><b>Bug 2</b> – When a student has a PENDING order for a course, the course card on
 *       the /courses page should display "Tiếp tục thanh toán" instead of "Mua ngay".
 *       Actual: the button stays as "Mua ngay".</li>
 * </ul>
 *
 * Run command (FE + BE must be running first):
 * <pre>
 *   ./mvnw -Dselenium.e2e=true -De2e.headless=false -De2e.slowMillis=1000 \
 *     -De2e.student.email=student@gmail.com -De2e.student.password=abcd@1234 \
 *     -Dtest=BugRegressionSeleniumTest test
 * </pre>
 */
@EnabledIfSystemProperty(named = "selenium.e2e", matches = "true")
class BugRegressionSeleniumTest {

    private static final String BASE_URL      = System.getProperty("e2e.baseUrl", "http://localhost:5173");
    private static final String STUDENT_EMAIL = System.getProperty("e2e.student.email", "student@gmail.com");
    private static final String STUDENT_PASSWORD = System.getProperty("e2e.student.password", "abcd@1234");
    private static final long SLOW_MILLIS     = Long.getLong("e2e.slowMillis", 600L);

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Cấu hình Chrome cho cả chạy local và CI.
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
        // Đóng browser sau mỗi test để không ảnh hưởng test kế tiếp.
        if (driver != null) {
            driver.quit();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bug 1: Purchased course must be removed from cart after successful payment
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Steps:
     * 1. Add 2 distinct courses to cart.
     * 2. Open the mini-cart drawer (quick-access from header).
     * 3. Click "Mua ngay" for the FIRST course directly inside the mini-cart drawer.
     *    (Drawer closes automatically and a success modal appears.)
     * 4. Dismiss the success modal → navigate to orders.
     * 5. Complete payment via VNPay mock-success callback.
     * 6. Navigate back to cart.
     * Expected: purchased course is gone; the other course remains; badge = 1.
     * Actual (bug): purchased course is still in the cart; clicking "Mua ngay"
     *               again shows "order already exists" error.
     */
    @Test
    @DisplayName("Purchased course must be removed from cart after payment via mini-cart")
    void shouldRemovePurchasedCourseFromCartAfterPayment() {
        // Khởi tạo các page cần dùng cho flow bug 1.
        LoginPage loginPage    = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage      = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage  = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // --- Arrange ---
        // Đăng nhập và xóa giỏ để bảo đảm dữ liệu test không bị nhiễu từ lần chạy trước.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        // Add 2 distinct courses; toBuy is the one we'll purchase via mini-cart
        coursesPage.openCourses();
        List<CoursesPage.CourseSelection> courses = coursesPage.addMultipleCoursesToCart(2);
        CoursesPage.CourseSelection toBuy  = courses.get(0);
        CoursesPage.CourseSelection toKeep = courses.get(1);

        // --- Act: open mini-cart, click "Mua ngay" for the first course ---
        // Đây chính là đường đi gây ra Bug 1 theo mô tả thực tế.
        cartPage.openMiniCart();
        cartPage.clickBuyNowInMiniCartForCourse(toBuy.title());
        // Drawer closes → success modal appears → navigate OK → /dashboard/orders
        cartPage.handleBuyNowOutcome();

        ordersPage.waitOrdersPageReady();
        String orderId = ordersPage.extractOrderIdByCourseTitle(toBuy.title());

        // Complete payment via mock VNPay callback
        // (Mô phỏng backend callback thành công để ổn định test.)
        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();

        // --- Assert ---
        // Bug 1: purchased course must be removed from cart
        cartPage.assertCourseAbsentFromCart(toBuy.title());

        // The other course must still be in cart
        cartPage.assertCoursePresentInCart(toKeep.title());

        // Badge must reflect the 1 remaining item.
        // Nếu badge khác 1 thì giỏ không phản ánh đúng trạng thái sau mua hàng.
        int badgeCount = cartPage.readCartBadgeCount();
        Assertions.assertEquals(1, badgeCount,
            "BUG DETECTED [Bug 1]: cart badge should show 1 remaining item after purchase, but shows " + badgeCount);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bug 2: Course button must change after a pending order is created
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Steps:
     * 1. Click "Mua ngay" directly on a course card on the /courses page.
     *    (This calls purchaseDirectlyService and creates a PENDING order.)
     * 2. Dismiss the success modal / navigate back to /courses.
     * Expected: the course card now shows "Tiếp tục thanh toán" (not "Mua ngay").
     * Actual (bug): the button remains "Mua ngay"; clicking it again shows
     *               "order already exists" error.
     *
     * Note: if the same course already has a pending order from a previous run,
     * the API returns "order already exists" and handleBuyNowOutcome() falls back
     * to navigating to /dashboard/orders — the pending order still exists in DB,
     * so the assertion on the courses page is still valid.
     */
    @Test
    @DisplayName("Course button must change to 'Tiếp tục thanh toán' when pending order exists")
    void shouldShowContinueToCheckoutButtonWhenPendingOrderExists() {
        // Khởi tạo page cho flow bug 2 (không cần orders page ở test này).
        LoginPage loginPage     = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage       = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);

        // --- Arrange ---
        // Đăng nhập và dọn giỏ để loại bỏ side-effect giữa các lần chạy.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        // --- Act: create a PENDING order via the courses listing "Mua ngay" button ---
        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.clickBuyNowOnFirstCourseFromListing();

        // Handle success modal (onOk navigates to /dashboard/orders).
        // If course already had a pending order (re-run), this falls back gracefully.
        cartPage.handleBuyNowOutcome();

        // Điều hướng về danh sách khóa học để kiểm tra trạng thái nút sau khi đã có pending order.
        coursesPage.openCourses();

        // --- Assert ---
        // Kiểm tra chính: phải chuyển thành "Tiếp tục thanh toán".
        // Bug 2 primary: button must now be "Tiếp tục thanh toán", not "Mua ngay"
        coursesPage.assertCourseShowsContinueToCheckout(selected.title());

        // Kiểm tra phụ: nếu vẫn hiện "Mua ngay" thì click sẽ ra lỗi order already exists.
        // Bug 2 secondary: if "Mua ngay" is still visible (bug present), click it
        // and verify that an error toast appears — proving the button is broken
        coursesPage.clickBuyNowIfStillVisibleAndAssertErrorToast(selected.title());
    }
}
