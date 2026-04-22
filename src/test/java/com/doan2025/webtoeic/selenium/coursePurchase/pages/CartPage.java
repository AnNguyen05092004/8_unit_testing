package com.doan2025.webtoeic.selenium.pages;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object cho giỏ hàng (mini-cart và full cart).
 */
public class CartPage extends BasePage {

    public record CartSnapshot(String title, BigDecimal price, BigDecimal total) {
    }

    public CartPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /**
     * Dọn sạch giỏ hàng nếu còn item.
     * Dùng để đưa test về trạng thái deterministic trước khi thao tác.
     */
    public void clearCartIfNeeded() {
        openPath("/dashboard/cart");
        waitUrlContains("/dashboard/cart");
        sleep();

        List<WebElement> emptyHeaders = driver.findElements(By.xpath("//h2[contains(normalize-space(),'Giỏ hàng trống')]"));
        if (!emptyHeaders.isEmpty()) {
            return;
        }

        List<WebElement> clearButtons = driver.findElements(By.xpath("//button[contains(normalize-space(),'Xóa tất cả')]"));
        if (clearButtons.isEmpty()) {
            return;
        }

        clearButtons.get(0).click();
        sleep();
        click(By.xpath("//button[contains(@class,'ant-btn-dangerous') and contains(normalize-space(),'Xóa tất cả')]"));
        wait.until(driver -> !driver.findElements(By.xpath("//h2[contains(normalize-space(),'Giỏ hàng trống')]")).isEmpty()
            || !driver.findElements(By.xpath("//button[contains(normalize-space(),'Khám phá khóa học')]")).isEmpty());
    }

    public void openMiniCart() {
        click(By.cssSelector(".cart-button"));
        waitVisible(By.xpath("//div[contains(@class,'ant-drawer') and contains(@class,'ant-drawer-open')]//*[contains(normalize-space(),'Giỏ hàng')]"));
    }

    /**
     * Từ mini-cart chuyển sang trang giỏ đầy đủ.
     * Nếu nút không có (UI thay đổi), fallback bằng điều hướng trực tiếp.
     */
    public void goToFullCartFromMiniCart() {
        List<WebElement> fullCartButtons = driver.findElements(By.xpath("//div[contains(@class,'ant-drawer-footer')]//button[contains(normalize-space(),'Xem giỏ hàng đầy đủ')]"));
        if (!fullCartButtons.isEmpty()) {
            click(fullCartButtons.get(0));
        } else {
            openPath("/dashboard/cart");
        }
        waitUrlContains("/dashboard/cart");
        waitVisible(By.xpath("//h2[contains(normalize-space(),'Giỏ hàng')]"));
    }

    public CartSnapshot readCartSnapshot(String title) {
        // Lấy snapshot title/price/item-total để assert tính đúng dữ liệu trước mua hàng.
        WebElement item = cartItemByTitle(title);
        String itemTitle = item.findElement(By.cssSelector(".item-title h4")).getText().trim();
        String priceText = item.findElement(By.cssSelector(".item-price h3")).getText().trim();
        String totalText = driver.findElement(By.xpath("//div[contains(@class,'total-row')]//h3")).getText().trim();
        return new CartSnapshot(itemTitle, normalizePrice(priceText), normalizePrice(totalText));
    }

    public void clickBuyNowInCartItem(String title) {
        click(By.xpath("//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')][.//h4[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]//button[contains(normalize-space(),'Mua ngay')]"));
    }

    public void clickRemoveInCartItem(String title) {
        click(By.xpath(
            "//div[contains(@class,'cart-page')]//div[contains(@class,'cart-items')]"
                + "//li[contains(@class,'ant-list-item')][.//h4[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]//button[contains(normalize-space(),'Xóa')]"));
    }

    public void confirmRemoveCourse(String title) {
        By itemLocator = By.xpath(
            "//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')][.//h4[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]");
        By confirmButton = By.xpath(
            "//div[contains(@class,'ant-modal') and .//button[contains(normalize-space(),'Xóa')]]"
                + "//button[contains(@class,'ant-btn-dangerous') and contains(normalize-space(),'Xóa')]");

        wait.until(d -> d.findElements(itemLocator).isEmpty() || !d.findElements(confirmButton).isEmpty());

        if (!driver.findElements(itemLocator).isEmpty() && !driver.findElements(confirmButton).isEmpty()) {
            click(confirmButton);
        }
        wait.until(d -> d.findElements(By.xpath(
            "//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')][.//h4[contains(normalize-space(),\""
                + escapeXpath(title) + "\")]]")).isEmpty());
    }

    /**
     * Clicks "Mua ngay" for a specific course inside the open mini-cart drawer.
     * The drawer must already be open (call openMiniCart() first).
     * After clicking, the drawer closes automatically and a success modal appears.
     */
    public void clickBuyNowInMiniCartForCourse(String title) {
        By locator = By.xpath(
            "//div[contains(@class,'ant-drawer') and contains(@class,'ant-drawer-open')]"
            + "//li[contains(@class,'ant-list-item') and contains(@class,'cart-item')]"
            + "[.//*[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"
            + "//ul[contains(@class,'ant-list-item-action')]"
            + "//button[contains(normalize-space(),'Mua ngay')]");
        click(locator);
        // Wait for drawer to close after purchase
        wait.until(d -> d.findElements(
            By.xpath("//div[contains(@class,'ant-drawer') and contains(@class,'ant-drawer-open')]")).isEmpty());
        sleep();
    }

    public void handleBuyNowOutcome() {
        // Sau "Mua ngay" có thể xảy ra 2 kết quả hợp lệ:
        // 1) Hiện modal tạo đơn thành công.
        // 2) Đã tự điều hướng về /dashboard/orders.
        By successModalTitle = By.xpath("//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Tạo đơn hàng thành công')]");
        By successModalOk = By.xpath("//div[contains(@class,'ant-modal') and .//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Tạo đơn hàng thành công')]]//button[contains(@class,'ant-btn-primary')]");

        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(successModalTitle),
                org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/dashboard/orders")
            ));
        } catch (Exception exception) {
            openPath("/dashboard/orders");
            return;
        }

        if (!driver.findElements(successModalTitle).isEmpty()) {
            // Click OK để app điều hướng theo luồng bình thường.
            click(successModalOk);
        }
    }

    private WebElement cartItemByTitle(String title) {
        try {
            return waitVisible(By.xpath("//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')][.//h4[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        } catch (Exception exception) {
            Assertions.fail("Expected course not found in cart. Target='" + title + "'. Cart diagnostics: " + readCartDiagnostics());
            return null;
        }
    }

    private String readCartDiagnostics() {
        List<WebElement> titles = driver.findElements(By.cssSelector(".cart-items .item-title h4"));
        if (!titles.isEmpty()) {
            StringBuilder builder = new StringBuilder("visible cart items: ");
            for (int index = 0; index < titles.size(); index++) {
                if (index > 0) {
                    builder.append(" ; ");
                }
                builder.append(titles.get(index).getText().trim());
            }
            return builder.toString();
        }

        if (!driver.findElements(By.xpath("//h2[contains(normalize-space(),'Giỏ hàng trống')] | //button[contains(normalize-space(),'Khám phá khóa học')]" )).isEmpty()) {
            return "cart is empty";
        }

        return "cart content not detected";
    }

    private BigDecimal normalizePrice(String rawText) {
        // Chuẩn hóa text tiền tệ về số nguyên để assert ổn định.
        String digits = rawText.replaceAll("[^0-9]", "");
        return new BigDecimal(digits);
    }

    /**
     * Navigates to the cart page and asserts the course is NOT present.
     * A failure here indicates Bug 1: purchased course was not removed from cart.
     */
    public void assertCourseAbsentFromCart(String title) {
        openPath("/dashboard/cart");
        waitUrlContains("/dashboard/cart");
        // Wait for cart content to render (items list, empty heading, or explore button)
        wait.until(d ->
            !d.findElements(By.cssSelector(".cart-items .ant-list-item")).isEmpty()
            || !d.findElements(By.xpath("//h2[contains(normalize-space(),'Giỏ hàng trống')]")).isEmpty()
            || !d.findElements(By.xpath("//button[contains(normalize-space(),'Khám phá khóa học')]")).isEmpty()
        );
        List<WebElement> items = driver.findElements(By.xpath(
            "//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')]"
            + "[.//h4[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        Assertions.assertTrue(items.isEmpty(),
            "BUG DETECTED [Bug 1]: course '" + title
            + "' should have been removed from cart after purchase, but is still present");
    }

    /**
     * Asserts the course IS present on the currently loaded cart page.
     */
    public void assertCoursePresentInCart(String title) {
        List<WebElement> items = driver.findElements(By.xpath(
            "//div[contains(@class,'cart-items')]//li[contains(@class,'ant-list-item')]"
            + "[.//h4[contains(normalize-space(),\"" + escapeXpath(title) + "\")]]"));
        Assertions.assertFalse(items.isEmpty(),
            "Course '" + title + "' should still be in cart but is missing");
    }

    /**
     * Reads the cart badge count from the header icon.
     */
    public int readCartBadgeCount() {
        List<WebElement> badges = driver.findElements(By.cssSelector(".cart-badge .ant-badge-count"));
        if (badges.isEmpty()) {
            return 0;
        }
        String text = badges.get(0).getText().replaceAll("[^0-9]", "");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public void assertCartTotalZero() {
        openPath("/dashboard/cart");
        waitUrlContains("/dashboard/cart");
        List<WebElement> totalRows = driver.findElements(By.xpath("//div[contains(@class,'total-row')]//h3"));
        if (totalRows.isEmpty()) {
            return;
        }
        Assertions.assertEquals(BigDecimal.ZERO, normalizePrice(totalRows.get(0).getText().trim()),
            "Expected cart total to become 0 after removing the only course");
    }
}
