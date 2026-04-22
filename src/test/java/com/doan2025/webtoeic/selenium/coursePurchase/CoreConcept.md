# Core Concept - Selenium E2E trong project này

Tài liệu này giải thích các kiến thức cơ bản để bạn đọc và hiểu toàn bộ bộ test đã tạo.

## 1) Mục tiêu của bộ test

Bộ test hiện tại có 3 nhóm:

1. Demo test kiểu inline (tất cả viết trong một class)
2. Demo test kiểu Page Object Model (tách lớp trang)
3. Regression test để bắt 2 bug đã xác nhận

Ý nghĩa:

- Test demo: chứng minh luồng mua khóa học hoạt động
- Test regression: chứng minh bug vẫn tồn tại (nếu chưa fix thì test fail là đúng)

---

## 2) JUnit 5 căn bản

### 2.1 Annotation quan trọng

- `@Test`: đánh dấu một hàm test
- `@BeforeEach`: chạy trước mỗi test (thường để mở browser)
- `@AfterEach`: chạy sau mỗi test (thường để đóng browser)
- `@DisplayName("...")`: đặt tên để đọc report dễ hơn
- `@EnabledIfSystemProperty(...)`: chỉ chạy test khi bật cờ thông qua -D

Ví dụ ý nghĩa:

- Nếu không truyền `-Dselenium.e2e=true` thì test selenium sẽ bị bỏ qua

### 2.2 Assertion căn bản

- `Assertions.assertEquals(expected, actual)`
- `Assertions.assertTrue(condition, message)`
- `Assertions.assertFalse(condition, message)`

Mục đích:

- So sánh giá trị mong đợi với giá trị thực tế
- Nếu sai, test fail và hiện message giúp debug

---

## 3) Selenium căn bản trong project

### 3.1 Khởi tạo browser

Trong `setUp()`:

1. Tạo `ChromeOptions`
2. Tùy chọn headless bằng system property `e2e.headless`
3. `WebDriverManager.chromedriver().setup()` để tự tìm driver
4. Tạo `new ChromeDriver(options)`

Trong `tearDown()`:

- `driver.quit()` để đóng toàn bộ browser/session

### 3.2 Các thành phần hay gặp

- `WebDriver`: điều khiển trình duyệt
- `By`: định vị phần tử (`By.xpath`, `By.cssSelector`)
- `WebElement`: đối tượng element trên trang
- `WebDriverWait + ExpectedConditions`: đợi element/url theo điều kiện

### 3.3 Vì sao ưu tiên explicit wait

Test UI hay bị trễ do render API. Nếu click ngay sẽ flaky.

Vì vậy bộ test đang dùng:

- `waitVisible(...)`
- `waitUrlContains(...)`
- `waitInvisible(...)`

Đây là cách dùng trong production test.

---

## 4) Hệ thống tham số -D (system properties)

Bộ test lấy cấu hình từ command line:

- `e2e.baseUrl`: URL FE
- `e2e.student.email`: tài khoản test
- `e2e.student.password`: mật khẩu test
- `e2e.headless`: true/false
- `e2e.slowMillis`: độ trễ để quan sát demo
- `selenium.e2e`: có bật test selenium hay không

Ví dụ:

```bash
./mvnw -Dselenium.e2e=true -De2e.headless=false -De2e.slowMillis=1000 -Dtest=CoursePurchaseFlowPageObjectSeleniumTest test
```

---

## 5) Page Object Model (POM) căn bản

### 5.1 Ý tưởng

Thay vì viết hết thao tác trong 1 class test, ta tách mỗi trang thành 1 class:

- `LoginPage`
- `CoursesPage`
- `CartPage`
- `OrdersPage`
- `OrderStatusPage`

`BasePage` chứa hàm dùng chung:

- click, type, wait, openPath, sleep, pause

### 5.2 Lợi ích

1. Test dễ đọc hơn (đọc như business flow)
2. Tái sử dụng được thao tác
3. Đổi locator 1 chỗ là đủ
4. Dễ bảo trì khi FE đổi giao diện

---

## 6) Giải thích từng test đã tạo

## 6.1 `CoursePurchaseFlowSeleniumTest`

Loại:

- Inline demo (không POM)

Mục tiêu:

- Mua khóa học từ giỏ hàng
- Kiểm tra lịch sử đơn hàng

Bản chất:

- Đây là bản để học thông suốt toàn bộ step Selenium

## 6.2 `CoursePurchaseFlowPageObjectSeleniumTest`

Loại:

- Demo chuẩn hơn với POM

Flow chính:

1. Đăng nhập
2. Xóa giỏ hàng nếu cần
3. Thêm khóa học có thể mua
4. Qua mini-cart -> full cart
5. Assert title/price/total
6. Mua ngay
7. Qua orders và lấy orderId
8. Mở mock thanh toán thành công
9. Bấm "Xem khóa học đã mua"
10. Assert có thể thấy khóa học trong video-courses với tag đã mua
11. Mở lịch sử đơn hàng và assert đơn tồn tại

Ghi chú:

- Đã đổi `pause(1000)` thành `pause(SLOW_MILLIS)` để nhất quán với cấu hình

## 6.3 `BugRegressionSeleniumTest`

Mục tiêu:

- Bắt 2 bug từ screenshot/manual test

Bug 1:

- Sau khi mua 1 khóa học từ mini-cart, khóa học đó phải biến mất khỏi cart
- Nếu nó vẫn còn trong cart -> test fail (có chữ "BUG DETECTED [Bug 1]")

Bug 2:

- Khi đã có pending order, nút trên card course phải là "Tiếp tục thanh toán"
- Nếu vẫn là "Mua ngay" -> test fail (có chữ "BUG DETECTED [Bug 2]")

Kết luận quan trọng:

- Regression test fail trong trường hợp này là kết quả đúng vì nó đang chứng minh bug tồn tại.

---

## 7) Đọc log test như thế nào

Khi Maven in:

- `Failures`: assertion sai (nghiệp vụ sai)
- `Errors`: lỗi runtime/wait timeout/element không tìm thấy

Ví dụ:

- `TimeoutException` tại `waitVisible(...)` -> UI không đến trạng thái mong đợi trong thời gian chờ
- `assertEquals` fail -> dữ liệu thực tế khác mong đợi

Thư mục cần xem thêm:

- `target/surefire-reports`

---

## 8) Các locators và thực hành tốt

Locators đang dùng:

- CSS selectors cho class rõ ràng
- XPath cho text tiếng Việt (Mua ngay, Thanh toán, Đã mua...)

Best practice cơ bản:

1. Ưu tiên locator ổn định (id, data-testid nếu có)
2. Hạn chế locator quá dài/dễ vỡ
3. Sau mỗi action quan trọng nên wait trạng thái tiếp theo

---

## 9) Slow mode và demo mode

`slowMillis` có 2 vai trò:

1. Làm chậm để người xem demo theo kịp
2. Giảm rung lắc nhẹ do animation/UI

Khuyến nghị:

- Demo: `-De2e.slowMillis=800` đến `1200`
- Chạy nhanh/CI: `-De2e.slowMillis=0` và `-De2e.headless=true`

---

## 10) Allure report trong project này

Tình trạng cấu hình:

- Đã thêm `allure-junit5`
- Đã thêm `allure-maven plugin`
- Kết quả lưu ở thư mục `allure-results` tại root module

Cách xem report đầy đủ:

1. Chạy test để tạo dữ liệu
2. Chạy `./mvnw io.qameta.allure:allure-maven:serve`
3. Mở URL localhost mà terminal in ra

Lưu ý:

- File `allure-maven.html` là trang wrapper của Maven Site, không phải UI report chính

---

## 11) Checklist ôn nhanh trước buổi demo

1. FE và BE đã run
2. Tài khoản test còn dùng được
3. Chạy `CoursePurchaseFlowPageObjectSeleniumTest` pass
4. Chạy `BugRegressionSeleniumTest` và giải thích vì sao fail là hợp lý
5. Mở surefire report hoặc Allure serve để show kết quả

---

## 12) Từ điển thuật ngữ nhanh

- E2E test: test xuyên suốt từ UI đến kết quả cuối
- POM: mẫu thiết kế tách trang thành class
- Regression test: test đảm bảo bug cũ không quay lại
- Flaky test: test lúc pass lúc fail không ổn định
- Assertion: câu lệnh kiểm tra mong đợi
- Headless: chạy browser ẩn, không hiện cửa sổ UI

---

## 13) Lời khuyên học tiếp

1. Học thêm về `data-testid` để locator bền hơn
2. Thêm screenshot khi fail (phục vụ report)
3. Tiếp tục tách base class dùng chung cho nhiều test class
4. Tự động hóa test data để giảm phụ thuộc DB có sẵn

Bạn đã làm rất đúng hướng. Chỉ cần nắm chắc các mục 2, 3, 5, 6, 7 trong tài liệu này là có thể giải thích bộ test rất tốt trong buổi báo cáo.
