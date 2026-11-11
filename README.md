# PTIT CSDL - Nhóm 30: Quản lý Rạp chiếu phim

Bộ hồ sơ này hoàn thiện phần 3 theo schema trong báo cáo PDF của nhóm, gồm:

- `database/database.sql`: tạo database, tạo bảng, dữ liệu mẫu và 13 truy vấn đã sửa.
- `src/main/java/...`: Spring Boot REST API thao tác MySQL.
- CRUD + tìm kiếm cho `Phim` và `KhachHang`.
- Tài khoản CUSTOMER/STAFF/ADMIN; khách đăng ký và đăng nhập bằng số điện thoại + mật khẩu, khóa sau 5 lần sai, admin mở khóa và mời nhân viên.
- Trang đăng nhập/đăng ký, hồ sơ/lịch sử khách hàng và giao diện quản trị tài khoản.
- Ảnh QR tĩnh Agribank; khách nhập số tiền và mã đơn riêng, ghế được giữ tạm và admin xác nhận chuyển khoản thủ công.
- Trang quản trị thanh toán có doanh thu, lịch sử giao dịch, thông tin suất/ghế và thao tác xác nhận/hủy.
- `pom.xml`: Maven project.

## 1. Chạy MySQL

Khuyến nghị MySQL 8.x.

1. Mở MySQL Workbench.
2. Mở `database/database.sql`.
3. Run toàn bộ file.
4. Kiểm tra database `QuanLyRapChieuPhim`.

> **Lưu ý:** Script xóa và tạo lại database `QuanLyRapChieuPhim` từ đầu. Sao lưu dữ liệu trước khi chạy; không chạy trên database đang có dữ liệu cần giữ lại.

Nếu tài khoản MySQL của bạn không phải `root/root`, có thể set biến môi trường trước khi chạy:

```properties
set DB_USERNAME=root
spring.datasource.password=root
```

trong `src/main/resources/application.properties`.

## 2. Chạy Spring Boot

Yêu cầu Java 17+ và Maven 3.9+.

```bash
mvn spring-boot:run
```

Hoặc:

```bash
mvn clean package
java -jar target/cinema-db-app-1.0.0.jar
```

Server mặc định: `http://localhost:8080`.

Trang đăng nhập/đăng ký: `http://localhost:8080/DangNhap.html` (mở `/` cũng sẽ chuyển đến đây). Quản lý phim/khách hàng yêu cầu tài khoản STAFF hoặc ADMIN: `http://localhost:8080/Database.html`.

Giao diện tài khoản khách hàng: `http://localhost:8080/TaiKhoan.html`; admin quản lý tài khoản tại `http://localhost:8080/QuanTri.html`, thanh toán/doanh thu tại `http://localhost:8080/QuanTriThanhToan.html`, và suất chiếu/giá vé tại `http://localhost:8080/QuanTriLichChieu.html`. Giao diện đặt vé: `http://localhost:8080/DatVe.html`, chỉ dành cho khách đã đăng nhập. Khách không cần email; nhân viên vẫn dùng email do admin cấp. Giá mặc định giữ theo hệ thống hiện tại: ghế thường 100.000₫, ghế VIP 50.000₫.

Các trang giao diện hỗ trợ Tiếng Việt/English và sáng/tối; lựa chọn được lưu trong trình duyệt và dùng chung giữa các trang.

### Cài đặt xác thực (bắt buộc trước khi chạy)

Với database đang có, import lần lượt `database/migrations/V1__account_security.sql`, `database/migrations/V2__customer_phone_login.sql`, `database/migrations/V3__qr_transfer_payments.sql`, rồi `database/migrations/V4__schedule_management.sql` trong phpMyAdmin/Workbench. V3 bổ sung trạng thái thanh toán, giữ ghế và snapshot ghế; V4 tạo bảng giá vé ban đầu theo mức đang dùng (thường 100.000₫, VIP 50.000₫). Các migration không drop database. Nếu V1–V3 đã chạy trước đây thì chỉ chạy V4 một lần. Không chạy lại `database/database.sql` nếu cần giữ dữ liệu vì file đó drop và tạo lại toàn bộ database.

Khách hàng có thể đăng ký và đăng nhập bằng số điện thoại cùng mật khẩu, không cần cấu hình SMTP. SMTP chỉ cần nếu muốn admin gửi thư mời tài khoản nhân viên. Đặt biến bootstrap admin trong PowerShell terminal dùng để chạy ứng dụng; không ghi mật khẩu admin vào file hoặc commit:

```powershell
$env:APP_BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = "..."
mvn spring-boot:run
```

Admin đầu tiên chỉ được tạo một lần từ cặp biến `APP_BOOTSTRAP_ADMIN_*`; sau khi khởi tạo có thể bỏ hai biến này. SMTP (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`) và `APP_PUBLIC_BASE_URL` chỉ cần cấu hình nếu sử dụng tính năng mời nhân viên qua email. Trên môi trường HTTPS đặt `SESSION_COOKIE_SECURE=true`.

Ảnh QR tĩnh được đóng gói tại `src/main/resources/static/qr-chuyen-khoan-agribank.png`; không cần cấu hình bốn biến `PAYMENT_BANK_*` hoặc `PAYMENT_ACCOUNT_*`. Khách phải tự nhập số tiền và mã tham chiếu hiện trên đơn vì QR tĩnh không tự điền chúng. Có thể đặt `PAYMENT_TRANSFER_EXPIRY_MINUTES` (mặc định 15, từ 1 đến 120 phút). Ngân hàng không gửi xác nhận tự động, nên chỉ xác nhận đơn trong trang quản trị sau khi tự đối chiếu giao dịch. Đơn hết hạn được nhả ghế; khoản tiền đến muộn cần được xử lý/hoàn thủ công.

- Khách hàng đăng ký bằng họ tên, số điện thoại và mật khẩu; không cần email hay bước xác minh email. Số điện thoại phải là duy nhất.
- Tài khoản khách hàng cũ được chuyển sang đăng nhập bằng số điện thoại khi chạy migration V2; số điện thoại trùng hoặc không hợp lệ cần được cập nhật riêng trước khi đăng nhập.
- Mật khẩu băm BCrypt, tối thiểu 12 ký tự; 5 lần đăng nhập sai liên tiếp sẽ khóa tài khoản đến khi admin mở khóa.
- Admin mời nhân viên từ danh sách `NhanVien`; nhân viên tự đặt mật khẩu qua liên kết dùng một lần, hết hạn sau 48 giờ.
- API nghiệp vụ kiểm tra role ở server; session dùng cookie HttpOnly/SameSite và các yêu cầu ghi dữ liệu phải có CSRF token.
- Nên cấu hình ít nhất hai tài khoản ADMIN để vẫn có người mở khóa nếu một admin bị khóa.

> Dữ liệu suất chiếu mẫu mang ngày năm 2024 để phục vụ các truy vấn báo cáo. Dùng trang quản trị lịch chiếu để tạo lịch tương lai; các lịch mẫu đã qua vẫn có thể xuất hiện trong danh sách đặt vé.

### Quản lý phim, suất chiếu và giá vé

- Thông tin phim (thêm/sửa/xóa) vẫn ở `Database.html`, dành cho STAFF/ADMIN.
- Admin quản lý lịch tại `http://localhost:8080/QuanTriLichChieu.html`: tạo/sửa/xóa suất trong các phòng có sẵn; phòng không thể bị trùng lịch; suất mới phải ở tương lai. Màn hình cũng hiển thị sức chứa, số vé đã bán, số ghế đang giữ và số suất theo phòng.
- Có thể cập nhật giá thường/VIP trên cùng màn hình; giá mới chỉ áp dụng cho đơn tạo sau đó. Giá và ghế của hóa đơn cũ được lưu nguyên trạng.
- Không thể sửa/xóa suất đã có vé, lịch sử đơn hoặc ghế đang được giữ. Muốn tạo nhiều suất, thêm từng suất ở giờ/phòng không giao nhau. Quản lý thêm phòng mới/sơ đồ ghế chưa có giao diện.
- Các API admin (chỉ role ADMIN): `GET /api/admin/schedules/options`, `GET /api/admin/schedules`, `POST /api/admin/schedules`, `PUT /api/admin/schedules/{id}`, `DELETE /api/admin/schedules/{id}`, `PUT /api/admin/schedules/prices`.

## 3. API mẫu

Các API `/api/phim` và `/api/khach-hang` chỉ dành cho STAFF/ADMIN; API tài khoản khách cần CUSTOMER. Các ví dụ `curl` bên dưới giả định bạn đã đăng nhập và gửi kèm session cookie; với POST/PUT/DELETE phải kèm CSRF token. Nên thử CRUD qua giao diện sau khi đăng nhập.

### Đặt vé

- `GET /api/dat-ve/phim`
- `GET /api/dat-ve/suat-chieu?maPhim=P001`
- `GET /api/dat-ve/suat-chieu/SC001/ghe`
- `GET /api/dat-ve/do-an`
- `POST /api/dat-ve` (cần session CUSTOMER)

Yêu cầu tạo đơn gồm mã suất chiếu, danh sách ghế, các món/số lượng và phương thức thanh toán; danh tính khách lấy từ session, không tin thông tin do trình duyệt tự gửi. Hóa đơn, vé và đồ ăn được lưu trong cùng một giao dịch; ghế đã có vé sẽ bị từ chối.

### Phim

```bash
curl http://localhost:8080/api/phim
curl "http://localhost:8080/api/phim?q=Avengers"
```

Thêm:

```bash
curl -X POST http://localhost:8080/api/phim \
  -H "Content-Type: application/json" \
  -d '{
    "maPhim":"P100",
    "tenPhim":"Demo Movie",
    "thoiLuong":120,
    "ngayKhoiChieu":"2026-08-24",
    "doanhThuUsd":0
  }'
```

Sửa:

```bash
curl -X PUT http://localhost:8080/api/phim/P100 \
  -H "Content-Type: application/json" \
  -d '{
    "maPhim":"P100",
    "tenPhim":"Demo Movie Updated",
    "thoiLuong":125,
    "ngayKhoiChieu":"2026-08-24",
    "doanhThuUsd":100000
  }'
```

Xóa:

```bash
curl -X DELETE http://localhost:8080/api/phim/P100
```

### Khách hàng

```bash
curl http://localhost:8080/api/khach-hang
curl "http://localhost:8080/api/khach-hang?q=Trần"
```

POST / PUT / DELETE tương tự như API phim.

## 4. Nội dung dữ liệu mẫu

Dữ liệu mẫu cố ý bao phủ các điều kiện của 13 truy vấn trong báo cáo:

- Có `Lê Văn Tài` là quản lý.
- Có nhân viên họ `Trần` có doanh số tháng 11 >= 200.000.
- Có khách hàng đủ tuổi và tổng hóa đơn tháng 11 > 200.000.
- Có khách hàng mua vé ở cả tháng 11 và 12/2024.
- Có khách hàng xem `Avengers: Endgame` nhưng không xem `Us`.
- Có phim thể loại `Hành động` trong 30/11 - 01/12.
- Có cặp `Anthony Russo` và `Joe Russo` làm chung ít nhất 2 phim.
- Có suất chiếu trước 12h để Query 13 có dữ liệu.

## 5. Các điểm đã sửa so với PDF

1. Query 6: sửa alias `oaiCa` -> `LoaiCa`.
2. Query 2: thêm `LIMIT 1` để lấy suất có doanh số lớn nhất.
3. Query 3: kiểm tra vé theo đúng `MaSuatChieu`, tránh coi ghế đã bán ở suất khác là ghế đã bán của suất hiện tại.
4. Query 7: tính mỗi hóa đơn một lần, tránh nhân doanh số do một hóa đơn có nhiều vé.
5. Query 8: dùng mốc tuổi cố định `2024-11-30` thay vì `CURDATE()` để kết quả ổn định khi chấm bài.
6. Query 9: giới hạn rõ thời gian năm 2024.
7. Query 12: sửa alias `ThanhToanChuyenKhoan` và điều kiện `ThanhToanChuyenKhoan > ThanhToanTienMat`.
8. Chuẩn hóa tên bảng `KhachHang` và thêm dấu phẩy còn thiếu ở CHECK của `NhanVien`.

## 6. Gợi ý cấu trúc GitHub

```text
PTIT-Cinema-Group30/
├── database/
│   └── database.sql
├── src/
│   └── main/
│       ├── java/com/ptit/cinema/
│       └── resources/application.properties
├── pom.xml
└── README.md
```
