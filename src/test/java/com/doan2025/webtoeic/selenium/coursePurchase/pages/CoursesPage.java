package com.doan2025.webtoeic.selenium.coursePurchase.pages;

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
import org.openqa.selenium.interactions.Actions;

/**
 * Page Object cho màn hình danh sách khóa học (/courses).
 *
 * Trách nhiệm chính:
 * - Tìm khóa học có thể thao tác (thêm giỏ, mua ngay)
 * - Hỗ trợ phân trang
 * - Kiểm tra trạng thái nút theo nghiệp vụ pending order
 */
public class CoursesPage extends BasePage {

    private static final int MAX_PAGINATION_SCAN_PAGES = 40;

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

        // Scan qua nhiều trang để tìm course còn khả dụng cho thao tác add-to-cart.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
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
                // Chỉ chấp nhận thành công khi persisted cart đã chứa title.
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
        // Dùng cho diagnostics khi không tìm được button phù hợp.
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
            return root.findElement(locator).getText().replaceAll("\\s+", " ").trim();
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

        // Thu thập đủ số lượng course distinct theo yêu cầu.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES && result.size() < count; pageAttempt++) {
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
        // Tìm nhanh course đầu tiên có nút Mua ngay còn active.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
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
     * Double-clicks the first course card that still exposes an active "Mua ngay" button.
     */
    public CourseSelection doubleClickBuyNowOnFirstCourseFromListing() {
        // Dùng để kiểm tra idempotency khi người dùng click nhanh 2 lần.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
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

                new Actions(driver).doubleClick(buyNowButtons.get(0)).perform();
                sleep();
                return new CourseSelection(title, price);
            }

            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("No course with active 'Mua ngay' button found in courses listing for double click");
        return null;
    }

    /**
     * Asserts that the course card for {@code title} shows "Tiếp tục thanh toán" (not "Mua ngay").
     * Waits up to 12 seconds for the button state to update after a pending order is created.
     * Fails with a clear BUG message if the button has not changed — detecting Bug 2.
     */
    public void assertCourseShowsContinueToCheckout(String title) {
        WebElement card = findCourseCard(title);
        try {
            wait.until(d -> !card.findElements(
                By.xpath(".//button[contains(normalize-space(),'Tiếp tục thanh toán')]")).isEmpty());
        } catch (Exception timeout) {
            String actual = readCardActions(card);
            Assertions.fail("BUG DETECTED [Bug 2]: course '" + title
                + "' should show 'Tiếp tục thanh toán' for pending order, but found: " + actual);
        }

        // Secondary check: 'Mua ngay' must no longer be present
        List<WebElement> buyNow = card.findElements(
            By.xpath(".//button[contains(normalize-space(),'Mua ngay')]"));
        Assertions.assertTrue(buyNow.isEmpty(),
            "BUG DETECTED [Bug 2]: course '" + title
                + "' should NOT show 'Mua ngay' when a pending order exists");
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


    /**
     * Assert course card shows "Thêm vào giỏ" when no order or cart item exists.
     */
    public void assertCourseShowsAddToCart(String title) {
        WebElement card = findCourseCard(title);
        Assertions.assertFalse(card.findElements(
            By.xpath(".//button[contains(normalize-space(),'Thêm vào giỏ') and not(@disabled)]")).isEmpty(),
            "Course '" + title + "' should show 'Thêm vào giỏ'");
    }

    /**
     * Assert course card shows the owned state after successful purchase.
     */
    public void assertCourseShowsOwnedState(String title) {
        WebElement card = findCourseCard(title);
        Assertions.assertTrue(
            !card.findElements(By.xpath(".//button[contains(normalize-space(),'Đến khóa học')]")).isEmpty()
            || !card.findElements(By.xpath(".//*[contains(normalize-space(),'Đã sở hữu')]")).isEmpty(),
            "Course '" + title + "' should show purchased state");
    }

    /**
     * Clicks the "Mua ngay" button for a specific course card.
     */
    public void clickBuyNowOnCourse(String title) {
        WebElement card = findCourseCard(title);
        List<WebElement> buyNowButtons = card.findElements(
            By.xpath(".//button[contains(normalize-space(),'Mua ngay') and not(@disabled)]"));
        Assertions.assertFalse(buyNowButtons.isEmpty(),
            "Course '" + title + "' should have an active 'Mua ngay' button");
        click(buyNowButtons.get(0));
    }

    /**
     * Double-clicks the "Mua ngay" button for a specific course card.
     */
    public void doubleClickBuyNowOnCourse(String title) {
        WebElement card = findCourseCard(title);
        List<WebElement> buyNowButtons = card.findElements(
            By.xpath(".//button[contains(normalize-space(),'Mua ngay') and not(@disabled)]"));
        Assertions.assertFalse(buyNowButtons.isEmpty(),
            "Course '" + title + "' should have an active 'Mua ngay' button");
        new Actions(driver).doubleClick(buyNowButtons.get(0)).perform();
        sleep();
    }

    /**
     * Clicks the pending-order button and navigates to the orders page.
     */
    public void clickContinueToCheckoutForCourse(String title) {
        WebElement card = findCourseCard(title);
        List<WebElement> buttons = card.findElements(
            By.xpath(".//button[contains(normalize-space(),'Tiếp tục thanh toán')]") );
        Assertions.assertFalse(buttons.isEmpty(),
            "Course '" + title + "' should show 'Tiếp tục thanh toán'");
        // Nút này phải điều hướng sang orders để tiếp tục thanh toán đơn pending.
        click(buttons.get(0));
        waitUrlContains("/dashboard/orders");
    }

    public void assertAnyCourseShowsContinueToCheckout() {
        // Không ràng buộc title để giảm flaky với data động/pagination.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> buttons = driver.findElements(
                By.xpath("//div[contains(@class,'course-card')]//button[contains(normalize-space(),'Tiếp tục thanh toán') and not(@disabled)]"));
            if (!buttons.isEmpty()) {
                return;
            }

            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("Expected at least one course showing 'Tiếp tục thanh toán' for pending order, but none found");
    }

    public void clickFirstContinueToCheckout() {
        // Helper chung: click button pending-order đầu tiên tìm thấy trong listing.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> buttons = driver.findElements(
                By.xpath("//div[contains(@class,'course-card')]//button[contains(normalize-space(),'Tiếp tục thanh toán') and not(@disabled)]"));
            if (!buttons.isEmpty()) {
                click(buttons.get(0));
                waitUrlContains("/dashboard/orders");
                return;
            }

            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("Could not find clickable 'Tiếp tục thanh toán' button on courses listing");
    }

    /** Assert there is at least one visible active Add-to-cart button in current listing. */
    public void assertAnyAddToCartVisible() {
        waitForCourseListLoaded();
        List<WebElement> addButtons = driver.findElements(
            By.xpath("//div[contains(@class,'course-card')]//button[contains(normalize-space(),'Thêm vào giỏ') and not(@disabled)]"));
        Assertions.assertFalse(addButtons.isEmpty(),
            "Expected at least one course with active 'Thêm vào giỏ' button");
    }

    private WebElement findCourseCard(String title) {
        String safeTitle = escapeXpath(title);
        By courseCardBy = By.xpath("//div[contains(@class,'course-card')][.//div[contains(@class,'course-card-title') and contains(normalize-space(),\""
            + safeTitle + "\")]]");

        // Tìm card theo title qua nhiều trang vì course có thể nằm ngoài trang hiện tại.
        for (int pageAttempt = 1; pageAttempt <= MAX_PAGINATION_SCAN_PAGES; pageAttempt++) {
            waitForCourseListLoaded();
            List<WebElement> cards = driver.findElements(courseCardBy);
            if (!cards.isEmpty()) {
                return cards.get(0);
            }
            if (!goToNextCoursePage()) {
                break;
            }
        }

        Assertions.fail("Course card not found after scanning pages: " + title);
        return null;
    }
}
