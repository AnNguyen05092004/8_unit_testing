package com.doan2025.webtoeic.selenium.pages;

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
        openPath("/");
        click(By.xpath("//div[contains(@class,'auth-buttons')]//button[contains(normalize-space(),'Đăng nhập')]"));
        waitVisible(By.xpath("//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Đăng nhập')]"));
        type(By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='Nhập email của bạn']"), email);
        type(By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='Nhập mật khẩu']"), password);
        click(By.xpath("//div[contains(@class,'ant-modal')]//button[@type='submit']"));
        waitInvisible(By.xpath("//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Đăng nhập')]"));
        waitVisible(By.cssSelector(".cart-button"));
    }
}
