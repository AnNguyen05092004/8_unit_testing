package com.doan2025.webtoeic.selenium.coursePurchase.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object cho trang kết quả thanh toán (/order-status).
 *
 * Dùng để mô phỏng callback thành công/thất bại và xác nhận UI kết quả.
 */
public class OrderStatusPage extends BasePage {

    public OrderStatusPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /**
     * Mô phỏng callback thanh toán thành công bằng query params.
     * orderId (txnRef) được lấy từ màn hình orders trước đó.
     */
    public void openMockSuccess(String orderId) {
        // Dùng endpoint UI /order-status để giả lập redirect từ cổng thanh toán.
        openPath("/order-status?status=success&txnRef=demo_" + orderId + "&transactionNo=123456789&amount=10000000&payDate=20260330121010");
    }

    /** Mô phỏng callback thanh toán thất bại (dùng cho negative test). */
    public void openMockFailed() {
        openPath("/order-status?status=failed&txnRef=demo_99999&transactionNo=0&amount=10000000&payDate=20260330121010");
    }

    /** Assert trạng thái thanh toán thành công hiển thị đúng. */
    public void assertSuccessVisible() {
        // Khẳng định trang kết quả đang ở success-state.
        waitVisible(By.xpath("//div[contains(@class,'ant-result-title') and contains(normalize-space(),'Thanh toán thành công')]"));
    }

    /** Assert trạng thái thanh toán thất bại hiển thị đúng. */
    public void assertFailedVisible() {
        // Kiểm tra cả tiêu đề và badge text thất bại để tránh false-positive.
        waitVisible(By.xpath("//div[contains(@class,'ant-result-title') and contains(normalize-space(),'Thanh toán thất bại')]"));
        waitVisible(By.xpath("//span[contains(normalize-space(),'Thất bại')]"));
    }

    /**
     * Clicks "Xem khóa học đã mua" on the success result page and waits for
     * the browser to land on /dashboard/video-courses.
     */
    public void clickViewPurchasedCourses() {
        // Từ trang kết quả thanh toán, đi sang trang "khóa học đã mua" theo luồng chuẩn.
        click(By.xpath(
            "//div[contains(@class,'ant-result-extra')]//button[contains(normalize-space(),'Xem khóa học đã mua')]"));
        waitUrlContains("/dashboard/video-courses");
    }
}
