# **XÂY DỰNG ỨNG DỤNG QUẢN LÝ SINH VIÊN VỚI MONGODB**

> * **Công nghệ phát triển:** Java. 
> * **Cơ sở dữ liệu:** MongoDB (Phiên bản 5.0 trở lên hoặc MongoDB Atlas).  
> * **Hình thức làm việc:** Cá nhân.

## **I. THIẾT KẾ CSDL & CẤU TRÚC DOCUMENT**

Hệ thống sử dụng một Database duy nhất (ví dụ: qlsinhvien\_db) và mô hình hóa dữ liệu trong collection sinhvien.  
**Cấu trúc Document mẫu:**

{  
  "\_id": ObjectId("64f1a2b3c4d5e6f7a8b9c0d1"),  
  "masv": "sv001",  
  "hoten": "Nguyễn Văn An",  
  "tuoi": 20,  
  "phai": "Nam",  
  "malop": "l01",  
  "ngoaingu": \["Tiếng Anh", "Tiếng Nhật"\],  
  "monhoc": \[  
    {  
      "mamon": "csdl",  
      "tenmon": "Cơ sở dữ liệu",  
      "diem": 8.5  
    },  
    {  
      "mamon": "laptrinh",  
      "tenmon": "Lập trình Cơ bản",  
      "diem": 7.0  
    }  
  \]  
}

## 

## **II. YÊU CẦU CHỨC NĂNG CHI TIẾT (10 ĐIỂM)**

### **1\. Kiến trúc & Quản lý Kết nối (1.0 Điểm)**

> * Sử dụng thư viện Driver chính thức: MongoDB.Driver (.NET) hoặc mongodb-driver-sync (Java).  
> * Triển khai mẫu thiết kế **Singleton Pattern** để quản lý đối tượng MongoClient và MongoDatabase, đảm bảo chỉ khởi tạo một kết nối duy nhất trong suốt vòng đời ứng dụng.  
> * Đọc chuỗi kết nối từ file cấu hình (appsettings.json, application.properties hoặc .env).

### **2\. Thiết kế Giao diện & Xử lý Dữ liệu Động (2.0 Điểm)**

> * **Thông tin cố định:** Cung cấp form nhập liệu Mã SV, Họ tên, Tuổi, Giới tính (Radio button/Combobox), Mã lớp.  
> * **Cơ chế thêm mảng động với nút \[+\]:**  
  * Nút **\[+\] Thêm ngoại ngữ**: Cho phép bấm để sinh thêm ô nhập text hoặc mở dialog nhập ngoại ngữ mới.  
  * Nút **\[+\] Thêm môn học**: Cho phép bấm để mở vùng nhập bộ 3 thông tin: Mã môn, Tên môn, Điểm số ($0 \\to 10$).  
> * **Linh hoạt lưu trữ:** Cho phép lưu sinh viên khi mảng ngoaingu hoặc monhoc đang rỗng \[\] và hỗ trợ bổ sung các thông tin này ở các lần cập nhật sau.

### **3\. Thao tác CRUD Cơ bản (2.5 Điểm)**

> * **Thêm mới (Create):** Thêm 1 sinh viên mới vào cơ sở dữ liệu (insertOne).  
> * **Hiển thị & Tìm kiếm (Read):**  
  * Tải toàn bộ danh sách sinh viên lên giao diện bảng/DataGrid.  
  * Tìm kiếm sinh viên chính xác theo masv.  
  * Lọc danh sách sinh viên theo malop.  
> * **Cập nhật thông tin (Update):** Cho phép sửa các thông tin cơ bản: Họ tên, Tuổi, Giới tính, Mã lớp theo masv (updateOne với $set).  
> * **Xóa dữ liệu (Delete):**  
  * Xóa 1 sinh viên được chọn theo masv (deleteOne).  
  * Xóa toàn bộ sinh viên thuộc một lớp cụ thể theo malop (deleteMany).

### **4\. Xử lý Mảng Nâng cao & Thay thế Document (1.5 Điểm)**

> * **Thêm phần tử vào mảng sau:** Viết chức năng cho phép bổ sung thêm một ngoại ngữ mới hoặc một môn học mới cho sinh viên đã có trong CSDL (sử dụng toán tử $push hoặc $addToSet).  
> * **Cập nhật phần tử trong mảng:** Sửa điểm số của một môn học cụ thể dựa vào masv và mamon (sử dụng Positional Operator $).  
> * **Thay thế Document:** Cho phép thay thế toàn bộ nội dung một document sinh viên theo trường \_id (replaceOne).

### **5\. Module Dashboard & Báo Cáo Thống Kê (2.0 Điểm)**

Xây dựng một màn hình Dashboard riêng biệt sử dụng **MongoDB Aggregation Framework**:

> * **Thẻ chỉ số tổng quan (KPI Cards):**  
  * Tổng số sinh viên hiện có.  
  * Tổng số lớp học khác nhau.  
  * Điểm trung bình toàn trường (tính gộp từ tất cả môn học của toàn bộ sinh viên).  
  * Tỷ lệ phần trăm Nam / Nữ.  
> * **Thống kê Sinh viên theo Lớp:** Bảng hiển thị từng lớp gồm: Mã lớp, Sĩ số, Điểm TB cao nhất, Điểm TB thấp nhất ($group).  
> * **Thống kê Mức độ Phổ biến Ngoại ngữ:** Đếm số lượng sinh viên theo từng loại ngoại ngữ (sử dụng $unwind mảng ngoaingu kết hợp $group và sắp xếp giảm dần).  
> * **Bảng Xếp hạng & Phân loại:**  
  * Bảng **Top 5 Sinh viên có điểm TB cao nhất** ($sort theo điểm TB, $limit: 5).  
  * Biểu đồ/Bảng phân loại học lực: Xuất sắc ($\\ge 8.5$), Giỏi ($7.0 \\to \< 8.5$), Khá ($5.5 \\to \< 7.0$), Trung bình/Yếu ($\< 5.5$).

### **6\. Tối ưu hóa & Đánh Index (1.0 Điểm)**

> * Viết lệnh/hàm tự động khởi tạo các Index khi ứng dụng vừa chạy:  
  * **Unique Index** cho trường masv (ngăn chặn trùng lặp mã sinh viên).  
  * **Compound Index** cho cặp trường { malop: 1, hoten: 1 } (tối ưu hóa thao tác tìm kiếm và sắp xếp theo danh sách lớp).

## **III. YÊU CẦU PHI CHỨC NĂNG & VALIDATION**

> * Validate dữ liệu đầu vào:  
  * Điểm số môn học chỉ được nằm trong thang điểm từ $0.0$ đến $10.0$.  
  * Tuổi sinh viên phải là số nguyên dương hợp lệ ($\> 0$).  
  * Bắt lỗi ngoại lệ khi vi phạm Unique Index (thông báo người dùng nếu nhập trùng masv).  
> * Giao diện trực quan, rõ ràng, có thông báo xác nhận trước khi thực hiện hành động xóa dữ liệu.

## **IV. HÌNH THỨC NỘP BÀI & TIÊU CHÍ ĐÁNH GIÁ**

> 1. **Hồ sơ nộp bài:**  
   * Toàn bộ mã nguồn dự án được đẩy lên GitHub/GitLab (kèm link nộp bài).  
   * File README.md mô tả cách cấu hình connection string, cài đặt dependencies và các bước chạy ứng dụng.  
   * File script data\_seed.js hoặc data\_seed.json chứa sẵn dữ liệu kiểm thử (tối thiểu 100 sinh viên).  
> 2. **Hình thức chấm thi:**  
   * Vấn đáp và demo trực tiếp ứng dụng.  
   * Kiểm tra code trực tiếp: Thao tác thêm mảng động $\\to$ Cập nhật điểm trong mảng $\\to$ Kiểm tra kết quả số liệu trên màn hình Dashboard $\\to$ Kiểm tra lỗi trùng khóa Unique Index.