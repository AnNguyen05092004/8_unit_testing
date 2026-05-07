package com.doan2025.webtoeic.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Selenium UI tests for Authentication + RBAC module.
 *
 * Prerequisites:
 * - Backend running at http://localhost:8888
 * - Frontend running at http://localhost:5173
 *
 * Screenshots saved to: docs/selenium-evidence/
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthRBACSeleniumTest {

    private static final String BASE_URL = "http://localhost:5173";
    private static final String API_BASE_URL = "http://localhost:8888/api/v1";
    private static final String SCREENSHOT_DIR = "docs/selenium-evidence/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(12);
    private static final List<String> FIREFOX_BINARY_CANDIDATES = List.of(
            System.getenv("FIREFOX_BIN") == null ? "" : System.getenv("FIREFOX_BIN"),
            "/snap/firefox/current/usr/lib/firefox/firefox",
            "/snap/firefox/current/usr/lib/firefox/firefox-bin",
            "/usr/lib/firefox/firefox",
            "/usr/lib64/firefox/firefox"
    );

    private static final String STUDENT_EMAIL = "student@gmail.com";
    private static final String TEACHER_EMAIL = "teacher@gmail.com";
    private static final String MANAGER_EMAIL = "manager@gmail.com";
    private static final String CONSULTANT_EMAIL = "consultant@gmail.com";
    private static final String PASSWORD = "abcd@1234";

    private static final By LOGIN_TRIGGER = By.xpath(
            "//button[(contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZĐĂÂÊÔƠƯ', 'abcdefghijklmnopqrstuvwxyzđăâêôơư'),'đăng nhập') " +
                    "or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'login') " +
                    "or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sign in')) and not(@type='submit')]");
    private static final By REGISTER_TRIGGER = By.xpath(
            "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZĐĂÂÊÔƠƯ', 'abcdefghijklmnopqrstuvwxyzđăâêôơư'),'đăng ký') " +
                    "or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'register') " +
                    "or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sign up')]");
    private static final By EMAIL_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[@type='email' or contains(translate(@placeholder,'EMAIL','email'),'email') or contains(translate(@name,'EMAIL','email'),'email')])[1]");
    private static final By PASSWORD_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[@type='password' or contains(translate(@placeholder,'PASSWORDMẬTKHẨU','passwordmậtkhẩu'),'mật khẩu') or contains(translate(@placeholder,'PASSWORD','password'),'password')])[1]");
    private static final By FORGOT_PASSWORD_EMAIL_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[@type='email' or contains(translate(@placeholder,'EMAIL','email'),'email') or contains(translate(@name,'EMAIL','email'),'email')])[1]");
    private static final By REGISTER_FIRST_NAME_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[contains(translate(@name,'FIRSTNAME','firstname'),'first') or contains(translate(@placeholder,'HỌFIRST','họfirst'),'họ') or contains(translate(@placeholder,'FIRST','first'),'first')])[1]");
    private static final By REGISTER_LAST_NAME_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[contains(translate(@name,'LASTNAME','lastname'),'last') or contains(translate(@placeholder,'TÊNLAST','tênlast'),'tên') or contains(translate(@placeholder,'LAST','last'),'last')])[1]");
    private static final By REGISTER_EMAIL_INPUT = EMAIL_INPUT;
    private static final By REGISTER_PASSWORD_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[@type='password' or contains(translate(@placeholder,'PASSWORDMẬTKHẨU','passwordmậtkhẩu'),'mật khẩu') or contains(translate(@placeholder,'PASSWORD','password'),'password')])[1]");
    private static final By REGISTER_CONFIRM_PASSWORD_INPUT = By.xpath(
            "(//div[contains(@class,'ant-modal')]//input[@type='password' or contains(translate(@name,'CONFIRM','confirm'),'confirm') or contains(translate(@placeholder,'XÁCNHẬNCONFIRM','xácnhậnconfirm'),'xác nhận') or contains(translate(@placeholder,'CONFIRM','confirm'),'confirm')])[last()]");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");
    private static final By USER_ICON = By.cssSelector(".user-icon");
    private static final By MODAL = By.cssSelector(".ant-modal");
    private static final By MODAL_CONTENT = By.cssSelector(".ant-modal-content");
    private static final By VALIDATION_ERRORS = By.cssSelector(".ant-form-item-explain-error");
    private static final By MESSAGE_NOTICE = By.cssSelector(".ant-message-notice-content");
    private static final By PASSWORD_TOGGLE_ICON = By.cssSelector(".ant-input-password-icon");

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        String firefoxBinary = resolveFirefoxBinary();
        if (firefoxBinary != null) {
            options.setBinary(firefoxBinary);
        }
        options.addArguments("--headless");
        options.addArguments("--width=1440");
        options.addArguments("--height=900");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        new File(SCREENSHOT_DIR).mkdirs();
    }

    private static String resolveFirefoxBinary() {
        for (String candidate : FIREFOX_BINARY_CANDIDATES) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }

            File file = new File(candidate);
            if (file.isFile() && file.canExecute()) {
                return candidate;
            }
        }

        return null;
    }

    @BeforeEach
    void resetBrowserState() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.clear();" +
                        "window.sessionStorage.clear();");
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void screenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), Paths.get(SCREENSHOT_DIR + name + ".png"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot: " + SCREENSHOT_DIR + name + ".png");
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void openLoginModal() {
        openHome();
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_TRIGGER)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL));
    }

    private void openRegisterModal() {
        openHome();
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_TRIGGER)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL));
    }

    private void login(String email, String password, String screenshotPrefix) {
        openLoginModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(email);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        screenshot(screenshotPrefix + "-before-submit");
        driver.findElement(SUBMIT_BUTTON).click();
    }

    private void loginSuccessfully(String email, String password, String screenshotPrefix) {
        login(email, password, screenshotPrefix);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL));
        wait.until(ExpectedConditions.visibilityOfElementLocated(USER_ICON));
        screenshot(screenshotPrefix + "-after-login");
    }

    private void openProtectedRoute(String path) {
        driver.get(BASE_URL + path);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void assertRedirectedToHome() {
        wait.until(ExpectedConditions.urlMatches(BASE_URL + "/?$"));
        assertTrue(driver.getCurrentUrl().matches(BASE_URL + "/?$"),
                "Expected browser to be redirected to home page");
    }

    private WebElement getVisibleModalContent() {
        return driver.findElements(MODAL_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible modal content found"));
    }

    private WebElement getVisibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible element found for locator: " + locator));
    }

    private void clearAndType(WebElement element, String value) {
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.DELETE);
        element.sendKeys(value);
    }

    private String getActiveElementPlaceholder() {
        Object placeholder = ((JavascriptExecutor) driver).executeScript(
                "return document.activeElement ? document.activeElement.getAttribute('placeholder') : null;");
        return placeholder == null ? "" : placeholder.toString();
    }

    private void setViewport(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
    }

    private String loginApiAndGetToken(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        HttpResponse<String> response = apiPost(null, "/auth/login", body);
        assertTrue(response.statusCode() == 200, "Login API should return 200 for seeded account " + email);
        return bodyAsJson(response).path("data").path("token").asText();
    }

    private HttpResponse<String> apiPost(String token, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode bodyAsJson(HttpResponse<String> response) throws Exception {
        return OBJECT_MAPPER.readTree(response.body());
    }

    private Long firstIdFromContent(JsonNode json) {
        JsonNode content = json.path("data").path("content");
        if (!content.isArray() || content.isEmpty()) {
            return null;
        }
        JsonNode first = content.get(0);
        return first.hasNonNull("id") ? first.get("id").asLong() : null;
    }

    private Long findUserIdByEmail(String token, String email) throws Exception {
        HttpResponse<String> response = apiPost(token, "/user/filter?page=0&size=10", "{\"email\":\"" + email + "\"}");
        if (response.statusCode() != 200) {
            return null;
        }
        JsonNode content = bodyAsJson(response).path("data").path("content");
        if (!content.isArray() || content.isEmpty()) {
            return null;
        }
        return content.get(0).path("id").asLong();
    }

    private Long findFirstClassId(String token) throws Exception {
        HttpResponse<String> response = apiPost(token, "/class/filter?page=0&size=10", "{}");
        return response.statusCode() == 200 ? firstIdFromContent(bodyAsJson(response)) : null;
    }

    private Long findFirstScheduleIdInClass(String token, Long classId) throws Exception {
        HttpResponse<String> response = apiPost(token, "/class/get-schedules-in-class?page=0&size=10",
                "{\"classId\":[" + classId + "]}");
        return response.statusCode() == 200 ? firstIdFromContent(bodyAsJson(response)) : null;
    }

    private Long findFirstQuizIdInClass(String token, Long classId) throws Exception {
        HttpResponse<String> response = apiPost(token, "/quiz/list-quiz-in-class?id-class=" + classId + "&page=0&size=10", "{}");
        return response.statusCode() == 200 ? firstIdFromContent(bodyAsJson(response)) : null;
    }

    private Long findFirstOwnCourseId(String token) throws Exception {
        HttpResponse<String> response = apiPost(token, "/course/own-courses?page=0&size=10", "{}");
        return response.statusCode() == 200 ? firstIdFromContent(bodyAsJson(response)) : null;
    }

    private Long findFirstOwnPostId(String token) throws Exception {
        HttpResponse<String> response = apiPost(token, "/post/own-posts?page=0&size=10", "{}");
        return response.statusCode() == 200 ? firstIdFromContent(bodyAsJson(response)) : null;
    }

    @Test
    @Order(1)
    @DisplayName("TC_LOGIN_001: Verify overall UI layout")
    void loginOverallUILayout() {
        openLoginModal();
        screenshot("TC_LOGIN_001-login-layout");

        String modalText = driver.findElement(MODAL_CONTENT).getText();
        assertTrue(modalText.contains("Đăng nhập"),
                "Login modal title should be visible");
        assertFalse(driver.findElements(EMAIL_INPUT).isEmpty(),
                "Email input should be visible");
        assertFalse(driver.findElements(PASSWORD_INPUT).isEmpty(),
                "Password input should be visible");
        assertFalse(driver.findElements(By.xpath("//button[contains(.,'Quên mật khẩu')]")).isEmpty(),
                "Forgot password link should be visible");
        assertFalse(driver.findElements(SUBMIT_BUTTON).isEmpty(),
                "Login submit button should be visible");
    }

    @Test
    @Order(2)
    @DisplayName("TC_LOGIN_002: Verify responsiveness of the Login form")
    void loginFormResponsiveOnMobileViewport() {
        setViewport(390, 844);
        openLoginModal();
        screenshot("TC_LOGIN_002-mobile-layout");

        Long hasHorizontalOverflow = (Long) ((JavascriptExecutor) driver).executeScript(
                "return document.documentElement.scrollWidth > window.innerWidth ? 1 : 0;");
        assertTrue(hasHorizontalOverflow == 0,
                "Login modal should not cause horizontal overflow on mobile viewport");

        setViewport(1440, 900);
    }

    @Test
    @Order(3)
    @DisplayName("TC_LOGIN_003: Verify show/hide password icon")
    void loginPasswordCanBeShownAndHidden() {
        openLoginModal();
        WebElement passwordInput = getVisibleElement(PASSWORD_INPUT);
        clearAndType(passwordInput, "Abc@123");

        assertTrue("password".equalsIgnoreCase(passwordInput.getAttribute("type")),
                "Password field should be masked initially");

        getVisibleElement(PASSWORD_TOGGLE_ICON).click();
        assertTrue("text".equalsIgnoreCase(getVisibleElement(PASSWORD_INPUT).getAttribute("type")),
                "Password field should become visible after first toggle");

        getVisibleElement(PASSWORD_TOGGLE_ICON).click();
        assertTrue("password".equalsIgnoreCase(getVisibleElement(PASSWORD_INPUT).getAttribute("type")),
                "Password field should be masked again after second toggle");
    }

    @Test
    @Order(4)
    @DisplayName("TC_LOGIN_004: Verify Tab navigation")
    void loginTabNavigationOrder() {
        openLoginModal();
        WebElement emailInput = getVisibleElement(EMAIL_INPUT);
        emailInput.click();
        emailInput.sendKeys(Keys.TAB);
        assertTrue("Nhập mật khẩu".equals(getActiveElementPlaceholder()),
                "First Tab should move focus from email to password");

        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        String activeText = String.valueOf(((JavascriptExecutor) driver).executeScript(
                "return document.activeElement ? document.activeElement.textContent : '';"));
        assertTrue(activeText.contains("Quên mật khẩu") || activeText.contains("Đăng nhập"),
                "Second Tab should move focus to forgot-password link or submit button");
    }

    @Test
    @Order(5)
    @DisplayName("TC_LOGIN_005: Verify form submission via Enter key")
    void loginSubmitWithEnterKey() {
        openLoginModal();
        getVisibleElement(EMAIL_INPUT).sendKeys(STUDENT_EMAIL);
        WebElement passwordInput = getVisibleElement(PASSWORD_INPUT);
        passwordInput.sendKeys(PASSWORD);
        screenshot("TC_LOGIN_005-before-enter");
        passwordInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL));
        wait.until(ExpectedConditions.visibilityOfElementLocated(USER_ICON));
        screenshot("TC_LOGIN_005-after-enter");

        assertTrue(driver.findElements(USER_ICON).size() == 1,
                "Submitting the login form with Enter should authenticate the user");
    }

    @Test
    @Order(6)
    @DisplayName("TC_LOGIN_006: Verify Loading state and prevent Double-click")
    void loginButtonShowsLoadingAndPreventsDoubleClick() {
        openLoginModal();
        getVisibleElement(EMAIL_INPUT).sendKeys(STUDENT_EMAIL);
        getVisibleElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        WebElement submitButton = getVisibleElement(SUBMIT_BUTTON);
        submitButton.click();
        submitButton.click();
        screenshot("TC_LOGIN_006-after-double-click");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElement(submitButton, "Đang đăng nhập"),
                ExpectedConditions.attributeContains(submitButton, "class", "ant-btn-loading"),
                ExpectedConditions.attributeToBe(submitButton, "disabled", "true")
        ));

        assertTrue(submitButton.getAttribute("class").contains("loading")
                        || submitButton.getAttribute("class").contains("ant-btn-loading")
                        || "true".equalsIgnoreCase(submitButton.getAttribute("disabled")),
                "Login button should enter loading/disabled state after first click");
    }

    @Test
    @Order(7)
    @DisplayName("TC_LOGIN_007: Login with valid account")
    void loginSuccessWithValidStudentCredentials() {
        loginSuccessfully(STUDENT_EMAIL, PASSWORD, "TC_LOGIN_007");
        assertTrue(driver.findElements(USER_ICON).size() == 1,
                "User icon should be visible after successful login");
        assertTrue(driver.findElements(LOGIN_TRIGGER).isEmpty(),
                "Login button should disappear after successful login");
    }

    @Test
    @Order(8)
    @DisplayName("TC_LOGIN_008: Leave required fields blank")
    void loginFailsWithEmptyFields() {
        openLoginModal();
        screenshot("TC_LOGIN_008-empty-form");
        driver.findElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_LOGIN_008-validation-errors");

        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Validation messages should appear when required fields are empty");
    }

    @Test
    @Order(9)
    @DisplayName("TC_LOGIN_009: Invalid email format")
    void loginRejectsInvalidEmailFormat() {
        openLoginModal();
        getVisibleElement(EMAIL_INPUT).sendKeys("student_gmail.com");
        getVisibleElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_LOGIN_009-invalid-email");

        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Invalid email format should be rejected on the login form");
    }

    @Test
    @Order(10)
    @DisplayName("TC_LOGIN_010: Account does not exist")
    void loginWithUnknownAccountShowsFriendlyMessage() {
        login("nick_ma@abc.com", "123123", "TC_LOGIN_010");
        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_LOGIN_010-unknown-account");

        String toastText = getVisibleElement(MESSAGE_NOTICE).getText();
        assertTrue(!toastText.toLowerCase().contains("status code"),
                "Unknown-account error should be user-friendly, not a raw HTTP status message");
    }

    @Test
    @Order(11)
    @DisplayName("TC_LOGIN_011: Incorrect Password")
    void loginFailsWithWrongPassword() {
        login(STUDENT_EMAIL, "12345", "TC_LOGIN_011");
        wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL));
        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_LOGIN_011-after-submit");

        assertFalse(driver.findElements(MESSAGE_NOTICE).isEmpty(),
                "An error message should be shown for invalid credentials");
        assertFalse(driver.findElements(MODAL).isEmpty(),
                "Login modal should remain open after failed login");
        assertTrue(driver.findElements(USER_ICON).isEmpty(),
                "User should not be authenticated after failed login");

        String toastText = getVisibleElement(MESSAGE_NOTICE).getText();
        assertTrue(!toastText.toLowerCase().contains("status code"),
                "Wrong-password error should be user-friendly, not a raw HTTP status message");
    }

    @Test
    @Order(12)
    @DisplayName("TC_LOGIN_012: Password is below the minimum length limit")
    void loginPasswordBelowMinimumLengthShouldBeRejectedClientSide() {
        openLoginModal();
        getVisibleElement(EMAIL_INPUT).sendKeys(STUDENT_EMAIL);
        getVisibleElement(PASSWORD_INPUT).sendKeys("12");
        getVisibleElement(SUBMIT_BUTTON).click();
        screenshot("TC_LOGIN_012-short-password");

        wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL));
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Short password should be rejected with a validation message before API submission");
    }

    @Test
    @Order(13)
    @DisplayName("TC_LOGIN_013: Trim whitespaces at the beginning/end of email")
    void loginShouldTrimEmailWhitespace() {
        loginSuccessfully(" student@gmail.com ", PASSWORD, "TC_LOGIN_013");
        assertTrue(driver.findElements(USER_ICON).size() == 1,
                "Login should succeed even when email has leading/trailing spaces");
    }

    @Test
    @Order(14)
    @DisplayName("TC_LOGIN_014: Account does not exist in Forgot Password")
    void forgotPasswordUnknownEmailShowsFriendlyMessage() {
        openLoginModal();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Quên mật khẩu')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FORGOT_PASSWORD_EMAIL_INPUT));
        getVisibleElement(FORGOT_PASSWORD_EMAIL_INPUT).sendKeys("acs@gmail.com");
        getVisibleElement(By.xpath("//button[contains(.,'Gửi mã OTP')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_LOGIN_014-forgot-unknown-email");

        String toastText = getVisibleElement(MESSAGE_NOTICE).getText();
        assertTrue(!toastText.toLowerCase().contains("status code"),
                "Forgot-password unknown-email message should be user-friendly");
    }

    @Test
    @Order(15)
    @DisplayName("TC_LOGIN_015: Invalid email format in Forgot Password")
    void forgotPasswordRejectsInvalidEmailFormat() {
        openLoginModal();
        screenshot("TC_LOGIN_015-login-modal");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Quên mật khẩu')]"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(EMAIL_INPUT));
        wait.until(ExpectedConditions.visibilityOfElementLocated(FORGOT_PASSWORD_EMAIL_INPUT));
        driver.findElement(FORGOT_PASSWORD_EMAIL_INPUT).sendKeys("acss.com");
        driver.findElement(By.xpath("//button[contains(.,'Gửi mã OTP')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_LOGIN_015-invalid-email");

        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Validation error should appear for invalid forgot-password email");
    }

    @Test
    @Order(16)
    @DisplayName("TC_REGIS_001: Verify overall UI layout")
    void registerOverallUILayout() {
        openRegisterModal();
        screenshot("TC_REGIS_001-register-layout");

        String modalText = driver.findElement(MODAL_CONTENT).getText();
        assertTrue(modalText.contains("Đăng ký"),
                "Register modal title should be visible");
        assertFalse(driver.findElements(REGISTER_FIRST_NAME_INPUT).isEmpty(),
                "First name input should be visible");
        assertFalse(driver.findElements(REGISTER_LAST_NAME_INPUT).isEmpty(),
                "Last name input should be visible");
        assertFalse(driver.findElements(REGISTER_EMAIL_INPUT).isEmpty(),
                "Register email input should be visible");
        assertFalse(driver.findElements(REGISTER_PASSWORD_INPUT).isEmpty(),
                "Register password input should be visible");
        assertFalse(driver.findElements(REGISTER_CONFIRM_PASSWORD_INPUT).isEmpty(),
                "Confirm password input should be visible");
    }

    @Test
    @Order(17)
    @DisplayName("TC_REGIS_002: Verify show/hide password icons")
    void registerPasswordFieldsCanBeShownAndHidden() {
        openRegisterModal();
        List<WebElement> passwordInputs = getVisibleModalContent().findElements(By.cssSelector("input[type='password']"));
        clearAndType(passwordInputs.get(0), "Abc@123");
        clearAndType(passwordInputs.get(1), "Abc@123");

        List<WebElement> toggleIcons = getVisibleModalContent().findElements(PASSWORD_TOGGLE_ICON);
        toggleIcons.get(0).click();
        toggleIcons.get(1).click();

        List<WebElement> visibleTextInputs = getVisibleModalContent().findElements(By.cssSelector("input[type='text']"));
        assertTrue(visibleTextInputs.size() >= 2,
                "Both register password fields should become visible after toggling");
    }

    @Test
    @Order(18)
    @DisplayName("TC_REGIS_003: Verify Tab navigation")
    void registerTabNavigationOrder() {
        openRegisterModal();
        WebElement firstNameInput = getVisibleElement(REGISTER_FIRST_NAME_INPUT);
        firstNameInput.click();
        firstNameInput.sendKeys(Keys.TAB);
        assertTrue("Nhập tên của bạn".equals(getActiveElementPlaceholder()),
                "First Tab should move from first name to last name");
    }

    @Test
    @Order(19)
    @DisplayName("TC_REGIS_004: Verify form submission via Enter key")
    void registerSubmitWithEnterKey() {
        openRegisterModal();
        String uniqueEmail = "selenium.enter." + System.currentTimeMillis() + "@gmail.com";
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys(uniqueEmail);
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        WebElement confirmInput = getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT);
        confirmInput.sendKeys(PASSWORD);
        screenshot("TC_REGIS_004-before-enter");
        confirmInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_REGIS_004-after-enter");
        assertTrue(getVisibleElement(MESSAGE_NOTICE).getText().length() > 0,
                "Submitting the register form with Enter should trigger processing");
    }

    @Test
    @Order(20)
    @DisplayName("TC_REGIS_005: Register with valid information")
    void registerWithValidInformation() {
        openRegisterModal();
        String uniqueEmail = "selenium.valid." + System.currentTimeMillis() + "@gmail.com";
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys(uniqueEmail);
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(PASSWORD);
        screenshot("TC_REGIS_005-before-submit");
        getVisibleElement(SUBMIT_BUTTON).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_REGIS_005-after-submit");
        assertTrue(getVisibleElement(MESSAGE_NOTICE).getText().length() > 0,
                "Valid registration should show a success notification");
    }

    @Test
    @Order(21)
    @DisplayName("TC_REGIS_006: Leave required fields blank")
    void registerBlankFieldsShowValidation() {
        openRegisterModal();
        getVisibleElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_REGIS_006-validation-errors");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Blank register fields should show validation errors");
    }

    @Test
    @Order(22)
    @DisplayName("TC_REGIS_007: Invalid Email format")
    void registerRejectsInvalidEmailFormat() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("johndoe.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_REGIS_007-invalid-email");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Register form should reject invalid email format");
    }

    @Test
    @Order(23)
    @DisplayName("TC_REGIS_008: Password is below the minimum length limit")
    void registerPasswordBelowEightCharactersShouldBeRejected() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("short.pass." + System.currentTimeMillis() + "@gmail.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys("1234567");
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys("1234567");
        getVisibleElement(SUBMIT_BUTTON).click();
        screenshot("TC_REGIS_008-short-password");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Password shorter than 8 characters should be rejected by the register form");
    }

    @Test
    @Order(24)
    @DisplayName("TC_REGIS_009: Password exceeds the maximum length limit")
    void registerPasswordTooLongShouldBeRejected() {
        openRegisterModal();
        String longPassword = "A".repeat(65);
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("long.pass." + System.currentTimeMillis() + "@gmail.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(longPassword);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(longPassword);
        getVisibleElement(SUBMIT_BUTTON).click();
        screenshot("TC_REGIS_009-long-password");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Password longer than the maximum allowed length should be rejected");
    }

    @Test
    @Order(25)
    @DisplayName("TC_REGIS_010: Passwords do not match")
    void registerDetectsPasswordMismatch() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("mismatch." + System.currentTimeMillis() + "@gmail.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys("abcd@9999");
        getVisibleElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS));
        screenshot("TC_REGIS_010-mismatch");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Register form should reject mismatched passwords");
    }

    @Test
    @Order(26)
    @DisplayName("TC_REGIS_011: Register with an existing Email")
    void registerWithExistingEmailShowsFriendlyMessage() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("John");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Doe");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys(STUDENT_EMAIL);
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MESSAGE_NOTICE));
        screenshot("TC_REGIS_011-existing-email");

        String toastText = getVisibleElement(MESSAGE_NOTICE).getText();
        assertTrue(!toastText.toLowerCase().contains("error 400")
                        && !toastText.toLowerCase().contains("status code"),
                "Existing-email error should be user-friendly, not a raw HTTP code");
    }

    @Test
    @Order(27)
    @DisplayName("TC_REGIS_012: Whitespace in Name fields")
    void registerShouldRejectWhitespaceOnlyNames() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("   ");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("   ");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("spaces." + System.currentTimeMillis() + "@gmail.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(PASSWORD);
        getVisibleElement(SUBMIT_BUTTON).click();
        screenshot("TC_REGIS_012-whitespace-names");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Whitespace-only names should be treated as blank and rejected");
    }

    @Test
    @Order(28)
    @DisplayName("TC_REGIS_013: Password must contain uppercase, lowercase, and number")
    void registerShouldEnforcePasswordComplexity() {
        openRegisterModal();
        getVisibleElement(REGISTER_FIRST_NAME_INPUT).sendKeys("Elliot");
        getVisibleElement(REGISTER_LAST_NAME_INPUT).sendKeys("Quang");
        getVisibleElement(REGISTER_EMAIL_INPUT).sendKeys("complexity." + System.currentTimeMillis() + "@gmail.com");
        getVisibleElement(REGISTER_PASSWORD_INPUT).sendKeys("123456");
        getVisibleElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys("123456");
        getVisibleElement(SUBMIT_BUTTON).click();
        screenshot("TC_REGIS_013-password-complexity");
        assertFalse(driver.findElements(VALIDATION_ERRORS).isEmpty(),
                "Weak numeric-only passwords should be rejected by register validation");
    }

    @Test
    @Order(29)
    @DisplayName("TC_RBAC_002: Student strictly prevented from accessing Member Management")
    void studentIsBlockedFromMemberManagementDashboard() {
        loginSuccessfully(STUDENT_EMAIL, PASSWORD, "TC_RBAC_002");
        openProtectedRoute("/dashboard/member-management");
        assertRedirectedToHome();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USER_ICON));
        screenshot("TC_RBAC_002-student-blocked");

        assertFalse(driver.findElements(MESSAGE_NOTICE).isEmpty(),
                "Access denied message should appear when student enters member management");
    }

    @Test
    @Order(30)
    @DisplayName("TC_RBAC_003: Consultant can access own assigned courses")
    void consultantCanAccessCourseManagementDashboard() {
        loginSuccessfully(CONSULTANT_EMAIL, PASSWORD, "TC_RBAC_003");
        openProtectedRoute("/dashboard/course-management");
        wait.until(ExpectedConditions.urlContains("/dashboard/course-management"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.linkText("Quản lý khóa học")));
        screenshot("TC_RBAC_003-consultant-course-management");

        assertTrue(driver.getCurrentUrl().contains("/dashboard/course-management"),
                "Consultant should be allowed to access course management");
    }

    @Test
    @Order(31)
    @DisplayName("TC_RBAC_001: Verify Teacher can mark attendance for their class")
    void teacherCanMarkAttendanceForClass() throws Exception {
        String teacherToken = loginApiAndGetToken(TEACHER_EMAIL, PASSWORD);
        Long classId = findFirstClassId(teacherToken);
        Assumptions.assumeTrue(classId != null, "No class found for TEACHER test data");

        Long scheduleId = findFirstScheduleIdInClass(teacherToken, classId);
        Assumptions.assumeTrue(scheduleId != null, "No class schedule found for TEACHER test data");

        Long studentId = findUserIdByEmail(teacherToken, STUDENT_EMAIL);
        Assumptions.assumeTrue(studentId != null, "Seeded STUDENT user not found");

        String body = "[{\"classId\":" + classId +
                ",\"studentId\":" + studentId +
                ",\"scheduleId\":" + scheduleId +
                ",\"attendanceStatus\":1}]";
        HttpResponse<String> response = apiPost(teacherToken, "/class/attendance", body);

        assertTrue(response.statusCode() == 200,
                "TEACHER should be able to mark attendance for class. Actual: " + response.statusCode());
    }

    @Test
    @Order(32)
    @DisplayName("TC_RBAC_004: Verify Manager can delete or disable user accounts")
    void managerCanDisableUserAccounts() throws Exception {
        String managerToken = loginApiAndGetToken(MANAGER_EMAIL, PASSWORD);
        Long userId = findUserIdByEmail(managerToken, STUDENT_EMAIL);
        Assumptions.assumeTrue(userId != null, "Seeded STUDENT user not found");

        HttpResponse<String> disableResponse = apiPost(managerToken, "/user/disable-user",
                "{\"id\":" + userId + ",\"isActive\":false}");
        try {
            assertTrue(disableResponse.statusCode() == 200,
                    "MANAGER should be able to disable user accounts. Actual: " + disableResponse.statusCode());
        } finally {
            apiPost(managerToken, "/user/disable-user", "{\"id\":" + userId + ",\"isActive\":true}");
        }
    }

    @Test
    @Order(33)
    @DisplayName("TC_RBAC_005: Verify Consultant cannot bypass Manager to approve Courses")
    void consultantShouldNotApproveCourses() throws Exception {
        String consultantToken = loginApiAndGetToken(CONSULTANT_EMAIL, PASSWORD);
        Long courseId = findFirstOwnCourseId(consultantToken);
        Assumptions.assumeTrue(courseId != null, "No course found for CONSULTANT test data");

        HttpResponse<String> response = apiPost(consultantToken, "/course/update-status",
                "{\"id\":" + courseId + ",\"isActive\":true}");
        assertTrue(response.statusCode() == 403,
                "CONSULTANT should be forbidden from approving courses. Actual: " + response.statusCode());
    }

    @Test
    @Order(34)
    @DisplayName("TC_RBAC_006: Verify Consultant cannot bypass Manager to approve Blog Posts")
    void consultantShouldNotApproveBlogPosts() throws Exception {
        String consultantToken = loginApiAndGetToken(CONSULTANT_EMAIL, PASSWORD);
        Long postId = findFirstOwnPostId(consultantToken);
        Assumptions.assumeTrue(postId != null, "No post found for CONSULTANT test data");

        HttpResponse<String> response = apiPost(consultantToken, "/post/update-status",
                "{\"id\":" + postId + ",\"isActive\":true}");
        assertTrue(response.statusCode() == 403,
                "CONSULTANT should be forbidden from approving blog posts. Actual: " + response.statusCode());
    }

    @Test
    @Order(35)
    @DisplayName("TC_RBAC_007: Verify Teacher cannot manually add students to a class")
    void teacherShouldNotAddStudentsToClass() throws Exception {
        String teacherToken = loginApiAndGetToken(TEACHER_EMAIL, PASSWORD);
        Long classId = findFirstClassId(teacherToken);
        Assumptions.assumeTrue(classId != null, "No class found for TEACHER test data");

        Long studentId = findUserIdByEmail(teacherToken, STUDENT_EMAIL);
        Assumptions.assumeTrue(studentId != null, "Seeded STUDENT user not found");

        HttpResponse<String> response = apiPost(teacherToken, "/class/add-user-to-class",
                "{\"id\":" + classId + ",\"memberIds\":[" + studentId + "]}");
        assertTrue(response.statusCode() == 403,
                "TEACHER should be forbidden from manually adding students to a class. Actual: " + response.statusCode());
    }

    @Test
    @Order(36)
    @DisplayName("TC_RBAC_008: Verify Consultant cannot interfere with Academic Quizzes/Exams")
    void consultantShouldNotCreateQuizzes() throws Exception {
        String consultantToken = loginApiAndGetToken(CONSULTANT_EMAIL, PASSWORD);
        HttpResponse<String> response = apiPost(consultantToken, "/quiz/create-quizz",
                "{\"title\":\"RBAC Selenium Quiz\",\"description\":\"Auth RBAC verification\"}");
        assertTrue(response.statusCode() == 403,
                "CONSULTANT should be forbidden from creating quizzes. Actual: " + response.statusCode());
    }

    @Test
    @Order(37)
    @DisplayName("TC_RBAC_009: Verify Student Quiz Submission endpoint is strictly authorized")
    void managerShouldNotSubmitQuizInClass() throws Exception {
        String managerToken = loginApiAndGetToken(MANAGER_EMAIL, PASSWORD);
        Long classId = findFirstClassId(managerToken);
        Assumptions.assumeTrue(classId != null, "No class found for MANAGER test data");

        Long quizId = findFirstQuizIdInClass(managerToken, classId);
        Assumptions.assumeTrue(quizId != null, "No quiz found in class for RBAC submit test");

        HttpResponse<String> response = apiPost(managerToken,
                "/quiz/submit-quiz-in-class?id-quiz=" + quizId + "&id-class=" + classId,
                "[]");
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Only STUDENT should be allowed to submit quiz in class. Actual: " + response.statusCode());
    }

}
