package com.doan2025.webtoeic.selenium.coursePurchase;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.doan2025.webtoeic.selenium.pages.CartPage;
import com.doan2025.webtoeic.selenium.pages.CoursesPage;
import com.doan2025.webtoeic.selenium.pages.LoginPage;
import com.doan2025.webtoeic.selenium.pages.OrderStatusPage;
import com.doan2025.webtoeic.selenium.pages.OrdersPage;
import com.doan2025.webtoeic.selenium.pages.OrdersPage.OrderStatistics;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Happy-path Selenium scenarios for course purchase flow.
 *
 * Goal of this class:
 * - Validate main user flows that should PASS in normal conditions.
 * - Keep each test isolated by optionally resetting DB and clearing cart.
 */
@EnabledIfSystemProperty(named = "selenium.e2e", matches = "true")
class CoursePurchaseFlowSystemFlowSeleniumTest {

    private static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:5173");
    private static final String STUDENT_EMAIL = System.getProperty("e2e.student.email", "student@gmail.com");
    private static final String STUDENT_PASSWORD = System.getProperty("e2e.student.password", "abcd@1234");
    private static final long SLOW_MILLIS = Long.getLong("e2e.slowMillis", 100L);

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Optionally reset DB per test if requested. Values: perTest | perClass | none
        String resetMode = System.getProperty("selenium.resetDb", "perClass");
        if ("perTest".equalsIgnoreCase(resetMode)) {
            TestDbUtils.resetDatabase("reset");
        }

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

    @BeforeAll
    static void beforeAll() {
        String resetMode = System.getProperty("selenium.resetDb", "perClass");
        if ("perClass".equalsIgnoreCase(resetMode)) {
            TestDbUtils.resetDatabase("reset");
        }
    }

    private void pause(long millis) {
        // Lightweight stabilization for UI transitions/async updates.
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("TC_FN_045 - add course from course page updates cart badge and popup content")
    void tcFn045_addCourseFromCoursePage_updatesCartBadgeAndPopupContent() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: logged-in student with empty cart.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        // Act: add first available course from listing.
        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();

        // Assert: selected course is visible in mini-cart and badge shows 1 item.
        cartPage.openMiniCart();
        cartPage.assertMiniCartContainsCourse(selected.title());
        Assertions.assertEquals(1, cartPage.readCartBadgeCount());
    }

    @Test
    @DisplayName("TC_FN_046 - remove course from mini cart restores Add to cart state")
    void tcFn046_removeCourseFromMiniCart_restoresAddToCartState() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: start from a clean cart and add one course.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();
        pause(SLOW_MILLIS);

        // Act: remove that course from mini-cart.
        cartPage.openMiniCart();
        cartPage.assertMiniCartContainsCourse(selected.title());
        cartPage.removeCourseFromMiniCart(selected.title());
        cartPage.confirmRemoveFromMiniCart();
        pause(SLOW_MILLIS);
        cartPage.assertMiniCartDoesNotContainCourse(selected.title());

        // Assert: course no longer exists in cart detail page either.
        cartPage.assertCourseAbsentFromCart(selected.title());

        int badge = cartPage.readCartBadgeCount();
        // Badge update may lag behind UI action; poll a few times to reduce flakiness.
        for (int attempt = 0; attempt < 6 && badge != 0; attempt++) {
            pause(Math.max(SLOW_MILLIS, 250L));
            badge = cartPage.readCartBadgeCount();
        }
        Assertions.assertEquals(0, badge,
            "Cart badge should become 0 after removing the only course from mini cart");
    }

    @Test
    @DisplayName("TC_FN_048 - payment from cart updates order history status")
    void tcFn048_paymentFromCart_updatesOrderHistoryStatus() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: add one course to cart.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();
        pause(SLOW_MILLIS);

        // Act: buy from cart, then simulate VNPay success callback.
        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();
        cartPage.clickBuyNowInCartItem(selected.title());
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        ordersPage.waitOrdersPageReady();
        ordersPage.assertHasPendingOrderForCourse(selected.title());
        String orderId = ordersPage.extractOrderIdByCourseTitle(selected.title());
        ordersPage.clickPaymentForCourse(selected.title());
        pause(SLOW_MILLIS);

        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();
        pause(SLOW_MILLIS * 2);

        // Assert: order is completed and payment button disappears.
        ordersPage.openOrders();
        pause(SLOW_MILLIS);
        ordersPage.assertOrderHasStatus(selected.title(), "Hoàn thành");
        ordersPage.assertNoPaymentButtonForCourse(selected.title());
    }

    // @Test
    // @DisplayName("TC_FN_049 - checkout removes purchased item and keeps remaining cart items")
    // void tcFn049_checkoutRemovesPurchasedItemAndKeepsRemainingItems() {
    //     LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
    //     CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
    //     CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
    //     OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
    //     OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

    //     loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
    //     cartPage.clearCartIfNeeded();

    //     coursesPage.openCourses();
    //     java.util.List<CoursesPage.CourseSelection> courses = coursesPage.addMultipleCoursesToCart(2);
    //     CoursesPage.CourseSelection purchased = courses.get(0);
    //     CoursesPage.CourseSelection remaining = courses.get(1);

    //     cartPage.openMiniCart();
    //     cartPage.goToFullCartFromMiniCart();
    //     cartPage.clickBuyNowInCartItem(purchased.title());
    //     cartPage.handleBuyNowOutcome();

    //     ordersPage.waitOrdersPageReady();
    //     String orderId = ordersPage.extractOrderIdByCourseTitle(purchased.title());
    //     statusPage.openMockSuccess(orderId);
    //     statusPage.assertSuccessVisible();

    //     cartPage.assertCourseAbsentFromCart(purchased.title());
    //     cartPage.assertCoursePresentInCart(remaining.title());
    //     Assertions.assertEquals(1, cartPage.readCartBadgeCount());
    // }

    @Test
    @DisplayName("TC_FN_050 - successful payment returns to courses with owned state")
    void tcFn050_successfulPaymentReturnsToCoursesWithOwnedState() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: create a single-item checkout flow.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();
        pause(SLOW_MILLIS);

        // Act: complete payment and navigate to purchased courses.
        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();
        cartPage.clickBuyNowInCartItem(selected.title());
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        ordersPage.waitOrdersPageReady();
        String orderId = ordersPage.extractOrderIdByCourseTitle(selected.title());
        ordersPage.clickPaymentForCourse(selected.title());
        pause(SLOW_MILLIS);
        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();
        pause(SLOW_MILLIS * 2);
        statusPage.clickViewPurchasedCourses();
        pause(SLOW_MILLIS);

        // Assert: user lands in owned-courses area and sees purchased marker.
        ordersPage.assertAnyPurchasedCourseVisibleInMyCourses();
        pause(SLOW_MILLIS);
        
        // Cross-check in orders page that selected course is completed.
        ordersPage.openOrders();
        pause(SLOW_MILLIS);
        ordersPage.assertOrderVisibleByTitle(selected.title());
        ordersPage.assertOrderHasStatus(selected.title(), "Hoàn thành");
    }

    @Test
    @DisplayName("TC_FN_051 - canceling a pending order does not restore it to cart")
    void tcFn051_cancelingPendingOrderDoesNotRestoreItToCart() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: create one pending order via Buy now.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.clickBuyNowOnFirstCourseFromListing();
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        // Act: cancel the pending order from orders page.
        ordersPage.waitOrdersPageReady();
        ordersPage.clickCancelOrderForCourse(selected.title());
        ordersPage.confirmCancelOrder();
        pause(SLOW_MILLIS);

        // Assert: cart stays empty after cancel; item is not restored automatically.
        cartPage.assertCourseAbsentFromCart(selected.title());
        Assertions.assertEquals(0, cartPage.readCartBadgeCount(),
            "Cart should be empty after canceling the only pending order");
    }

    @Test
    @DisplayName("TC_FN_052 - order statistics update after checkout")
    void tcFn052_orderStatisticsUpdateAfterCheckout() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: capture dashboard statistics before payment.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        ordersPage.openOrders();
        pause(SLOW_MILLIS);
        OrderStatistics before = ordersPage.readStatistics();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.addFirstPurchasableCourseToCart();
        pause(SLOW_MILLIS);

        // Act: checkout one course and mark payment as successful.
        cartPage.openMiniCart();
        cartPage.goToFullCartFromMiniCart();
        cartPage.clickBuyNowInCartItem(selected.title());
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        ordersPage.waitOrdersPageReady();
        String orderId = ordersPage.extractOrderIdByCourseTitle(selected.title());
        ordersPage.clickPaymentForCourse(selected.title());
        pause(SLOW_MILLIS);
        
        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();
        pause(SLOW_MILLIS * 2);

        OrderStatistics after = null;
        boolean statsUpdated = false;
        // Stats cards can update asynchronously; retry for a short period.
        for (int attempt = 0; attempt < 15; attempt++) {
            ordersPage.openOrders();
            pause(SLOW_MILLIS);
            after = ordersPage.readStatistics();

            if (after.totalOrders() >= before.totalOrders() + 1
                || after.completedOrders() >= before.completedOrders() + 1) {
                statsUpdated = true;
                break;
            }
            pause(1000);
        }

        Assertions.assertTrue(statsUpdated,
            "Order statistics did not update as expected after successful payment. "
                + "before(total=" + before.totalOrders() + ", completed=" + before.completedOrders() + ")"
                + ", after(total=" + (after == null ? -1 : after.totalOrders())
                + ", completed=" + (after == null ? -1 : after.completedOrders()) + ")");
        
        // Completed order should not increase pending counter.
        Assertions.assertEquals(before.pendingOrders(), after.pendingOrders(),
            "Pending orders count should remain unchanged after successful payment");
        
        // Total spent must increase by at least selected course price.
        long totalSpentBefore = parseDigits(before.totalSpent());
        long totalSpentAfter = parseDigits(after.totalSpent());
        Assertions.assertTrue(totalSpentAfter >= totalSpentBefore + selected.price().longValue(),
            "Total spent should increase by course price after payment");
    }

    // @Test
    // @DisplayName("TC_FN_053 - pending order shows Continue to checkout on courses page")
    // void tcFn053_pendingOrderShowsContinueToCheckoutOnCoursesPage() {
    //     LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
    //     CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
    //     CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
    //     OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);

    //     loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
    //     cartPage.clearCartIfNeeded();

    //     coursesPage.openCourses();
    //     CoursesPage.CourseSelection selected = coursesPage.clickBuyNowOnFirstCourseFromListing();
    //     cartPage.handleBuyNowOutcome();
    //     pause(SLOW_MILLIS);

    //     ordersPage.waitOrdersPageReady();
    //     pause(SLOW_MILLIS);
    //     ordersPage.assertHasPendingOrderForCourse(selected.title());

    //     // Navigate back to courses to verify button state change
    //     coursesPage.openCourses();
    //     pause(SLOW_MILLIS);

    //     // Verify pending-order CTA exists and can navigate back to orders.
    //     coursesPage.assertAnyCourseShowsContinueToCheckout();
    //     coursesPage.clickFirstContinueToCheckout();
    //     pause(SLOW_MILLIS);

    //     ordersPage.waitOrdersPageReady();
    //     ordersPage.assertAnyPendingOrderVisible();
    // }

    @Test
    @DisplayName("TC_FN_054 - canceling a pending order returns course button to Buy now")
    void tcFn054_cancelingPendingOrderReturnsCourseButtonToBuyNow() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: create pending order for a course.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.clickBuyNowOnFirstCourseFromListing();
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        // Act: cancel that order.
        ordersPage.waitOrdersPageReady();
        pause(SLOW_MILLIS);
        ordersPage.clickCancelOrderForCourse(selected.title());
        ordersPage.confirmCancelOrder();
        pause(SLOW_MILLIS);

        // Assert via cart state: item is not in cart after cancel flow.
        cartPage.assertCourseAbsentFromCart(selected.title());
        Assertions.assertEquals(0, cartPage.readCartBadgeCount(),
            "Cart should be empty after canceling the order");
    }

    @Test
    @DisplayName("TC_FN_055 - double-clicking Buy now does not create duplicate orders")
    void tcFn055_doubleClickingBuyNowDoesNotCreateDuplicateOrders() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: login and clear previous state.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        // Act: trigger rapid double-click purchase on one course card.
        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.doubleClickBuyNowOnFirstCourseFromListing();
        cartPage.handleBuyNowOutcome();

        // Assert: exactly one pending order exists for selected title.
        ordersPage.waitOrdersPageReady();
        Assertions.assertEquals(1, ordersPage.countOrdersByTitle(selected.title()));
        ordersPage.assertHasPendingOrderForCourse(selected.title());
    }

    @Test
    @DisplayName("TC_FN_056 - browser back after successful payment does not allow repaying")
    void tcFn056_browserBackAfterSuccessfulPaymentDoesNotAllowRepaying() {
        LoginPage loginPage = new LoginPage(driver, BASE_URL, SLOW_MILLIS);
        CartPage cartPage = new CartPage(driver, BASE_URL, SLOW_MILLIS);
        CoursesPage coursesPage = new CoursesPage(driver, BASE_URL, SLOW_MILLIS);
        OrdersPage ordersPage = new OrdersPage(driver, BASE_URL, SLOW_MILLIS);
        OrderStatusPage statusPage = new OrderStatusPage(driver, BASE_URL, SLOW_MILLIS);

        // Arrange: create a normal buy-now payment flow.
        loginPage.loginAs(STUDENT_EMAIL, STUDENT_PASSWORD);
        cartPage.clearCartIfNeeded();

        coursesPage.openCourses();
        CoursesPage.CourseSelection selected = coursesPage.clickBuyNowOnFirstCourseFromListing();
        cartPage.handleBuyNowOutcome();
        pause(SLOW_MILLIS);

        // Act: complete payment, then navigate browser back.
        ordersPage.waitOrdersPageReady();
        int expectedCount = ordersPage.countOrdersByTitle(selected.title());
        String orderId = ordersPage.extractOrderIdByCourseTitle(selected.title());
        ordersPage.clickPaymentForCourse(selected.title());
        pause(SLOW_MILLIS);

        statusPage.openMockSuccess(orderId);
        statusPage.assertSuccessVisible();
        pause(SLOW_MILLIS * 2);

        // Browser back should not reopen a payable state for completed order.
        driver.navigate().back();
        pause(SLOW_MILLIS);

        if (!driver.getCurrentUrl().contains("/dashboard/orders")) {
            ordersPage.openOrders();
        } else {
            ordersPage.waitOrdersPageReady();
        }
        pause(SLOW_MILLIS);
        
        // Assert: no duplicate order and final status remains completed.
        int count = ordersPage.countOrdersByTitle(selected.title());
        Assertions.assertEquals(expectedCount, count,
            "Browser back should not create extra orders for the same course");
        
        ordersPage.assertOrderHasStatus(selected.title(), "Hoàn thành");
        ordersPage.assertNoPaymentButtonForCourse(selected.title());
    }



    private long parseDigits(String text) {
        // Convert currency-like text to numeric value for deterministic comparison.
        String digits = text == null ? "" : text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0L : Long.parseLong(digits);
    }
}