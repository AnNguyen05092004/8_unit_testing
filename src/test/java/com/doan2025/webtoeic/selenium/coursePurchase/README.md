# Selenium E2E Tests — Hướng dẫn chạy

## Yêu cầu trước khi chạy

| Điều kiện | Chi tiết |
|-----------|----------|
| FE đang chạy | `http://localhost:5173` |
| BE đang chạy | `http://localhost:8888` |
| Chrome đã cài | ChromeDriver tự động tải qua WebDriverManager |

---

## 1. Khởi động môi trường

### Khởi động FE (Terminal 1)
```bash
cd "/Users/an/Documents/Ptit Docs/SQA/Đồ test2/FE-dev"
npm run dev
```

### Khởi động BE (Terminal 2 — nếu chưa chạy)
```bash
cd "/Users/an/Documents/Ptit Docs/SQA/Đồ test2/BE-develop"
./mvnw spring-boot:run
```

> Trong bài tập này, DB local mà web đang dùng (`doand21`) được xem như DB test.
> Trước khi chạy, hãy reset/seed lại DB; sau khi chạy xong, hãy restore seed hoặc cleanup các row đã tạo.

Ví dụ seed lại database test:
```bash
cd "/Users/an/Documents/PTIT/SQA/Đồ test2"
./reset-test-db.sh reset
```

Nếu cần dọn lại dữ liệu sau khi test, chạy lại `./reset-test-db.sh reset` để đưa DB về seed ban đầu.

---

## 2. Chạy test

> Tất cả lệnh `./mvnw` phải chạy từ thư mục `BE-develop/`

```bash
cd "/Users/an/Documents/Ptit Docs/SQA/Đồ test2/BE-develop"


### Chạy class Page Object
```bash
./mvnw -Dselenium.e2e=true \
  -De2e.headless=false \
  -De2e.slowMillis=1000 \
  -De2e.student.email=student@gmail.com \
  -De2e.student.password=abcd@1234 \
  -Dtest=CoursePurchaseFlowPageObjectSeleniumTest test
```

Sau khi chạy xong, nếu test đã tạo cart item, order pending, enrollment hoặc payment callback data thì restore seed lại để DB quay về trạng thái sạch.

### Chạy class Regression (2 bug đã phát hiện)
```bash
./mvnw -Dselenium.e2e=true \
  -De2e.headless=false \
  -De2e.slowMillis=1000 \
  -De2e.student.email=student@gmail.com \
  -De2e.student.password=abcd@1234 \
  -Dtest=BugRegressionSeleniumTest test
```

### Chạy tất cả E2E (cả 3 class)
```bash
./mvnw -Dselenium.e2e=true \
  -De2e.headless=false \
  -De2e.slowMillis=1000 \
  -De2e.student.email=student@gmail.com \
  -De2e.student.password=abcd@1234 \
  "-Dtest=CoursePurchaseFlowSeleniumTest,CoursePurchaseFlowPageObjectSeleniumTest,BugRegressionSeleniumTest" test
```

### Chạy nhanh (headless, không delay — dùng cho CI)
```bash
./mvnw -Dselenium.e2e=true \
  -De2e.headless=true \
  -De2e.slowMillis=0 \
  -De2e.student.email=student@gmail.com \
  -De2e.student.password=abcd@1234 \
  "-Dtest=CoursePurchaseFlowSeleniumTest,CoursePurchaseFlowPageObjectSeleniumTest,BugRegressionSeleniumTest" test
```

### Xem báo cáo Allure (sau khi chạy test)

**Mở live server (khuyến nghị):**
```bash
./mvnw io.qameta.allure:allure-maven:serve
```
Lệnh tự khởi động Jetty server và mở trình duyệt với báo cáo đầy đủ (biểu đồ, timeline, log từng bước).

**Tạo báo cáo tĩnh thành file HTML:**
```bash
./mvnw io.qameta.allure:allure-maven:report
```
File được tạo tại `target/site/allure-maven-plugin/index.html`.

> Dữ liệu kết quả test (JSON) được lưu tại `allure-results/` trong thư mục `BE-develop/` sau mỗi lần chạy.

---

## 3. Các tham số tuỳ chỉnh

| Tham số | Mặc định | Ý nghĩa |
|---------|----------|---------|
| `selenium.e2e` | *(bắt buộc đặt = `true`)* | Bật E2E test |
| `e2e.baseUrl` | `http://localhost:5173` | URL của FE |
| `e2e.student.email` | `student@gmail.com` | Tài khoản student |
| `e2e.student.password` | `abcd@1234` | Mật khẩu |
| `e2e.headless` | `false` | `true` = chạy ẩn (không mở browser) |
| `e2e.slowMillis` | `600` | Delay giữa mỗi bước (ms). `0` = nhanh nhất, `2000` = chậm để quan sát |

---

## 4. Cấu trúc thư mục test

```
e2e/
├── CoursePurchaseFlowSeleniumTest.java         ← class demo gốc (inline, không POM)
├── CoursePurchaseFlowPageObjectSeleniumTest.java  ← class dùng Page Object
├── BugRegressionSeleniumTest.java              ← regression test 2 bug đã phát hiện
├── README.md
└── pages/
    ├── BasePage.java        ← abstract base (wait helpers, click, type...)
    ├── LoginPage.java       ← đăng nhập qua modal
    ├── CoursesPage.java     ← duyệt danh sách khóa học, thêm vào giỏ, mua ngay
    ├── CartPage.java        ← mini-cart, full cart, mua ngay, assert giỏ hàng
    ├── OrdersPage.java      ← lịch sử đơn hàng
    └── OrderStatusPage.java ← trang kết quả thanh toán
```

---

## 5. Các test case

### CoursePurchaseFlowSeleniumTest & CoursePurchaseFlowPageObjectSeleniumTest

| Test | Loại | Mô tả |
|------|------|-------|
| `shouldPurchaseCourseFromCartAndVerifyOrderHistory` | Positive | Đăng nhập → thêm khóa học vào giỏ → mini-cart → full cart → mua ngay → xác nhận đơn hàng → thanh toán mock → xác nhận thành công |
| `shouldShowFailedPaymentStatusOnOrderStatusPage` | Negative | Điều hướng đến URL thanh toán thất bại → xác nhận hiển thị "Thanh toán thất bại" |

### BugRegressionSeleniumTest

| Test | Bug | Loại | Mô tả |
|------|-----|------|-------|
| `shouldRemovePurchasedCourseFromCartAfterPayment` | Bug 1 | Negative | Thêm 2 khóa vào giỏ → mở mini-cart → bấm "Mua ngay" cho 1 khóa → thanh toán mock thành công → assert khóa đã mua **bị xóa** khỏi giỏ, khóa còn lại **vẫn còn**, badge = 1 |
| `shouldShowContinueToCheckoutButtonWhenPendingOrderExists` | Bug 2 | Negative | Bấm "Mua ngay" trực tiếp từ trang /courses → tạo PENDING order → quay lại /courses → assert nút đổi thành "Tiếp tục thanh toán" → nếu nút vẫn là "Mua ngay" thì bấm thử và xác nhận lỗi hiện ra |

---

## 6. Mô tả 2 bug đã phát hiện

### Bug 1 — Khóa học đã mua không bị xóa khỏi giỏ hàng

| | |
|---|---|
| **Tình huống** | Giỏ hàng có nhiều khóa học. Bấm "Mua ngay" cho 1 khóa trong mini-cart → thanh toán VNPay thành công |
| **Kết quả mong đợi** | Khóa học vừa mua bị xóa khỏi giỏ. Badge giảm. Các khóa còn lại không bị ảnh hưởng |
| **Kết quả thực tế** | Khóa đã mua vẫn còn trong giỏ. Nếu bấm "Mua ngay" lại → báo lỗi "đơn hàng đã tồn tại" |
| **Test phát hiện** | `shouldRemovePurchasedCourseFromCartAfterPayment` |

### Bug 2 — Nút "Mua ngay" không đổi thành "Tiếp tục thanh toán" khi có đơn PENDING

| | |
|---|---|
| **Tình huống** | Bấm "Mua ngay" trên trang /courses → đơn hàng PENDING được tạo → quay lại xem card khóa học |
| **Kết quả mong đợi** | Nút chuyển sang "Tiếp tục thanh toán" (màu vàng), dẫn tới /dashboard/orders |
| **Kết quả thực tế** | Nút vẫn hiển thị "Mua ngay". Bấm lại → hiện thông báo lỗi từ API |
| **Test phát hiện** | `shouldShowContinueToCheckoutButtonWhenPendingOrderExists` |

---

## 7. Selenium trong dự án thực tế

### 7.1 Vị trí của E2E test trong quy trình phát triển

Một dự án thực tế thường có 3 tầng test, gọi là **Test Pyramid**:

```
        /‾‾‾‾‾‾‾‾‾\
       /  E2E Test  \     ← ít, chậm, đắt (Selenium nằm ở đây)
      /‾‾‾‾‾‾‾‾‾‾‾‾‾\
     / Integration Test\  ← vừa (API test)
    /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
   /     Unit Test      \  ← nhiều, nhanh, rẻ nhất
  /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
```

Selenium chỉ bao phủ tầng trên cùng — **chứng minh luồng nghiệp vụ quan trọng hoạt động đúng từ góc nhìn người dùng**, không thay thế unit test.

---

### 7.2 Những luồng thường được tự động hóa bằng Selenium

| Loại luồng | Ví dụ thực tế |
|---|---|
| **Luồng mua hàng/thanh toán** | Thêm giỏ → checkout → thanh toán → xác nhận (như project này) |
| **Luồng đăng ký/đăng nhập** | Tạo tài khoản → xác thực email → đăng nhập |
| **Luồng CRUD quan trọng** | Admin tạo/sửa/xóa sản phẩm, course, user |
| **Luồng phân quyền** | Kiểm tra user thường không thấy trang admin |
| **Smoke test sau deploy** | Chạy 5–10 test cốt lõi ngay sau khi release để đảm bảo hệ thống hoạt động |

---

### 7.3 Cách tích hợp vào CI/CD thực tế

Trong pipeline CI (GitHub Actions, Jenkins, GitLab CI...), E2E test thường chạy như sau:

```
Push code
    ↓
Build + Unit test  (nhanh, 1–5 phút)
    ↓
Deploy lên môi trường staging
    ↓
Chạy E2E test headless  (5–30 phút)
    ↓
Nếu pass → deploy lên production
Nếu fail → chặn deployment + thông báo team
```

Lệnh CI thường trông như thế này (không cần `SLOW_MILLIS`, `headless=true`):

```bash
./mvnw -Dselenium.e2e=true \
  -De2e.headless=true \
  -De2e.slowMillis=0 \
  -De2e.baseUrl=https://staging.myapp.com \
  -De2e.student.email=$TEST_EMAIL \
  -De2e.student.password=$TEST_PASSWORD \
  test
```

> Tài khoản test được lưu trong **secret của CI**, không hard-code vào code.

---

### 7.4 Những điểm khác biệt so với project này

| Điểm | Project này | Dự án thực tế |
|---|---|---|
| **Framework** | Selenium thuần | Thường bọc thêm Serenity BDD, Cucumber, hoặc dùng Playwright |
| **Test data** | Dùng data có sẵn trong DB | Tạo data mới trước test, xóa sau test (test isolation) |
| **Môi trường** | Local | Dedicated staging environment |
| **Tài khoản test** | Hard-code trong lệnh | CI secret / `.env` file |
| **Báo cáo** | Allure, Surefire | Allure + thông báo Slack/Teams khi fail |
| **Screenshot khi fail** | Chưa có | Gần như bắt buộc để debug |

---

### 7.5 Điểm mạnh và giới hạn của Selenium

**Điểm mạnh:**
- Kiểm thử giao diện thực sự như người dùng
- Phát hiện bug tích hợp giữa FE–BE–DB
- Tự động hóa regression test sau mỗi release

**Giới hạn cần biết:**
- Chậm hơn unit test nhiều lần
- Dễ bị **flaky** (lúc pass lúc fail) nếu không wait đúng cách
- Khó bảo trì khi UI thay đổi thường xuyên
- Không phù hợp để kiểm thử logic nghiệp vụ chi tiết (dùng unit test thay)

