package com.doan2025.webtoeic.selenium.pages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object cho màn hình danh sách khóa học (/courses).
 *
 * Trách nhiệm chính:
 * - Tìm khóa học có thể thao tác (thêm giỏ, mua ngay)
 * - Hỗ trợ phân trang
 * - Kiểm tra trạng thái nút theo nghiệp vụ pending order
 */
public class CoursesPage extends BasePage {

    public record CourseSelection(String title, BigDecimal price) {
    }

    public CoursesPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /** Mở trang courses. */
    public void openCourses() {
        openPath("/courses");
    }

    /**
     * Thêm khóa học đầu tiên có thể mua vào giỏ.
     * Quét tối đa nhiều trang và chỉ trả về khi localStorage xác nhận đã lưu vào cart.
     */
    public CourseSelection addFirstPurchasableCourseToCart() {
        StringBuilder diagnostics = new StringBuilder();
        Set<String> triedTitles = new LinkedHashSet<>();

        for (int pageAttempt = 1; pageAttempt <= 8; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> cards = driver.findElements(By.cssSelector(".course-card"));

            for (int index = 0; index < cards.size(); index++) {
                cards = driver.findElements(By.cssSelector(".course-card"));
                WebElement card = cards.get(index);

                List<WebElement> addButtons = card.findElements(By.xpath(".//button[contains(normalize-space(),'Thêm vào giỏ') and not(@disabled)]"));
                if (addButtons.isEmpty()) {
                    continue;
                }

                String title = safeText(card, By.cssSelector(".course-card-title"));
                if (triedTitles.contains(title)) {
                    continue;
                }
                triedTitles.add(title);

                String priceText = card.findElement(By.cssSelector(".course-card-price")).getText().trim();
                BigDecimal price = normalizePrice(priceText);

                click(addButtons.get(0));
                if (waitForCourseInPersistedCart(title)) {
                    return new CourseSelection(title, price);
                }
            }

            diagnostics.append("Page ").append(pageAttempt).append(" visible actions: ").append(readVisibleCourseActions()).append(System.lineSeparator());
            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("No course could actually be added to cart. " + diagnostics);
        return null;
    }

    private void waitForCourseListLoaded() {
        // Chấp nhận 2 trạng thái hợp lệ: có course-card hoặc có empty-state.
        wait.until(driver -> {
            boolean hasCards = !driver.findElements(By.cssSelector(".course-card")).isEmpty();
            boolean hasEmptyState = !driver.findElements(By.cssSelector(".ant-empty")).isEmpty();
            return hasCards || hasEmptyState;
        });
        sleep();
    }

    private boolean goToNextCoursePage() {
        waitForCourseListLoaded();
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

        String activeBefore = readActiveCoursePage();
        next.click();
        sleep();
        // Chờ trang active thay đổi để tránh thao tác vào dữ liệu cũ.
        wait.until(driver -> !readActiveCoursePage().equals(activeBefore));
        waitForCourseListLoaded();
        return true;
    }

    private String readActiveCoursePage() {
        List<WebElement> activePages = driver.findElements(By.cssSelector(".custom-pagination .ant-pagination-item-active"));
        if (activePages.isEmpty()) {
            activePages = driver.findElements(By.cssSelector(".ant-pagination-item-active"));
        }
        return activePages.isEmpty() ? "unknown" : activePages.get(0).getText().trim();
    }

    private String readVisibleCourseActions() {
        List<WebElement> cards = driver.findElements(By.cssSelector(".course-card"));
        StringBuilder builder = new StringBuilder();
        for (WebElement card : cards) {
            String title = safeText(card, By.cssSelector(".course-card-title"));
            String actions = card.findElement(By.cssSelector(".course-card-actions")).getText().replace(System.lineSeparator(), " | ").trim();
            if (builder.length() > 0) {
                builder.append(" ; ");
            }
            builder.append(title).append(" -> ").append(actions);
        }
        return builder.toString();
    }

    private boolean waitForCourseInPersistedCart(String title) {
        try {
            // Dựa vào localStorage cart-storage để xác nhận add-to-cart thực sự thành công.
            wait.until(driver -> persistedCartContains(title));
            sleep();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean persistedCartContains(String title) {
        Object value = ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('cart-storage');");
        return value instanceof String stored && stored.contains(title);
    }

    private String safeText(WebElement root, By locator) {
        try {
            return root.findElement(locator).getText().trim();
        } catch (Exception exception) {
            return "unknown";
        }
    }

    private BigDecimal normalizePrice(String rawText) {
        // Chuyển "499.000đ" -> 499000 để so sánh số học ổn định.
        String digits = rawText.replaceAll("[^0-9]", "");
        return new BigDecimal(digits);
    }

    /**
     * Adds {@code count} distinct courses to the cart by scanning the listing (multiple pages).
     * Skips courses whose "Thêm vào giỏ" button is absent or disabled (already in cart / owned).
     */
    public List<CourseSelection> addMultipleCoursesToCart(int count) {
        List<CourseSelection> result = new ArrayList<>();
        Set<String> addedTitles = new LinkedHashSet<>();

        for (int pageAttempt = 1; pageAttempt <= 8 && result.size() < count; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> cards = driver.findElements(By.cssSelector(".course-card"));

            for (int index = 0; index < cards.size() && result.size() < count; index++) {
                cards = driver.findElements(By.cssSelector(".course-card"));
                WebElement card = cards.get(index);

                List<WebElement> addButtons = card.findElements(
                    By.xpath(".//button[contains(normalize-space(),'Thêm vào giỏ') and not(@disabled)]"));
                if (addButtons.isEmpty()) {
                    continue;
                }

                String title = safeText(card, By.cssSelector(".course-card-title"));
                if (addedTitles.contains(title)) {
                    continue;
                }

                String priceText = card.findElement(By.cssSelector(".course-card-price")).getText().trim();
                BigDecimal price = normalizePrice(priceText);

                click(addButtons.get(0));
                if (waitForCourseInPersistedCart(title)) {
                    result.add(new CourseSelection(title, price));
                    addedTitles.add(title);
                }
            }

            if (result.size() < count && !goToNextCoursePage()) {
                break;
            }
        }

        Assertions.assertTrue(result.size() >= count,
            "Could only add " + result.size() + " of " + count + " requested courses to cart");
        return result;
    }

    /**
     * Finds the first course card showing an active "Mua ngay" button and clicks it.
     * Used to create a PENDING order directly from the courses listing (Bug 2 scenario).
     */
    public CourseSelection clickBuyNowOnFirstCourseFromListing() {
        for (int pageAttempt = 1; pageAttempt <= 8; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> cards = driver.findElements(By.cssSelector(".course-card"));

            for (int index = 0; index < cards.size(); index++) {
                cards = driver.findElements(By.cssSelector(".course-card"));
                WebElement card = cards.get(index);

                List<WebElement> buyNowButtons = card.findElements(
                    By.xpath(".//button[contains(normalize-space(),'Mua ngay') and not(@disabled)]"));
                if (buyNowButtons.isEmpty()) {
                    continue;
                }

                String title = safeText(card, By.cssSelector(".course-card-title"));
                String priceText = card.findElement(By.cssSelector(".course-card-price")).getText().trim();
                BigDecimal price = normalizePrice(priceText);

                click(buyNowButtons.get(0));
                return new CourseSelection(title, price);
            }

            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("No course with active 'Mua ngay' button found in courses listing");
        return null;
    }

    /**
     * Asserts that the course card for {@code title} shows "Tiếp tục thanh toán" (not "Mua ngay").
     * Waits up to 12 seconds for the button state to update after a pending order is created.
     * Fails with a clear BUG message if the button has not changed — detecting Bug 2.
     */
    public void assertCourseShowsContinueToCheckout(String title) {
        try {
            wait.until(d -> {
                for (WebElement card : d.findElements(By.cssSelector(".course-card"))) {
                    if (title.equals(safeText(card, By.cssSelector(".course-card-title")))) {
                        return !card.findElements(
                            By.xpath(".//button[contains(normalize-space(),'Tiếp tục thanh toán')]")).isEmpty();
                    }
                }
                return false;
            });
        } catch (Exception timeout) {
            String actual = "course card not found on current page";
            for (WebElement card : driver.findElements(By.cssSelector(".course-card"))) {
                if (title.equals(safeText(card, By.cssSelector(".course-card-title")))) {
                    actual = readCardActions(card);
                    break;
                }
            }
            Assertions.fail("BUG DETECTED [Bug 2]: course '" + title
                + "' should show 'Tiếp tục thanh toán' for pending order, but found: " + actual);
        }

        // Secondary check: 'Mua ngay' must no longer be present
        for (WebElement card : driver.findElements(By.cssSelector(".course-card"))) {
            if (title.equals(safeText(card, By.cssSelector(".course-card-title")))) {
                List<WebElement> buyNow = card.findElements(
                    By.xpath(".//button[contains(normalize-space(),'Mua ngay')]"));
                Assertions.assertTrue(buyNow.isEmpty(),
                    "BUG DETECTED [Bug 2]: course '" + title
                        + "' should NOT show 'Mua ngay' when a pending order exists");
                return;
            }
        }
    }

    private String readCardActions(WebElement card) {
        try {
            return card.findElement(By.cssSelector(".course-card-actions")).getText().trim();
        } catch (Exception exception) {
            return "unknown actions";
        }
    }

    /**
     * Checks if the course card for {@code title} still shows "Mua ngay" (bug is present).
     * If yes, clicks the button and asserts that an Ant Design error toast appears —
     * documenting that clicking again surfaces an "order already exists" error.
     *
     * Call this AFTER {@link #assertCourseShowsContinueToCheckout} to provide the
     * secondary evidence of Bug 2: not only is the button wrong, but using it causes an error.
     *
     * If the button is correctly absent (bug is fixed), this method is a no-op.
     */
    public void clickBuyNowIfStillVisibleAndAssertErrorToast(String title) {
        WebElement targetCard = null;
        for (WebElement card : driver.findElements(By.cssSelector(".course-card"))) {
            if (title.equals(safeText(card, By.cssSelector(".course-card-title")))) {
                targetCard = card;
                break;
            }
        }

        if (targetCard == null) {
            return; // card not on current page, skip
        }

        List<WebElement> buyNowButtons = targetCard.findElements(
            By.xpath(".//button[contains(normalize-space(),'Mua ngay') and not(@disabled)]"));

        if (buyNowButtons.isEmpty()) {
            return; // button already gone = bug is fixed, nothing to do
        }

        // Bug is still present: click the button to expose the secondary error
        click(buyNowButtons.get(0));

        // Ant Design message.error toast: .ant-message-notice containing an error icon
        By errorToast = By.xpath(
            "//div[contains(@class,'ant-message-notice')]"
            + "[.//*[contains(@class,'ant-message-error') or contains(@class,'anticon-close-circle')]]");
        try {
            WebElement toast = waitVisible(errorToast);
            String toastText = toast.getText().trim();
            // Record the error text in the failure message for visibility
            Assertions.assertNotNull(toastText,
                "BUG DETECTED [Bug 2 secondary]: error toast appeared but had no text");
            // The toast itself confirming the error is enough; log it via assertion message
            Assertions.assertTrue(toast.isDisplayed(),
                "BUG DETECTED [Bug 2 secondary]: 'Mua ngay' is still clickable for course '"
                + title + "' and clicking it shows error: \"" + toastText + "\"");
        } catch (org.openqa.selenium.TimeoutException noToast) {
            // No toast means clicking triggered unexpected navigation or silent failure
            Assertions.fail(
                "BUG DETECTED [Bug 2 secondary]: 'Mua ngay' is still clickable for course '"
                + title + "' but no error toast appeared after clicking. "
                + "Current URL: " + driver.getCurrentUrl());
        }
    }
}
