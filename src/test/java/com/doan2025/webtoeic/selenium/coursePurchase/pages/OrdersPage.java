package com.doan2025.webtoeic.selenium.pages;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object cho màn hình lịch sử đơn hàng và các hành động liên quan đơn.
 */
public class OrdersPage extends BasePage {

    public OrdersPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /** Navigate to /dashboard/orders then wait for the page to be ready. */
    public void openOrders() {
        openPath("/dashboard/orders");
        waitVisible(By.xpath("//h1[contains(normalize-space(),'Lịch sử đơn hàng')]"));
    }

    /** Wait (without navigating) for the orders page to be ready. */
    public void waitOrdersPageReady() {
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

    public String extractOrderIdByCourseTitle(String title) {
        // Header card có dạng "Đơn hàng #12345" -> cắt để lấy orderId.
        WebElement orderCard = waitVisible(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        String header = orderCard.findElement(By.cssSelector(".order-info h4")).getText().trim();
        return header.replace("Đơn hàng #", "").trim();
    }

    public void clickPaymentForCourse(String title) {
        click(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]//button[contains(normalize-space(),'Thanh toán')]"));
    }

    /** Assert đơn hàng của khóa học có tồn tại trong danh sách lịch sử đơn. */
    public void assertOrderVisibleByTitle(String title) {
        WebElement card = waitVisible(By.xpath("//div[contains(@class,'order-card')][.//h5[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        if (!card.getText().contains(title)) {
            throw new AssertionError("Order card does not contain expected title: " + title);
        }
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
        // Wait for loading spinner to disappear
        wait.until(d -> d.findElements(By.cssSelector(".loading-container .ant-spin")).isEmpty());
        // Course card with matching title
        By courseCard = By.xpath(
            "//div[contains(@class,'course-card-item')][.//*[contains(normalize-space(),\""
            + escapeXpath(title) + "\")]]");
        WebElement card = waitVisible(courseCard);
        // "Đã mua" tag must be present inside the card
        boolean hasBoughtTag = !card.findElements(
            By.xpath(".//*[contains(@class,'purchased-tag') or contains(normalize-space(),'Đã mua')]"))
            .isEmpty();
        Assertions.assertTrue(hasBoughtTag,
            "Course '" + title + "' found on video-courses page but 'Đã mua' tag is missing");
    }
}
