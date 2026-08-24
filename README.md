# Ứng dụng Quản lý Sinh viên với MongoDB

Đây là ứng dụng Desktop xây dựng bằng Java Swing kết hợp cơ sở dữ liệu NoSQL MongoDB, đáp ứng đầy đủ yêu cầu quản lý, thao tác mảng động và thống kê Aggregation.

## 1. Yêu cầu Hệ thống
- **Java JDK**: 17 hoặc mới hơn.
- **Maven**: 3.8.x hoặc mới hơn (để tải dependencies và build).
- **MongoDB**: Đã cài đặt MongoDB Server (chạy ở cổng mặc định `localhost:27017`) hoặc cung cấp chuỗi kết nối Atlas.

## 2. Cấu hình Cơ sở dữ liệu
Mở file `src/main/resources/application.properties` để tùy chỉnh chuỗi kết nối:
```properties
# Sửa thành URI nếu dùng MongoDB Atlas hoặc port khác
mongodb.uri=mongodb://localhost:27017
mongodb.database=qlsinhvien_db
```

## 3. Cài đặt và Chạy ứng dụng

### Bằng Maven CLI (Terminal/Command Prompt)
1. Mở terminal tại thư mục gốc dự án (nơi chứa file `pom.xml`).
2. Build dự án:
```bash
mvn clean install
```
3. Chạy file JAR đã được build (bao gồm tất cả dependencies):
```bash
java -jar target/MongoDB_QLSV-1.0-SNAPSHOT-jar-with-dependencies.jar
```
## 4. Dữ liệu thử nghiệm (Data Seed)
- File `data_seed.json` chứa hơn 100 sinh viên mẫu.
- **Tự động Import:** Ứng dụng sẽ tự động đọc file này và nạp dữ liệu vào Database ngay trong lần chạy ĐẦU TIÊN (khi collection `sinhvien` đang rỗng). 
- Các Index (Unique Index trên `masv` và Compound Index trên `{malop: 1, hoten: 1}`) sẽ tự động được tạo ra.

## 5. Các Chức năng chính
- **Quản lý Sinh viên (CRUD):** Thêm, sửa, xóa, tìm kiếm, lọc theo mã SV/Lớp. Bắt lỗi Unique Index nếu trùng mã sinh viên.
- **Xử lý Mảng động ($push, $addToSet, Positional Operator):**
  - Nút **[+] Thêm Ngoại ngữ**: Bổ sung ngôn ngữ mới.
  - Nút **[+] Thêm Môn học**: Mở popup nhập thông tin điểm môn học.
  - Nút **Sửa Điểm Môn**: Chọn mã môn học và cập nhật điểm.
- **Dashboard & Báo cáo (Aggregation Framework):**
  - KPI tổng quan (Tổng số SV, Lớp, Điểm TB, Tỉ lệ Nam/Nữ).
  - Thống kê chi tiết theo Lớp (Sĩ số, ĐTB Cao/Thấp).
  - Đếm ngoại ngữ phổ biến, Top 5 Sinh viên điểm cao.
