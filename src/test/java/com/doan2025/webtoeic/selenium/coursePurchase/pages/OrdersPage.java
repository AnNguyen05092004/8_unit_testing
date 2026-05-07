package com.doan2025.webtoeic.selenium.pages;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object cho màn hình lịch sử đơn hàng và các hành động liên quan đơn.
 */
public class OrdersPage extends BasePage {

    private static final int MAX_MY_COURSES_SCAN_PAGES = 40;

    public record OrderStatistics(int totalOrders, int pendingOrders, int completedOrders, String totalSpent) {
    }

    public OrdersPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /** Navigate to /dashboard/orders then wait for the page to be ready. */
    public void openOrders() {
        // Chủ động điều hướng thay vì phụ thuộc state từ màn trước.
        openPath("/dashboard/orders");
        waitVisible(By.xpath("//h1[contains(normalize-space(),'Lịch sử đơn hàng')]"));
    }

    /** Wait (without navigating) for the orders page to be ready. */
    public void waitOrdersPageReady() {
        // Dùng khi action trước đó đã tự redirect về orders.
        waitUrlContains("/dashboard/orders");
        waitVisible(By.xpath("//h1[contains(normalize-space(),'Lịch sử đơn hàng')]"));
    }

    /** Kiểm tra có order ở trạng thái Đang chờ cho đúng khóa học cần test. */
    public void assertHasPendingOrderForCourse(String title) {
        WebElement pendingOrder = waitVisible(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")] and .//*[contains(normalize-space(),'Đang chờ')]]"));
        if (!pendingOrder.isDisplayed()) {
            throw new AssertionError("Pending order not found for course: " + title);
        }
    }

    public void assertAnyPendingOrderVisible() {
        List<WebElement> pendingOrders = driver.findElements(By.xpath(
            "//div[contains(@class,'order-card')][.//*[contains(normalize-space(),'Đang chờ')]]"));
        Assertions.assertFalse(pendingOrders.isEmpty(),
            "Expected at least one pending order, but none was found");
    }

    public String extractOrderIdByCourseTitle(String title) {
        // Prefer the pending/payable card for this course to avoid matching stale historical orders.
        List<WebElement> pendingCards = driver.findElements(By.xpath(
            "//div[contains(@class,'order-card')]"
                + "[.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]"
                + " and .//*[contains(normalize-space(),'Đang chờ')]"
                + " and .//button[contains(normalize-space(),'Thanh toán')]]"));

        WebElement orderCard;
        if (!pendingCards.isEmpty()) {
            // Ưu tiên card pending hiện tại để lấy orderId đúng ngữ cảnh thanh toán.
            orderCard = pendingCards.get(0);
        } else {
            // Header card có dạng "Đơn hàng #12345" -> cắt để lấy orderId.
            orderCard = waitVisible(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        }

        String header = orderCard.findElement(By.cssSelector(".order-info h4")).getText().trim();
        return header.replace("Đơn hàng #", "").trim();
    }

    public void clickPaymentForCourse(String title) {
        // Click nút Thanh toán của đúng course title.
        click(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]//button[contains(normalize-space(),'Thanh toán')]"));
    }

    /** Assert đơn hàng của khóa học có tồn tại trong danh sách lịch sử đơn. */
    public void assertOrderVisibleByTitle(String title) {
        WebElement card = waitVisible(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        if (!card.getText().contains(title)) {
            throw new AssertionError("Order card does not contain expected title: " + title);
        }
    }

    public int countOrdersByTitle(String title) {
        // Dùng để phát hiện duplicate order trong các test idempotency/back-navigation.
        List<WebElement> cards = driver.findElements(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        return cards.size();
    }

    public void assertOrderHasStatus(String title, String statusLabel) {
        By orderCardsBy = By.xpath(
            "//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]");

        try {
            // Poll cho tới khi ít nhất một card khớp title có status mong đợi.
            wait.until(d -> {
                List<WebElement> cards = d.findElements(orderCardsBy);
                if (cards.isEmpty()) {
                    return false;
                }
                for (WebElement card : cards) {
                    if (card.getText().contains(statusLabel)) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception timeout) {
            // Thu thập text card hiện tại để message lỗi dễ debug nguyên nhân mismatch.
            List<WebElement> cards = driver.findElements(orderCardsBy);
            StringBuilder statuses = new StringBuilder();
            for (WebElement card : cards) {
                if (statuses.length() > 0) {
                    statuses.append(" | ");
                }
                statuses.append(card.getText().replaceAll("\\s+", " ").trim());
            }
            Assertions.fail("Order for '" + title + "' should show status '" + statusLabel
                + "'. Current matching cards: " + statuses);
        }
    }

    public void assertNoPaymentButtonForCourse(String title) {
        List<WebElement> cards = driver.findElements(By.xpath(
            "//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]"));
        Assertions.assertFalse(cards.isEmpty(),
            "Order for '" + title + "' should exist in order history");

        // Lấy card đầu tiên hiện tại; kỳ vọng card completed không còn nút thanh toán.
        WebElement latestCard = cards.get(0);
        List<WebElement> paymentButtons = latestCard.findElements(
            By.xpath(".//button[contains(normalize-space(),'Thanh toán')]"));
        Assertions.assertTrue(paymentButtons.isEmpty(),
            "Latest order card for '" + title + "' should not expose a payment button after completion");
    }

    public void clickCancelOrderForCourse(String title) {
        // Hủy order pending theo title.
        click(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]//button[contains(normalize-space(),'Hủy đơn')]"));
    }

    public void confirmCancelOrder() {
        // Confirm modal hủy đơn (nút dangerous).
        click(By.xpath("//div[contains(@class,'ant-modal')]//button[contains(@class,'ant-btn-dangerous') and contains(normalize-space(),'Hủy đơn hàng')]"));
    }

    public OrderStatistics readStatistics() {
        // Đọc snapshot các card thống kê để so sánh trước/sau checkout.
        int totalOrders = readStatisticValue("Tổng đơn hàng");
        int pendingOrders = readStatisticValue("Đang chờ");
        int completedOrders = readStatisticValue("Hoàn thành");
        String totalSpent = readStatisticText("Tổng chi tiêu");
        return new OrderStatistics(totalOrders, pendingOrders, completedOrders, totalSpent);
    }

    private int readStatisticValue(String title) {
        // Trích số từ text locale (vd: 1.234) -> int.
        String text = readStatisticText(title);
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private String readStatisticText(String title) {
        WebElement statistic = waitVisible(By.xpath("//div[contains(@class,'order-statistics')]//div[contains(@class,'ant-statistic')][.//div[contains(@class,'ant-statistic-title') and contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        return statistic.findElement(By.cssSelector(".ant-statistic-content-value")).getText().trim();
    }

    /**
     * Clicks the "Xem khóa học" button on the order card for the given course title.
     * Navigates to /dashboard/video-courses.
     */
    public void clickViewCourseForOrder(String title) {
        click(By.xpath(
            "//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\""
            + escapeXpath(title) + "\")]]"
            + "//button[contains(normalize-space(),'Xem khóa học')]"));
        waitUrlContains("/dashboard/video-courses");
    }

    /**
     * Asserts the video-courses page shows the purchased course card with the "Đã mua" tag.
     */
    public void assertCourseAppearsInMyCourses(String title) {
        waitVisible(By.xpath("//div[contains(@class,'video-courses-page')]"));
        for (int pageAttempt = 1; pageAttempt <= MAX_MY_COURSES_SCAN_PAGES; pageAttempt++) {
            wait.until(d -> d.findElements(By.cssSelector(".loading-container .ant-spin")).isEmpty());

            By courseCard = By.xpath(
                "//div[contains(@class,'course-card-item')][.//*[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]");
            List<WebElement> matches = driver.findElements(courseCard);
            if (!matches.isEmpty()) {
                WebElement card = matches.get(0);
                boolean hasBoughtTag = !card.findElements(
                    By.xpath(".//*[contains(@class,'purchased-tag') or contains(normalize-space(),'Đã mua')]"))
                    .isEmpty();
                Assertions.assertTrue(hasBoughtTag,
                    "Course '" + title + "' found on video-courses page but 'Đã mua' tag is missing");
                return;
            }

            if (!goToNextMyCoursesPage()) {
                break;
            }
        }

        Assertions.fail("Purchased course not found on my-courses pages: " + title);
    }

    public void assertAnyPurchasedCourseVisibleInMyCourses() {
        waitVisible(By.xpath("//div[contains(@class,'video-courses-page')]"));

        // Quét nhiều trang vì khóa học đã mua có thể không ở trang đầu.
        for (int pageAttempt = 1; pageAttempt <= MAX_MY_COURSES_SCAN_PAGES; pageAttempt++) {
            wait.until(d -> d.findElements(By.cssSelector(".loading-container .ant-spin")).isEmpty());

            List<WebElement> boughtTags = driver.findElements(By.xpath(
                "//div[contains(@class,'course-card-item')]//*[contains(@class,'purchased-tag') or contains(normalize-space(),'Đã mua')]"));
            if (!boughtTags.isEmpty()) {
                return;
            }

            if (!goToNextMyCoursesPage()) {
                break;
            }
        }

        Assertions.fail("No purchased course marker found on my-courses pages");
    }

    private boolean goToNextMyCoursesPage() {
        // Hỗ trợ cả custom selector và selector mặc định của Ant Pagination.
        List<WebElement> nextButtons = driver.findElements(By.cssSelector(".custom-pagination .ant-pagination-next"));
        if (nextButtons.isEmpty()) {
            nextButtons = driver.findElements(By.cssSelector(".ant-pagination-next"));
        }
        if (nextButtons.isEmpty()) {
            return false;
        }

        WebElement next = nextButtons.get(0);
        String className = next.getAttribute("class");
        if (className != null && className.contains("ant-pagination-disabled")) {
            return false;
        }

        String activeBefore = readActivePage();
        next.click();
        sleep();
        // Chờ page number đổi để tránh đọc lại data cũ.
        wait.until(d -> !readActivePage().equals(activeBefore));
        return true;
    }

    private String readActivePage() {
        List<WebElement> activePages = driver.findElements(By.cssSelector(".custom-pagination .ant-pagination-item-active"));
        if (activePages.isEmpty()) {
            activePages = driver.findElements(By.cssSelector(".ant-pagination-item-active"));
        }
        return activePages.isEmpty() ? "unknown" : activePages.get(0).getText().trim();
    }
}
