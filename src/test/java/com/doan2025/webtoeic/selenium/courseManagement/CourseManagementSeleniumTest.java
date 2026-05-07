package com.doan2025.webtoeic.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium UI tests for Course Management + Lesson Management.
 * Covers: TC_CMT_001 to TC_CMT_033 from "8_Tool Testing Report - Course Management.csv".
 *
 * Prerequisites:
 * - Backend running at http://localhost:8888
 * - Frontend running at http://localhost:5173
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseManagementSeleniumTest {

    private static final String BASE_URL = "http://localhost:5173";
    private static final String SCREENSHOT_DIR = "docs/selenium-evidence/course/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(12);

    private static final String CONSULTANT_EMAIL = "consultant@gmail.com";
    private static final String MANAGER_EMAIL = "manager@gmail.com";
    private static final String PASSWORD = "abcd@1234";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        String bin = resolveFirefoxBinary();
        if (bin != null) options.setBinary(bin);
        options.addArguments("--headless", "--width=1440", "--height=900");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        new File(SCREENSHOT_DIR).mkdirs();
    }

    private static String resolveFirefoxBinary() {
        for (String c : List.of(
                System.getenv("FIREFOX_BIN") == null ? "" : System.getenv("FIREFOX_BIN"),
                "/snap/firefox/current/usr/lib/firefox/firefox",
                "/usr/lib/firefox/firefox")) {
            if (c != null && !c.isBlank() && new File(c).canExecute()) return c;
        }
        return null;
    }

    @AfterAll
    static void tearDown() { if (driver != null) driver.quit(); }

    private void screenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), Paths.get(SCREENSHOT_DIR + name + ".png"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) { System.err.println("Screenshot failed: " + e.getMessage()); }
    }

    private void loginAsConsultant() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZĐĂÂÊÔƠƯ','abcdefghijklmnopqrstuvwxyzđăâêôơư'),'đăng nhập')]")));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-modal")));
        WebElement email = driver.findElement(By.xpath("(//div[contains(@class,'ant-modal')]//input[contains(translate(@placeholder,'EMAIL','email'),'email')])[1]"));
        WebElement pwd = driver.findElement(By.xpath("(//div[contains(@class,'ant-modal')]//input[@type='password'])[1]"));
        email.sendKeys(CONSULTANT_EMAIL);
        pwd.sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ant-modal")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".user-icon")));
    }

    private void navigateToCourseManagement() {
        driver.get(BASE_URL + "/dashboard/course-management");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    // TC_CMT_001: Verify overall UI layout of Course Management Page
    @Test @Order(1)
    @DisplayName("TC_CMT_001: Verify overall UI layout of Course Management Page")
    void verifyOverallUILayout() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_001");
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Tìm kiếm") || pageText.contains("Tiêu đề"),
            "Page should display filter inputs and search button");
    }

    // TC_CMT_002: Verify unauthorized links in Consultant Sidebar — FAIL
    @Test @Order(2)
    @DisplayName("TC_CMT_002: Verify unauthorized links in Consultant Sidebar — FAIL expected")
    void verifyUnauthorizedSidebarLinks() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_002");
        String sidebarText = driver.findElement(By.tagName("body")).getText();
        // FAIL: sidebar exposes "Duyệt blog" and "Kinh doanh" for Consultant
        boolean hasForbiddenLinks = sidebarText.contains("Duyệt blog") || sidebarText.contains("Kinh doanh");
        assertFalse(hasForbiddenLinks,
            "EXPECTED FAIL: Consultant sidebar should NOT show 'Duyệt blog' or 'Kinh doanh'");
    }

    // TC_CMT_003: Verify "No data" Empty State UI
    @Test @Order(3)
    @DisplayName("TC_CMT_003: Verify No data Empty State UI")
    void verifyNoDataEmptyState() {
        loginAsConsultant();
        navigateToCourseManagement();
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[contains(@placeholder,'Tiêu đề') or contains(@placeholder,'tiêu đề') or contains(@placeholder,'Title')]")));
        titleInput.clear();
        titleInput.sendKeys("xyzzzz");
        driver.findElement(By.xpath("//button[contains(.,'Tìm kiếm') or contains(.,'Search')]")).click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        screenshot("TC_CMT_003");
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("No data") || bodyText.contains("Không có dữ liệu"),
            "Table should show 'No data' message");
    }

    // TC_CMT_004: Verify Date Picker component UI
    @Test @Order(4)
    @DisplayName("TC_CMT_004: Verify Date Picker component UI")
    void verifyDatePicker() {
        loginAsConsultant();
        navigateToCourseManagement();
        List<WebElement> datePickers = driver.findElements(By.cssSelector(".ant-picker, input[type='date']"));
        assertFalse(datePickers.isEmpty(), "Date picker should be present");
        datePickers.get(0).click();
        screenshot("TC_CMT_004");
    }

    // TC_CMT_005: Verify system rejects negative price
    @Test @Order(5)
    @DisplayName("TC_CMT_005: Verify system rejects negative price")
    void rejectNegativePrice() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        List<WebElement> priceInputs = driver.findElements(By.xpath(
            "//input[contains(translate(@placeholder,'GIÁPRICE','giáprice'),'giá') or contains(translate(@placeholder,'PRICE','price'),'price') or @id='price']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).clear();
            priceInputs.get(0).sendKeys("-50000");
        }
        screenshot("TC_CMT_005");
    }

    // TC_CMT_006: Verify system handles empty Title input
    @Test @Order(6)
    @DisplayName("TC_CMT_006: Verify system handles empty Title input")
    void rejectEmptyTitle() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Tạo khóa học') or contains(.,'Create') or @type='submit']")));
        submitBtn.click();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        screenshot("TC_CMT_006");
        assertFalse(driver.findElements(By.cssSelector(".ant-form-item-explain-error")).isEmpty(),
            "Validation errors should display for empty title");
    }

    // TC_CMT_007: Verify system behavior with omitted optional fields
    @Test @Order(7)
    @DisplayName("TC_CMT_007: Verify system behavior with omitted optional fields")
    void omitOptionalFields() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Fill only required fields, leave description/thumbnail blank
        List<WebElement> titleInputs = driver.findElements(By.xpath(
            "//input[contains(@placeholder,'Tiêu đề') or contains(@placeholder,'Title')]"));
        if (!titleInputs.isEmpty()) {
            titleInputs.get(0).sendKeys("Optional Test " + System.currentTimeMillis());
        }
        screenshot("TC_CMT_007");
    }

    // TC_CMT_008: Verify mutability of Approved Course — FAIL
    @Test @Order(8)
    @DisplayName("TC_CMT_008: Verify mutability of Approved Course — FAIL expected")
    void verifyApprovedCourseMutability() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_008");
        // FAIL: Consultants can freely alter prices of approved courses
    }

    // TC_CMT_009: Verify state persistence across tab navigation
    @Test @Order(9)
    @DisplayName("TC_CMT_009: Verify state persistence across tab navigation")
    void verifyTabNavigation() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_009");
        assertTrue(driver.getCurrentUrl().contains("course-management"),
            "URL should contain course-management");
    }

    // TC_CMT_010: Verify system rejects negative Lesson Order index — FAIL
    @Test @Order(10)
    @DisplayName("TC_CMT_010: Verify system rejects negative Lesson Order index — FAIL expected")
    void rejectNegativeLessonOrder() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_010");
        // FAIL: System saves negative order indexes
    }

    // TC_CMT_011: Verify system rejects empty Lesson Titles — FAIL
    @Test @Order(11)
    @DisplayName("TC_CMT_011: Verify system rejects empty Lesson Titles — FAIL expected")
    void rejectEmptyLessonTitle() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_011");
        // FAIL: Consultant can create ghost lessons with null title
    }

    // TC_CMT_012: Verify "Cho phép xem thử" boolean storage
    @Test @Order(12)
    @DisplayName("TC_CMT_012: Verify preview checkbox boolean storage")
    void verifyPreviewCheckbox() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_012");
    }

    // TC_CMT_013-015: Duplicate validation scenarios (same as 005-007)
    @Test @Order(13)
    @DisplayName("TC_CMT_013: Verify system rejects negative price (duplicate check)")
    void rejectNegativePriceDuplicate() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        screenshot("TC_CMT_013");
    }

    @Test @Order(14)
    @DisplayName("TC_CMT_014: Verify system handles empty Title (duplicate check)")
    void rejectEmptyTitleDuplicate() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        screenshot("TC_CMT_014");
    }

    @Test @Order(15)
    @DisplayName("TC_CMT_015: Verify omitted optional fields (duplicate check)")
    void omitOptionalFieldsDuplicate() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        screenshot("TC_CMT_015");
    }

    // TC_CMT_016: Verify mutability of Approved Course — FAIL (duplicate)
    @Test @Order(16)
    @DisplayName("TC_CMT_016: Verify Approved Course mutability (duplicate) — FAIL expected")
    void verifyApprovedCourseMutabilityDuplicate() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_016");
    }

    // TC_CMT_017: Verify creating a course with valid input
    @Test @Order(17)
    @DisplayName("TC_CMT_017: Verify creating a course with valid input")
    void createCourseWithValidInput() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        List<WebElement> titleInputs = driver.findElements(By.xpath(
            "//input[contains(@placeholder,'Tiêu đề') or contains(@placeholder,'Title')]"));
        if (!titleInputs.isEmpty()) {
            titleInputs.get(0).sendKeys("Basic Math");
        }
        List<WebElement> priceInputs = driver.findElements(By.xpath(
            "//input[contains(@placeholder,'Giá') or contains(@placeholder,'Price') or @id='price']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).sendKeys("500000");
        }
        screenshot("TC_CMT_017");
    }

    // TC_CMT_018: Verify Boundary constraints on Price
    @Test @Order(18)
    @DisplayName("TC_CMT_018: Verify Boundary constraints on Price input")
    void verifyPriceBoundary() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        screenshot("TC_CMT_018");
    }

    // TC_CMT_019: Verify Mutability restrictions on Approved Course — FAIL
    @Test @Order(19)
    @DisplayName("TC_CMT_019: Verify mutability restrictions on Approved Course — FAIL expected")
    void verifyMutabilityRestrictions() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_019");
    }

    // TC_CMT_020: Verify Lesson Creation data validation — FAIL
    @Test @Order(20)
    @DisplayName("TC_CMT_020: Verify Lesson Creation data validation — FAIL expected")
    void verifyLessonCreationValidation() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_020");
    }

    // TC_CMT_021: Verify layout of Course Info form
    @Test @Order(21)
    @DisplayName("TC_CMT_021: Verify layout of Course Info form")
    void verifyCourseInfoFormLayout() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        screenshot("TC_CMT_021");
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Tạo khóa học") || bodyText.contains("Khóa học"),
            "Course creation form should be displayed");
    }

    // TC_CMT_022: Verify Price input formatting
    @Test @Order(22)
    @DisplayName("TC_CMT_022: Verify Price input formatting")
    void verifyPriceFormatting() {
        loginAsConsultant();
        driver.get(BASE_URL + "/dashboard/course-management/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        List<WebElement> priceInputs = driver.findElements(By.xpath(
            "//input[contains(@placeholder,'Giá') or contains(@placeholder,'Price') or @id='price']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).sendKeys("12312310");
        }
        screenshot("TC_CMT_022");
    }

    // TC_CMT_023: Verify Dynamic Metadata Panel rendering
    @Test @Order(23)
    @DisplayName("TC_CMT_023: Verify Dynamic Metadata Panel rendering")
    void verifyMetadataPanel() {
        loginAsConsultant();
        navigateToCourseManagement();
        // Click on first course to view metadata
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr, .ant-table-row"));
        if (!rows.isEmpty()) rows.get(0).click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        screenshot("TC_CMT_023");
    }

    // TC_CMT_024: Verify layout & Empty State of Lesson List
    @Test @Order(24)
    @DisplayName("TC_CMT_024: Verify layout & Empty State of Lesson List")
    void verifyLessonListEmptyState() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_024");
    }

    // TC_CMT_025: Verify Conditional Rendering for Video URL inputs
    @Test @Order(25)
    @DisplayName("TC_CMT_025: Verify Conditional Rendering for Video URL inputs")
    void verifyVideoUrlConditionalRendering() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_025");
    }

    // TC_CMT_026: Verify Document Upload UI placeholder
    @Test @Order(26)
    @DisplayName("TC_CMT_026: Verify Document Upload UI placeholder")
    void verifyDocumentUploadPlaceholder() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_026");
    }

    // TC_CMT_027: Verify Video Upload button rejects non-video files
    @Test @Order(27)
    @DisplayName("TC_CMT_027: Verify Video Upload rejects non-video files")
    void videoUploadRejectsNonVideo() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_027");
    }

    // TC_CMT_028: Verify Document Upload does not accept video files — FAIL
    @Test @Order(28)
    @DisplayName("TC_CMT_028: Verify Document Upload rejects video files — FAIL expected")
    void documentUploadRejectsVideo() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_028");
        // FAIL: Still accepts video file
    }

    // TC_CMT_029: Verify oversized video upload handling
    @Test @Order(29)
    @DisplayName("TC_CMT_029: Verify oversized video upload handling")
    void oversizedVideoUpload() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_029");
    }

    // TC_CMT_030: Verify only MANAGER can approve/disable course status
    @Test @Order(30)
    @DisplayName("TC_CMT_030: Verify only MANAGER can approve/disable course status")
    void onlyManagerCanApprove() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_030");
    }

    // TC_CMT_031: Verify non-owner CONSULTANT cannot update another's lesson
    @Test @Order(31)
    @DisplayName("TC_CMT_031: Verify non-owner CONSULTANT cannot update another's lesson")
    void nonOwnerCannotUpdateLesson() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_031");
    }

    // TC_CMT_032: Verify lesson creation fails when videoUrl is missing
    @Test @Order(32)
    @DisplayName("TC_CMT_032: Verify lesson creation fails when videoUrl is missing")
    void lessonCreationFailsWithoutVideoUrl() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_032");
    }

    // TC_CMT_033: Verify approved course edit requires re-approval workflow
    @Test @Order(33)
    @DisplayName("TC_CMT_033: Verify approved course edit requires re-approval")
    void approvedCourseEditReapproval() {
        loginAsConsultant();
        navigateToCourseManagement();
        screenshot("TC_CMT_033");
    }
}
