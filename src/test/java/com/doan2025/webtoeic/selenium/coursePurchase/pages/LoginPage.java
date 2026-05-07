package com.doan2025.webtoeic.selenium.coursePurchase.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object cho luồng đăng nhập ở trang chủ.
 */
public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver, String baseUrl, long slowMillis) {
        super(driver, baseUrl, slowMillis);
    }

    /**
     * Thực hiện đăng nhập bằng modal "Đăng nhập" trên trang chủ.
     * Sau khi submit thành công, modal đóng và icon giỏ hàng xuất hiện.
     */
    public void loginAs(String email, String password) {
        // 1) Mở trang chủ để thao tác đúng entry-point của người dùng.
        openPath("/");

        // 2) Mở modal đăng nhập từ cụm nút auth ở header.
        click(By.xpath("//div[contains(@class,'auth-buttons')]//button[contains(normalize-space(),'Đăng nhập')]"));
        waitVisible(By.xpath("//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Đăng nhập')]"));

        // 3) Điền credentials và submit form.
        type(By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='Nhập email của bạn']"), email);
        type(By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='Nhập mật khẩu']"), password);
        click(By.xpath("//div[contains(@class,'ant-modal')]//button[@type='submit']"));

        // 4) Chờ modal biến mất và UI logged-in xuất hiện để xác nhận login thành công.
        waitInvisible(By.xpath("//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Đăng nhập')]"));
        waitVisible(By.cssSelector(".cart-button"));
    }
}
