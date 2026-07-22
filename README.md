# EduNexus - Course Management System

Hệ thống quản lý khóa học (Course Management System) xây dựng bằng **Spring Boot + Thymeleaf**,
triển khai 26 màn hình cho 2 vai trò **SME** (soạn nội dung khóa học) và **Student** (học viên),
theo đặc tả trong `G1_1-PRD.md` và `List_Screen.md`.

## 1. Yêu cầu môi trường

| Thành phần | Phiên bản |
|---|---|
| JDK | 17 hoặc 21 (⚠️ **không dùng JDK 25** hoặc các bản quá mới — Lombok/Spring Boot 3.3 chưa hỗ trợ, sẽ lỗi biên dịch) |
| SQL Server | 2019+ (hoặc dùng profile `h2` để chạy thử không cần cài gì) |
| Maven | 3.9+ (nếu máy chưa có Maven, xem mục 5) |

## 2. Cấu hình database

Mở `src/main/resources/application.yml`, chỉnh lại thông tin kết nối SQL Server cho đúng máy bạn:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=edunexus;encrypt=true;trustServerCertificate=true
    username: sa
    password: <mật khẩu SQL Server của bạn>
```

Sau đó chạy script tạo database trong thư mục `sql/` — xem chi tiết ở **mục 4**.

## 3. Chạy ứng dụng

### Cách 1 — Chạy với SQL Server thật (khuyến nghị, dùng khi nộp bài / demo chính thức)

```bash
mvn spring-boot:run
```

hoặc chạy trực tiếp class `com.edunexus.EdunexusApplication` từ IDE (IntelliJ/Eclipse), **nhớ đặt
Project SDK là JDK 17 hoặc 21** (File → Project Structure → Project, trong IntelliJ).

Lần chạy đầu tiên, Hibernate sẽ tự tạo toàn bộ bảng và `DataSeeder` sẽ tự chèn dữ liệu mẫu (không
cần chạy tay bất kỳ câu SQL tạo bảng nào).

### Cách 2 — Chạy nhanh không cần cài SQL Server (dùng H2 in-memory, chỉ để test/xem thử)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Dữ liệu sẽ mất khi tắt ứng dụng (in-memory), phù hợp để xem nhanh giao diện mà không cần cài đặt gì.

### Truy cập ứng dụng

Mở trình duyệt tại: **http://localhost:8080/login**

**Tài khoản demo** (mật khẩu chung: `Password123!`):

| Email | Vai trò |
|---|---|
| `sme@edunexus.dev` | SME (soạn nội dung khóa học) |
| `student1@edunexus.dev` | Student |
| `student2@edunexus.dev` | Student |

Đăng nhập SME sẽ vào thẳng "Khóa học của tôi"; đăng nhập Student vào thẳng "Dashboard".

## 4. Script SQL Server (thư mục `sql/`)

| File | Mục đích |
|---|---|
| `sql/01_create_database.sql` | Tạo database `edunexus` (chạy 1 lần duy nhất, trước khi khởi động app lần đầu) |
| `sql/02_insert_test_data.sql` | Chèn thêm dữ liệu test: thêm 1 SME, 2 Student, 1 khóa học "Cơ sở dữ liệu quan hệ" đầy đủ (module/bài giảng/câu hỏi/thẻ ghi nhớ/bài tập), thêm enrollment, tiến trình học, 1 lượt làm quiz đã hoàn thành, 1 bài nộp đã được AI chấm |

**Thứ tự chạy bắt buộc:**
1. Chạy `01_create_database.sql` để tạo database.
2. Khởi động ứng dụng Spring Boot **ít nhất 1 lần** (mục 3, Cách 1) để Hibernate tạo bảng và
   `DataSeeder` chèn dữ liệu nền (SME + 2 Student + khóa học "Nền tảng Lập trình Web"). Có thể tắt
   ứng dụng sau khi thấy log `Started EdunexusApplication`.
3. Chạy `02_insert_test_data.sql` để chèn thêm dữ liệu test.

**Cách chạy:**

- **SQL Server Management Studio (SSMS)**: mở file, bấm Execute (F5). SSMS tự nhận encoding UTF-8
  của file nên tiếng Việt hiển thị đúng ngay.
- **sqlcmd**: **bắt buộc thêm cờ `-f 65001`** để đọc đúng file UTF-8, nếu không tiếng Việt sẽ bị lỗi
  font khi lưu vào database:
  ```bash
  sqlcmd -S localhost -U sa -P "<mật khẩu>" -C -f 65001 -i sql\01_create_database.sql
  sqlcmd -S localhost -U sa -P "<mật khẩu>" -C -f 65001 -i sql\02_insert_test_data.sql
  ```

Script `02_insert_test_data.sql` an toàn chạy lại nhiều lần (mỗi INSERT đều kiểm tra tồn tại trước
theo khóa tự nhiên như email/tiêu đề) nên sẽ không tạo dữ liệu trùng lặp.

Sau khi chạy xong, có thể đăng nhập thêm các tài khoản test mới (cùng mật khẩu `Password123!`):
`sme2@edunexus.dev`, `student3@edunexus.dev`, `student4@edunexus.dev`.

## 5. Nếu máy chưa cài Maven

Tải Apache Maven tại https://maven.apache.org/download.cgi, giải nén, rồi chạy lệnh `mvn` từ thư
mục `bin` đã giải nén (hoặc thêm vào PATH). Ví dụ chạy trực tiếp không cần thêm PATH:

```bash
<đường-dẫn-tới>\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

## 6. Cấu trúc thư mục chính

```
src/main/java/com/edunexus/
  domain/        Entity JPA (User, Course, Module, Lesson, Question, Flashcard, Assignment, Quiz...)
  repository/    Spring Data JPA repositories
  security/      Cấu hình đăng nhập, phân quyền theo vai trò
  service/       Nghiệp vụ; service/ai chứa AI giả lập (mock, không cần API key thật)
  web/           Controller: web/auth, web/sme, web/student
  bootstrap/     DataSeeder - tự chèn dữ liệu mẫu lúc khởi động lần đầu
src/main/resources/
  templates/     Giao diện Thymeleaf (fragments/ dùng chung layout Student & SME)
  application.yml         Cấu hình chính (SQL Server)
  application-h2.yml      Cấu hình profile H2 để test nhanh
sql/             Script tạo database & chèn dữ liệu test
```

## 7. Một số điểm đơn giản hóa (do giới hạn phạm vi 26 màn hình)

- Không có vai trò Admin/Teacher/Course Manager, không có màn thanh toán/đăng ký khóa học — dữ liệu
  ghi danh (Enrollment) được seed sẵn, không có màn hình tự đăng ký.
- Tính năng AI (soạn bài giảng, sinh câu hỏi/thẻ ghi nhớ, chấm luận bằng AI, trích xuất YouTube) đều
  là **mock** (giả lập rule-based), không gọi API AI thật, không cần API key.
- Bài luận sau khi AI chấm hiển thị điểm trực tiếp cho học viên (không có bước Giáo viên xác nhận vì
  không có vai trò Teacher trong phạm vi 26 màn).
- "Thời gian học" trên Personal Progress là số ước tính, không phải thời gian phiên học thực tế.
