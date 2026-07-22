/*
    EduNexus - Tạo database
    -----------------------
    Chạy file này TRƯỚC TIÊN trên SQL Server (SSMS / sqlcmd), với tài khoản có quyền tạo database
    (vd: sa). Sau khi chạy xong, cập nhật username/password của bạn trong
    src/main/resources/application.yml rồi khởi động ứng dụng Spring Boot MỘT LẦN.

    Ứng dụng dùng Hibernate với `spring.jpa.hibernate.ddl-auto: update`, nên khi khởi động lần đầu
    nó sẽ TỰ TẠO toàn bộ bảng (users, courses, modules, lessons, questions, ...) và tự chèn dữ liệu
    mẫu ban đầu (DataSeeder: 1 SME, 2 Student, 1 khóa học đầy đủ).

    Chỉ sau khi ứng dụng đã chạy ít nhất 1 lần (để bảng đã tồn tại), bạn mới chạy tiếp file
    02_insert_test_data.sql để chèn thêm dữ liệu test.
*/

IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = N'edunexus')
BEGIN
    CREATE DATABASE edunexus;
END
GO
