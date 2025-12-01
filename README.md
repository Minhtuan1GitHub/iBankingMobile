# Ứng dụng Ngân hàng Số (Mobile Banking App)

## Tổng quan

Trong thời đại Công nghệ 4.0, các ngân hàng và công ty tài chính phải cung cấp cho khách hàng những cơ chế thuận tiện để thực hiện mọi hoạt động tài chính. Một phương thức là ngân hàng trực tuyến qua ứng dụng web, phương thức khác là phát triển ứng dụng ngân hàng trên thiết bị di động. Với ứng dụng này, khách hàng có thể thực hiện các giao dịch mà không cần đến ngân hàng hay ATM.

Tại Việt Nam, các ngân hàng như Vietcombank, BIDV, OCB đều đã có ứng dụng di động. Khách hàng chỉ cần tải ứng dụng từ Google Play hoặc Apple Store, ngân hàng hỗ trợ thiết lập ban đầu và sau đó khách hàng tự thao tác.

Trong dự án này, nhóm sẽ phát triển một ứng dụng ngân hàng số nhằm tăng hiệu quả vận hành và cải thiện trải nghiệm khách hàng.

---

## Yêu cầu nghiệp vụ

### 1. Hồ sơ người dùng

* Hỗ trợ 2 loại người dùng: khách hàng và nhân viên ngân hàng.
* Mỗi loại người dùng có giao diện và quyền hạn khác nhau.
* Nhân viên ngân hàng có thể tạo và chỉnh sửa thông tin khách hàng.
* Khách hàng có thể xem và cập nhật thông tin cá nhân.

### 2. Yêu cầu bảo mật

* Hệ thống có màn hình đăng nhập.
* Hỗ trợ eKYC: xác thực sinh trắc học (ví dụ: quét khuôn mặt).
* Giao dịch có giá trị cao yêu cầu xác minh bằng ảnh sinh trắc.
* Tất cả giao dịch yêu cầu 2FA (OTP).

### 3. Quản lý tài khoản

Hỗ trợ 3 loại tài khoản:

* Checking (thanh toán)
* Saving (tiết kiệm)
* Mortgage (thế chấp)

Chức năng:

* Xem số dư tài khoản.
* Xem lãi suất, lợi nhuận theo tháng (đối với tiết kiệm).
* Nhân viên ngân hàng có thể thay đổi lãi suất.
* Xem lịch thanh toán định kỳ đối với tài khoản thế chấp.
* Xem lịch sử giao dịch.
* Nạp tiền / rút tiền.

### 4. Quản lý giao dịch

* Xác thực giao dịch theo quy định bảo mật.
* Hệ thống kiểm tra giao dịch hợp lệ trước khi lưu.
* Chuyển tiền nội bộ hoặc liên ngân hàng.
* Tích hợp cổng thanh toán như VNPay hoặc Stripe.

### 5. Tiện ích

* Thanh toán hóa đơn điện/nước.
* Nạp tiền điện thoại.
* Mua vé máy bay, vé xem phim.
* Đặt phòng khách sạn.
* Thanh toán thương mại điện tử.

### 6. Điều hướng & Bản đồ

* Xác định vị trí người dùng.
* Hiển thị các chi nhánh ngân hàng gần nhất.
* Gợi ý đường đi ngắn nhất.

---

## Yêu cầu kỹ thuật

### 1. Thành viên nhóm

* Dự án thực hiện bởi nhóm 2–3 người.

### 2. Cơ sở dữ liệu

* Có thể dùng SQLite.
* Ưu tiên dùng Firebase (Cloud Database).

### 3. Triển khai

* Ứng dụng được đưa lên Google Play Store.

### 4. Báo cáo dự án

Bao gồm:

* **Phân tích & thiết kế**: nguyên tắc thiết kế, design patterns, class diagram, ERD, use case, kiến trúc hệ thống.
* **Triển khai**: cấu trúc code, công nghệ sử dụng.
* **Giao diện người dùng**: mô tả đầy đủ chức năng.

### 5. Video demo

* Trình bày toàn bộ tính năng của ứng dụng.

---

## Ghi chú


