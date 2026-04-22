package com.doan2025.webtoeic.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base class cho tất cả Page Object.
 *
 * Chứa các thao tác Selenium dùng chung (open, click, type, wait)
 * để các page con chỉ tập trung vào nghiệp vụ màn hình.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final String baseUrl;
    protected final long slowMillis;

    protected BasePage(WebDriver driver, String baseUrl, long slowMillis) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.slowMillis = slowMillis;
        // Timeout chuẩn cho explicit wait trong toàn bộ suite.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    /** Mở một path tương đối theo baseUrl, ví dụ /courses hoặc /dashboard/cart. */
    protected void openPath(String path) {
        driver.get(baseUrl + path);
        sleep();
    }

    /** Chờ đến khi element hiển thị rồi trả về element đó để thao tác tiếp. */
    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Chờ element biến mất khỏi giao diện (thường dùng cho modal/spinner). */
    protected void waitInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Chờ URL chứa một đoạn text mong đợi, ví dụ /dashboard/orders. */
    protected void waitUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** Click qua locator với explicit wait để giảm flaky. */
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        sleep();
    }

    /** Click trực tiếp vào element đã tìm được. */
    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        sleep();
    }

    /** Xóa nội dung input cũ rồi nhập giá trị mới. */
    protected void type(By locator, String value) {
        WebElement input = waitVisible(locator);
        input.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE);
        input.sendKeys(value);
        sleep();
    }

    /** Delay theo slowMillis để chạy chậm phục vụ demo/quan sát UI. */
    protected void sleep() {
        if (slowMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(slowMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting in page object", exception);
        }
    }

    /** Pause for an explicit number of milliseconds, regardless of slowMillis. */
    public void pause(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during explicit pause", exception);
        }
    }

    protected String escapeXpath(String text) {
        // Tránh lỗi XPath khi title có dấu nháy kép.
        return text.replace("\"", "").trim();
    }
}
