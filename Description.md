# **XÂY DỰNG ỨNG DỤNG QUẢN LÝ SINH VIÊN VỚI MONGODB**

> * **Công nghệ phát triển:** Java. 
> * **Cơ sở dữ liệu:** MongoDB (Phiên bản 5.0 trở lên hoặc MongoDB Atlas).  
> * **Hình thức làm việc:** Nhóm

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
  * *Giải thích chi tiết:* Dự án sử dụng Driver chính thức `mongodb-driver-sync` phiên bản `5.0.0` được khai báo trong `pom.xml`. Đây là driver đồng bộ tiêu chuẩn của MongoDB dành cho Java, hỗ trợ đầy đủ các thao tác CRUD và Aggregation Framework.
> * Triển khai mẫu thiết kế **Singleton Pattern** để quản lý đối tượng MongoClient và MongoDatabase, đảm bảo chỉ khởi tạo một kết nối duy nhất trong suốt vòng đời ứng dụng.  
  * *Giải thích chi tiết:* Triển khai qua lớp `com.qlsv.config.MongoDbConnection`. Constructor được khai báo `private` để ngăn việc khởi tạo từ bên ngoài. Phương thức `getInstance()` được khai báo `synchronized` để tránh race condition trong môi trường đa luồng, đảm bảo chỉ tạo duy nhất một kết nối `MongoClient` chạy suốt vòng đời ứng dụng.
> * Đọc chuỗi kết nối từ file cấu hình (appsettings.json, application.properties hoặc .env).
  * *Giải thích chi tiết:* Khi lớp `MongoDbConnection` khởi tạo, hàm constructor sử dụng `Properties.load()` để đọc thông tin `mongodb.uri` và `mongodb.database` từ file `application.properties` nằm trong classpath thông qua `ClassLoader`, giúp thay đổi chuỗi kết nối linh hoạt mà không cần thay đổi code.

### **2\. Thiết kế Giao diện & Xử lý Dữ liệu Động (2.0 Điểm)**

> * **Thông tin cố định:** Cung cấp form nhập liệu Mã SV, Họ tên, Tuổi, Giới tính (Radio button/Combobox), Mã lớp.  
  * *Giải thích chi tiết:* Trong lớp `StudentPanel.java`, hàm `initComponents()` xây dựng giao diện form bằng cách kết hợp `JPanel` sử dụng bố cục `GridLayout(3, 4, 10, 10)` chứa các thành phần Swing chuẩn như `JTextField` (để nhập Mã SV, Họ tên, Tuổi, Mã lớp) và `JComboBox<String>` (để chọn Giới tính Nam/Nữ).
> * **Cơ chế thêm mảng động với nút \[+\]:**  
  * Nút **\[+\] Thêm ngoại ngữ**: Cho phép bấm để sinh thêm ô nhập text hoặc mở dialog nhập ngoại ngữ mới.  
    * *Giải thích chi tiết:* Hàm `StudentPanel.addLanguage()` được kích hoạt khi click nút `[+] Thêm Ngoại ngữ`. Nó hiển thị một hộp thoại nhập liệu (`JOptionPane.showInputDialog`) để lấy chuỗi ngoại ngữ từ người dùng. Sau đó, nó gọi `SinhVienDAO.addNgoaiNgu(masv, ngoaingu)`. DAO sẽ cập nhật mảng `ngoaingu` trong MongoDB bằng toán tử `$addToSet` để đảm bảo không bị trùng lặp phần tử ngoại ngữ.
  * Nút **\[+\] Thêm môn học**: Cho phép bấm để mở vùng nhập bộ 3 thông tin: Mã môn, Tên môn, Điểm số ($0 \\to 10$).  
    * *Giải thích chi tiết:* Hàm `StudentPanel.addSubject()` hiển thị một `JOptionPane.showConfirmDialog` tùy biến chứa 3 trường nhập liệu (`mamon`, `tenmon`, và `diem`). Sau khi kiểm tra điểm số hợp lệ ($0 \to 10$), hàm gom thông tin này thành một đối tượng `Document` môn học con rồi gọi `SinhVienDAO.addMonHoc(masv, monhocDoc)` để dùng toán tử `$push` thêm vào mảng `monhoc`.
> * **Linh hoạt lưu trữ:** Cho phép lưu sinh viên khi mảng ngoaingu hoặc monhoc đang rỗng \[\] và hỗ trợ bổ sung các thông tin này ở các lần cập nhật sau.
  * *Giải thích chi tiết:* Trong hàm `StudentPanel.addStudent()`, khi tiến hành thêm mới một sinh viên, đối tượng `Document` được chuẩn bị sẵn với hai trường mảng `ngoaingu` và `monhoc` khởi tạo là các danh sách rỗng (`new ArrayList<String>()` và `new ArrayList<Document>()`). Điều này cho phép tài liệu được insert thành công vào MongoDB mà không bắt buộc phải có sẵn dữ liệu mảng, và người dùng có thể bổ sung ngoại ngữ/môn học sau này.

### **3\. Thao tác CRUD Cơ bản (2.5 Điểm)**

> * **Thêm mới (Create):** Thêm 1 sinh viên mới vào cơ sở dữ liệu (insertOne).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.addSinhVien(Document doc)` nhận dữ liệu sinh viên đã gom từ form và gọi phương thức `collection.insertOne(doc)` từ driver để trực tiếp lưu trữ vào cơ sở dữ liệu MongoDB.
> * **Hiển thị & Tìm kiếm (Read):**  
  * Tải toàn bộ danh sách sinh viên lên giao diện bảng/DataGrid.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getAllSinhVien()` thực hiện truy vấn `collection.find()` không kèm bộ lọc và nạp toàn bộ kết quả vào một danh sách Java (`into(new ArrayList<>())`). Sau đó, dữ liệu được ánh xạ lên `DefaultTableModel` thông qua hàm `StudentPanel.loadDataToTable()`.
  * Tìm kiếm sinh viên chính xác theo masv.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getSinhVienByMasv(String masv)` thực hiện truy vấn chính xác bằng cách sử dụng bộ lọc `Filters.eq("masv", masv)` kết hợp phương thức `.first()` để lấy tài liệu sinh viên đầu tiên khớp điều kiện từ MongoDB.
  * Lọc danh sách sinh viên theo malop.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getSinhVienByLop(String malop)` truy vấn danh sách sinh viên bằng bộ lọc `Filters.eq("malop", malop)` và lưu các kết quả vào một danh sách thông qua phương thức `into(new ArrayList<>())`.
> * **Cập nhật thông tin (Update):** Cho phép sửa các thông tin cơ bản: Họ tên, Tuổi, Giới tính, Mã lớp theo masv (updateOne với $set).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.updateThongTinCoBan(...)` sử dụng `Updates.combine()` gom các toán tử cập nhật đơn lẻ như `Updates.set("hoten", ...)` để cập nhật đồng thời nhiều trường thông tin cơ bản của sinh viên dựa trên mã sinh viên bằng phương thức `collection.updateOne(filter, update)`.
> * **Xóa dữ liệu (Delete):**  
  * Xóa 1 sinh viên được chọn theo masv (deleteOne).  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.deleteSinhVien(String masv)` gọi phương thức `collection.deleteOne()` với bộ lọc `Filters.eq("masv", masv)` để loại bỏ chính xác 1 document sinh viên tương ứng.
  * Xóa toàn bộ sinh viên thuộc một lớp cụ thể theo malop (deleteMany).
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.deleteSinhVienByLop(String malop)` gọi phương thức `collection.deleteMany()` với bộ lọc `Filters.eq("malop", malop)` để xóa đồng loạt toàn bộ các sinh viên thuộc về mã lớp đó.

### **4\. Xử lý Mảng Nâng cao & Thay thế Document (1.5 Điểm)**

> * **Thêm phần tử vào mảng sau:** Viết chức năng cho phép bổ sung thêm một ngoại ngữ mới hoặc một môn học mới cho sinh viên đã có trong CSDL (sử dụng toán tử $push hoặc $addToSet).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.addNgoaiNgu(...)` sử dụng `Updates.addToSet("ngoaingu", ngoaingu)` để thêm phần tử vào mảng nếu phần tử đó chưa tồn tại trong mảng. Hàm `SinhVienDAO.addMonHoc(...)` sử dụng `Updates.push("monhoc", monhocDoc)` để đẩy trực tiếp phần tử môn học mới vào cuối mảng `monhoc`.
> * **Cập nhật phần tử trong mảng:** Sửa điểm số của một môn học cụ thể dựa vào masv và mamon (sử dụng Positional Operator $).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.updateDiemMonHoc(...)` định vị phần tử trong mảng bằng bộ lọc kép `Filters.and(Filters.eq("masv", masv), Filters.eq("monhoc.mamon", mamon))`. Tiếp đó, hàm thực thi cập nhật bằng toán tử vị trí (Positional Operator `$`) thông qua `Updates.set("monhoc.$.diem", diemMoi)`, giúp thay đổi điểm số của chính xác môn học đã khớp mà không ảnh hưởng đến các môn khác.
> * **Thay thế Document:** Cho phép thay thế toàn bộ nội dung một document sinh viên theo trường \_id (replaceOne).
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.replaceDocument(...)` tìm tài liệu sinh viên cũ dựa trên thuộc tính `_id` (`Filters.eq("_id", id)`) và gọi phương thức `collection.replaceOne(filter, newDoc)` để ghi đè toàn bộ dữ liệu của document bằng document mới.

### **5\. Module Dashboard & Báo Cáo Thống Kê (2.0 Điểm)**

Xây dựng một màn hình Dashboard riêng biệt sử dụng **MongoDB Aggregation Framework**:

> * **Thẻ chỉ số tổng quan (KPI Cards):**  
  * Tổng số sinh viên hiện có.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.countTotalSinhVien(String malop)` sử dụng `collection.countDocuments()` để đếm nhanh số lượng document trong collection, hỗ trợ truyền filter `Filters.eq("malop", malop)` nếu người dùng áp dụng bộ lọc lớp trên giao diện.
  * Tổng số lớp học khác nhau.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.countTotalClasses()` gọi phương thức `collection.distinct("malop", String.class)` của driver để lấy ra danh sách các mã lớp không trùng lặp và dùng hàm `.size()` để trả về tổng số lượng lớp.
  * Điểm trung bình toàn trường (tính gộp từ tất cả môn học của toàn bộ sinh viên).  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getDiemTrungBinh(String malop)` thiết lập Aggregation Pipeline bao gồm: stage `$match` (nếu lọc lớp), stage `$unwind: "$monhoc"` để bung các phần tử trong mảng môn học thành các document đơn lẻ, và stage `$group` gom nhóm toàn bộ dữ liệu (`_id: null`) và gọi `Accumulators.avg("avgDiem", "$monhoc.diem")` để tính trung bình điểm số.
  * Tỷ lệ phần trăm Nam / Nữ.  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getTiLeNamNu(String malop)` áp dụng Aggregation Pipeline bao gồm stage `$group` gom nhóm theo trường giới tính (`_id: "$phai"`) kết hợp accumulator `Accumulators.sum("count", 1)` để đếm số lượng mỗi nhóm, từ đó tính tỷ lệ % trên Swing.
> * **Thống kê Sinh viên theo Lớp:** Bảng hiển thị từng lớp gồm: Mã lớp, Sĩ số, Điểm TB cao nhất, Điểm TB thấp nhất ($group).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.getThongKeTheoLop()` dùng Aggregation Pipeline: stage `$addFields` để tính điểm trung bình cho từng sinh viên thông qua toán tử `$avg` trên mảng `monhoc.diem`, sau đó dùng `$group` gom nhóm theo `$malop` tính `$max` và `$min` trên thuộc tính điểm trung bình vừa tính.
> * **Thống kê Mức độ Phổ biến Ngoại ngữ:** Đếm số lượng sinh viên theo từng loại ngoại ngữ (sử dụng $unwind mảng ngoaingu kết hợp $group và sắp xếp giảm dần).  
  * *Giải thích chi tiết:* Hàm `SinhVienDAO.getThongKeNgoaiNgu(String malop)` triển khai Pipeline: stage `$unwind: "$ngoaingu"` để tách các phần tử trong mảng ngoại ngữ, stage `$group` gom nhóm theo tên ngoại ngữ (`_id: "$ngoaingu"`) để đếm sĩ số qua `$sum`, và cuối cùng `$sort` giảm dần theo số lượng (`count: -1`).
> * **Bảng Xếp hạng & Phân loại:**  
  * Bảng **Top 5 Sinh viên có điểm TB cao nhất** ($sort theo điểm TB, $limit: 5).  
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getTop5SinhVien(String malop)` dùng Aggregation: stage `$addFields` tính điểm trung bình cho mỗi sinh viên, tiếp đó `$sort` giảm dần theo điểm trung bình (`diemTB: -1`), giới hạn lấy ra 5 bản ghi qua `$limit: 5`, và sử dụng `$project` để lọc các trường cần thiết.
  * Biểu đồ/Bảng phân loại học lực: Xuất sắc ($\\ge 8.5$), Giỏi ($7.0 \\to \< 8.5$), Khá ($5.5 \\to \< 7.0$), Trung bình/Yếu ($\< 5.5$).
    * *Giải thích chi tiết:* Hàm `SinhVienDAO.getPhanLoaiHocLuc(String malop)` chạy pipeline Aggregation để bổ sung điểm trung bình cho mỗi sinh viên. Tiếp đó, hàm duyệt qua kết quả trả về trong vòng lặp Java và kiểm tra các mốc điểm số bằng câu lệnh `if-else` để phân nhóm học lực (Xuất sắc, Giỏi, Khá, Trung bình, Yếu) một cách linh hoạt.

### **6\. Tối ưu hóa & Đánh Index (1.0 Điểm)**

> * Viết lệnh/hàm tự động khởi tạo các Index khi ứng dụng vừa chạy:  
  * **Unique Index** cho trường masv (ngăn chặn trùng lặp mã sinh viên).  
    * *Giải thích chi tiết:* Hàm `MongoDbConnection.initIndexes()` sử dụng `collection.createIndex(Indexes.ascending("masv"), new IndexOptions().unique(true))` để yêu cầu MongoDB đảm bảo tính duy nhất của trường `masv`, ngăn chặn việc chèn trùng lặp mã sinh viên.
  * **Compound Index** cho cặp trường { malop: 1, hoten: 1 } (tối ưu hóa thao tác tìm kiếm và sắp xếp theo danh sách lớp).
    * *Giải thích chi tiết:* Hàm `MongoDbConnection.initIndexes()` gọi `collection.createIndex(Indexes.ascending("malop", "hoten"))` để tạo chỉ mục đa trường, giúp tăng hiệu năng khi thực hiện các câu lệnh tìm kiếm, lọc theo lớp học đồng thời sắp xếp danh sách sinh viên theo tên.

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
