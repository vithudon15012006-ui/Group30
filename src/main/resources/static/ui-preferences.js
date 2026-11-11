(() => {
  const pairs = [
    ['Hệ thống quản lý rạp chiếu phim', 'Cinema management system'],
    ['Trải nghiệm điện ảnh của bạn', 'Your movie experience'],
    ['Mỗi bộ phim, một trải nghiệm đáng nhớ.', 'Every movie, an experience to remember.'],
    ['Tạo tài khoản để đặt chỗ yêu thích, quản lý vé và xem lại các giao dịch của bạn trong một nơi.', 'Create an account to reserve your favorite seats, manage tickets, and review your transactions in one place.'],
    ['Chọn ghế và đồ ăn trước khi đến rạp.', 'Choose your seats and snacks before arriving.'],
    ['Lịch sử hóa đơn và thông tin vé luôn sẵn sàng.', 'Your booking history and ticket details, always available.'],
    ['Đăng ký nhanh bằng số điện thoại.', 'Sign up quickly with your phone number.'],
    ['PTIT Cinema · Tài khoản được bảo vệ bằng mã hóa mật khẩu', 'PTIT Cinema · Your account is protected with password hashing'],
    ['Khám phá · Đặt vé · Trải nghiệm', 'Discover · Book · Enjoy'],
    ['Rạp phim trong tầm tay', 'Cinema within reach'],
    ['Đăng nhập hoặc đăng ký', 'Sign in or create an account'],
    ['Tạo tài khoản khách hàng', 'Create a customer account'],
    ['Đăng ký bằng số điện thoại và mật khẩu, không cần email hay mã xác minh.', 'Sign up with a phone number and password; no email or verification code is needed.'],
    ['Chào mừng trở lại', 'Welcome back'],
    ['Khách hàng đăng nhập bằng số điện thoại; nhân viên dùng email được cấp.', 'Customers sign in with a phone number; staff use their assigned email.'],
    ['Số điện thoại hoặc email nhân viên', 'Phone number or staff email'],
    ['Số điện thoại đăng nhập', 'Sign-in phone number'],
    ['Số điện thoại là tên đăng nhập của tài khoản khách hàng.', 'Your phone number is the username for your customer account.'],
    ['Tạo tài khoản thành công. Bạn có thể đăng nhập bằng số điện thoại và mật khẩu.', 'Account created. You can now sign in with your phone number and password.'],
    ['Số điện thoại này đã có tài khoản. Hãy đăng nhập bằng số điện thoại đó.', 'This phone number already has an account. Sign in with that number.'],
    ['Số điện thoại này đã được liên kết với tài khoản khác.', 'This phone number is already linked to another account.'],
    ['Số điện thoại phải có từ 7 đến 15 chữ số.', 'Phone numbers must contain 7 to 15 digits.'],
    ['Không cần xác minh email', 'No email verification needed'],
    ['Tài khoản khách hàng đăng ký và đăng nhập bằng số điện thoại cùng mật khẩu.', 'Customer accounts sign up and sign in with a phone number and password.'],
    ['Đăng nhập an toàn', 'Sign in securely'],
    ['Tạo tài khoản', 'Create account'],
    ['Đăng nhập', 'Sign in'],
    ['Họ và tên', 'Full name'],
    ['Số điện thoại', 'Phone number'],
    ['Mật khẩu', 'Password'],
    ['Email', 'Email'],
    ['Lịch chiếu', 'Showtimes'],
    ['Xem lịch chiếu', 'View showtimes'],
    ['Không chia sẻ mật khẩu. Nhân viên và quản trị viên được cấp quyền riêng, không đăng ký qua biểu mẫu khách hàng.', 'Never share your password. Staff and administrators receive separate accounts and cannot register through the customer form.'],
    ['Vui lòng nhập số điện thoại/email và mật khẩu.', 'Enter your phone number/staff email and password.'],
    ['Số điện thoại/email hoặc mật khẩu không chính xác.', 'The phone number/staff email or password is incorrect.'],
    ['Tài khoản nhân viên chưa được kích hoạt. Vui lòng liên hệ quản trị viên.', 'This staff account is not activated. Contact an administrator.'],
    ['Tài khoản', 'Account'],
    ['Tài khoản của tôi', 'My account'],
    ['Không gian khách hàng', 'Customer account'],
    ['Tài khoản của bạn', 'Your account'],
    ['Xin chào, ', 'Hello, '],
    ['Xin chào, bạn', 'Hello there'],
    ['Quản lý thông tin, bảo mật và các giao dịch đã thực hiện.', 'Manage your profile, security, and past transactions.'],
    ['Thông tin cá nhân', 'Personal information'],
    ['Lưu thông tin', 'Save changes'],
    ['Đổi mật khẩu', 'Change password'],
    ['Dùng mật khẩu dài, riêng biệt cho tài khoản này.', 'Use a long, unique password for this account.'],
    ['Mật khẩu hiện tại', 'Current password'],
    ['Mật khẩu mới', 'New password'],
    ['Cập nhật mật khẩu', 'Update password'],
    ['Lịch sử đặt vé và giao dịch', 'Bookings and transactions'],
    ['Chỉ các hóa đơn thuộc tài khoản của bạn mới hiển thị ở đây.', 'Only invoices belonging to your account are shown here.'],
    ['Đang tải giao dịch…', 'Loading transactions…'],
    ['Đăng xuất', 'Sign out'],
    ['Đặt vé', 'Book tickets'],
    ['Đặt vé · Nhóm 30', 'Booking · Group 30'],
    ['Đặt vé xem phim', 'Book movie tickets'],
    ['Chọn phim, suất chiếu, ghế ngồi và món ăn yêu thích. Kiểm tra thông tin trước khi xác nhận đơn.', 'Choose a movie, showtime, seats, and snacks. Review your order before confirming.'],
    ['Chuyển khoản QR cần admin đối soát thủ công; hệ thống không tự nhận thông báo từ ngân hàng. Ngày chiếu được lấy trực tiếp từ dữ liệu trong cơ sở dữ liệu.', 'QR transfers require manual admin reconciliation; the system does not receive automatic bank notifications. Show dates come directly from the database.'],
    ['Chọn phim và suất chiếu', 'Choose a movie and showtime'],
    ['Chọn một suất để xem sơ đồ ghế', 'Choose a showtime to view the seat map'],
    ['Chọn ghế', 'Choose seats'],
    ['Ghế đã đặt sẽ không thể chọn', 'Booked seats cannot be selected'],
    ['MÀN HÌNH', 'SCREEN'],
    ['Còn trống', 'Available'],
    ['Đang chọn', 'Selected'],
    ['Đã đặt', 'Booked'],
    ['Ghế VIP có viền nét đứt', 'VIP seats have a dashed border'],
    ['Chưa chọn ghế', 'No seats selected'],
    ['Đồ ăn và thức uống', 'Food and drinks'],
    ['Không bắt buộc · Có thể bỏ qua', 'Optional · You can skip this'],
    ['Thông tin và thanh toán', 'Details and payment'],
    ['Đang kiểm tra tài khoản…', 'Checking account…'],
    ['Phương thức thanh toán', 'Payment method'],
    ['Chuyển khoản', 'Bank transfer'],
    ['Tiền mặt tại quầy', 'Cash at the counter'],
    ['Khi xác nhận, hệ thống sẽ tạo hóa đơn và vé trong cơ sở dữ liệu. Đây chưa phải giao dịch tiền thật.', 'Confirmation creates an invoice and tickets in the database. This is not a real payment.'],
    ['Tổng thanh toán', 'Total'],
    ['Xác nhận đặt vé', 'Confirm booking'],
    ['PTIT Cinema · Hệ thống đặt vé minh họa', 'PTIT Cinema · Demonstration booking system'],
    ['Quản lý dữ liệu', 'Data management'],
    ['Không gian dữ liệu', 'Data workspace'],
    ['Tra cứu và quản lý danh sách phim, khách hàng trong cơ sở dữ liệu PTIT Cinema.', 'Search and manage movies and customers in the PTIT Cinema database.'],
    ['Danh sách phim', 'Movies'],
    ['Thêm, cập nhật và tìm kiếm phim', 'Add, update, and search movies'],
    ['Tìm phim', 'Search movies'],
    ['Tìm phim theo mã hoặc tên', 'Search movies by ID or title'],
    ['Tìm theo mã hoặc tên phim', 'Search by movie ID or title'],
    ['Tìm kiếm', 'Search'],
    ['Mã phim', 'Movie ID'],
    ['Tên phim', 'Movie title'],
    ['Thời lượng (phút)', 'Duration (minutes)'],
    ['Ngày khởi chiếu', 'Release date'],
    ['Doanh thu (USD)', 'Revenue (USD)'],
    ['Ví dụ: P007', 'Example: P007'],
    ['Nhập tên phim', 'Enter a movie title'],
    ['Thêm phim', 'Add movie'],
    ['Làm mới form', 'Reset form'],
    ['Đang chỉnh sửa phim', 'Editing movie'],
    ['Khách hàng', 'Customers'],
    ['Thêm, cập nhật và tìm kiếm khách hàng', 'Add, update, and search customers'],
    ['Tìm khách hàng', 'Search customers'],
    ['Tìm khách hàng theo mã, tên, số điện thoại hoặc email', 'Search customers by ID, name, phone, or email'],
    ['Tìm mã, tên, SĐT hoặc email', 'Search ID, name, phone, or email'],
    ['Mã khách hàng', 'Customer ID'],
    ['Ngày sinh', 'Date of birth'],
    ['Thêm khách hàng', 'Add customer'],
    ['Đang chỉnh sửa khách hàng', 'Editing customer'],
    ['Mã KH', 'Customer ID'],
    ['Thao tác', 'Actions'],
    ['Sửa', 'Edit'],
    ['Xóa', 'Delete'],
    ['Đang tải danh sách phim…', 'Loading movies…'],
    ['Đang tải danh sách khách hàng…', 'Loading customers…'],
    ['Chưa có phim phù hợp. Hãy thử từ khóa khác.', 'No matching movies. Try another search.'],
    ['Chưa có khách hàng phù hợp. Hãy thử từ khóa khác.', 'No matching customers. Try another search.'],
    ['Hệ thống quản lý rạp chiếu phim', 'Cinema management system'],
    ['PTIT Cinema · Bài tập lớn Cơ sở dữ liệu · Nhóm 30', 'PTIT Cinema · Database project · Group 30'],
    ['Bảo mật và vận hành', 'Security and operations'],
    ['Khu vực quản trị', 'Admin console'],
    ['Quản lý tài khoản', 'Account management'],
    ['Mở khóa theo yêu cầu đã xác minh danh tính, hoặc mời nhân viên kích hoạt tài khoản.', 'Unlock accounts after verifying the requester, or invite staff to activate their accounts.'],
    ['Tài khoản đang bị khóa', 'Locked accounts'],
    ['Sau 5 lần đăng nhập sai liên tiếp; chỉ admin mới có thể mở khóa.', 'Locked after 5 consecutive failed sign-in attempts; only an admin can unlock it.'],
    ['Làm mới', 'Refresh'],
    ['Tra cứu theo email, tên hoặc vai trò', 'Search by email, name, or role'],
    ['Tra cứu theo số điện thoại, email nhân viên, tên hoặc vai trò', 'Search by phone number, staff email, name, or role'],
    ['Tra cứu tài khoản bị khóa', 'Search locked accounts'],
    ['Tài khoản', 'Account'],
    ['Vai trò', 'Role'],
    ['Mở khóa', 'Unlock'],
    ['Đang tải…', 'Loading…'],
    ['Không tìm thấy tài khoản đang khóa.', 'No locked accounts found.'],
    ['Mời nhân viên', 'Invite staff'],
    ['Nhân viên tự đặt mật khẩu từ liên kết email một lần.', 'Staff set their own password using a one-time email link.'],
    ['Nhân viên', 'Staff member'],
    ['Email công việc', 'Work email'],
    ['Gửi lời mời', 'Send invitation'],
    ['Liên kết đặt mật khẩu hết hạn sau 48 giờ. Không gửi mật khẩu tạm thời qua chat.', 'The password setup link expires after 48 hours. Do not send temporary passwords over chat.'],
    ['Nguyên tắc mở khóa', 'Unlocking guidelines'],
    ['Đảm bảo đúng người trước khi khôi phục quyền truy cập.', 'Verify the person before restoring access.'],
    ['Liên hệ khách qua kênh xác minh có sẵn của rạp trước khi mở khóa. Thao tác được ghi vào nhật ký quản trị. Việc mở khóa đặt lại bộ đếm đăng nhập sai; nếu tiếp tục sai 5 lần, tài khoản sẽ bị khóa lại.', 'Verify the customer through an available cinema channel before unlocking. The action is recorded in the admin audit log. Unlocking resets the failed sign-in counter; another 5 failures will lock the account again.'],
    ['Nhập số điện thoại', 'Enter a phone number'],
    ['Nhập họ và tên', 'Enter your full name'],
    ['Đăng nhập thành công.', 'Signed in successfully.'],
    ['Đã cập nhật thông tin tài khoản.', 'Account details updated.'],
    ['Đã cập nhật mật khẩu.', 'Password updated.'],
    ['Đã mở khóa tài khoản. Người dùng có thể đăng nhập lại.', 'Account unlocked. The user can sign in again.'],
    ['Xác nhận đã kiểm tra danh tính chủ tài khoản và mở khóa?', 'Confirm that the account owner has been verified and unlock the account?'],
    ['Mật khẩu cần ít nhất 12 ký tự và tối đa 72 byte UTF-8.', 'Password must be at least 12 characters and no more than 72 UTF-8 bytes.'],
    ['Vui lòng nhập số điện thoại/email và mật khẩu.', 'Enter your phone number/staff email and password.'],
    ['Số điện thoại/email hoặc mật khẩu không chính xác.', 'The phone number/staff email or password is incorrect.'],
    ['Tài khoản đã bị khóa sau 5 lần đăng nhập sai. Vui lòng liên hệ admin để mở khóa.', 'Your account was locked after 5 failed sign-in attempts. Contact an administrator to unlock it.'],
    ['Tài khoản đã bị khóa. Vui lòng liên hệ admin để mở khóa.', 'Your account is locked. Contact an administrator to unlock it.'],
    ['Máy chủ gặp lỗi. Hãy kiểm tra log Spring Boot; nếu chưa chuyển sang đăng nhập bằng số điện thoại, cần import migration V2.', 'A server error occurred. Check the Spring Boot logs; if phone sign-in has not been enabled, import migration V2.'],
    ['Không truy cập được cơ sở dữ liệu. Hãy bật MySQL, kiểm tra kết nối và xem log Spring Boot.', 'Cannot access the database. Start MySQL, check the connection, and review the Spring Boot logs.'],
    ['Liên kết không hợp lệ hoặc đã hết hạn.', 'This link is invalid or has expired.'],
    ['Kích hoạt tài khoản nhân viên', 'Activate staff account'],
    ['Đặt mật khẩu tối thiểu 12 ký tự để hoàn tất kích hoạt và xác minh email.', 'Set a password of at least 12 characters to activate your account and verify your email.'],
    ['Mật khẩu mới', 'New password'],
    ['Nhập lại mật khẩu', 'Confirm password'],
    ['Ít nhất 12 ký tự', 'At least 12 characters'],
    ['Kích hoạt tài khoản', 'Activate account'],
    ['Hai mật khẩu chưa khớp.', 'The passwords do not match.'],
    ['Đã kích hoạt tài khoản nhân viên. Bạn có thể đăng nhập.', 'Staff account activated. You can now sign in.'],
    ['Liên kết không thể sử dụng.', 'This link cannot be used.'],
    ['PTIT Cinema · Đặt vé · Nhóm 30', 'PTIT Cinema · Booking · Group 30'],
    ['Kết quả', 'Results'],
    ['Không tìm thấy phim này. Danh sách có thể đã được cập nhật.', 'Movie not found. The list may have changed.'],
    ['Không tìm thấy khách hàng này. Danh sách có thể đã được cập nhật.', 'Customer not found. The list may have changed.'],
    ['Chưa có phim được xếp suất chiếu.', 'No movies have showtimes yet.'],
    ['Phim này hiện chưa có suất chiếu.', 'This movie has no scheduled showtimes.'],
    ['Vui lòng chọn suất chiếu trước.', 'Please choose a showtime first.'],
    ['Vui lòng chọn ít nhất một ghế.', 'Please select at least one seat.'],
    ['Vui lòng chọn suất chiếu.', 'Please choose a showtime.'],
    ['Vui lòng chọn từ 1 đến 10 ghế.', 'Please select between 1 and 10 seats.'],
    ['Mỗi đơn tối đa 10 ghế.', 'A booking can include up to 10 seats.'],
    ['Đã chọn ', 'Selected '],
    [' ghế đã đặt', ' seats booked'],
    [' ghế', ' seats'],
    ['Đặt vé thành công · Mã hóa đơn ', 'Booking confirmed · Invoice '],
    ['Đang tải phim…', 'Loading movies…'],
    ['Chọn phim', 'Choose a movie'],
    [' · ', ' · '],
    ['phút', 'minutes'],
    ['Ghế ', 'Seat '],
    [' · Ghế ', ' · Seat '],
    ['Họ và tên', 'Full name'],
    ['Phim này hiện chưa có suất chiếu.', 'This movie has no scheduled showtimes.'],
    ['Đang ghi nhận đơn…', 'Submitting booking…'],
    ['Đặt vé thành công', 'Booking confirmed'],
    ['Ghế:', 'Seats:'],
    ['Khách hàng:', 'Customer:'],
    ['Tiền vé:', 'Tickets:'],
    ['Đồ ăn:', 'Food:'],
    ['Tổng cộng:', 'Total:'],
    ['mô phỏng', 'simulated'],
    ['Thanh toán:', 'Payment:'],
    ['Tiền mặt', 'Cash'],
    ['Đang tải thực đơn…', 'Loading menu…'],
    ['Chưa có sản phẩm trong thực đơn.', 'No products are available.'],
    ['Chưa có giao dịch nào. ', 'No transactions yet. '],
    ['Quản lý phim và khách hàng', 'Movie and customer management'],
    ['Người dùng', 'User'],
    ['Admin', 'Admin'],
    ['Đăng xuất thành công.', 'Signed out successfully.'],
    ['Đang tải danh sách…', 'Loading list…'],
    ['Không tải được danh sách phim:', 'Could not load movies:'],
    ['Không tải được danh sách khách hàng:', 'Could not load customers:'],
    ['Đã cập nhật thông tin phim.', 'Movie updated.'],
    ['Đã thêm phim mới.', 'Movie added.'],
    ['Đã cập nhật thông tin khách hàng.', 'Customer updated.'],
    ['Đã thêm khách hàng mới.', 'Customer added.'],
    ['Đã xóa ', 'Deleted '],
    ['phim "', 'movie "'],
    ['khách hàng "', 'customer "'],
    ['Bạn có chắc muốn xóa ', 'Are you sure you want to delete '],
    [' không?', '?'],
    ['Tài khoản khách hàng', 'Customer account'],
    ['Tài khoản nhân viên', 'Staff account'],
    ['Không thể hoàn tất yêu cầu. Vui lòng thử lại.', 'Could not complete the request. Please try again.'],
    ['Yêu cầu chưa thể hoàn tất.', 'Could not complete the request.'],
    ['Yêu cầu thất bại', 'Request failed'],
    ['Vui lòng đăng nhập để tiếp tục.', 'Please sign in to continue.'],
    ['Bạn không có quyền thực hiện thao tác này.', 'You are not authorized to perform this action.'],
    ['Thanh toán mô phỏng', 'Simulated payment'],
    ['Xem danh sách phim', 'View movie list'],
    ['Tìm theo mã hoặc tên', 'Search by ID or name'],
    ['Mã nhân viên', 'Staff ID'],
    ['Sửa thông tin', 'Edit details'],
    ['Quản trị thanh toán', 'Payment administration'],
    ['Lịch chiếu & giá vé', 'Showtimes & ticket prices'],
    ['Lịch chiếu và giá vé', 'Showtimes and ticket prices'],
    ['Vận hành rạp', 'Cinema operations'],
    ['Tạo suất chiếu trong các phòng hiện có và cập nhật giá áp dụng cho những đơn đặt mới. Giá trên vé đã phát hành được giữ nguyên.', 'Create showtimes in existing rooms and update prices for new orders. Prices on issued tickets remain unchanged.'],
    ['Giá vé', 'Ticket prices'],
    ['Áp dụng theo loại ghế cho các giao dịch đặt sau khi lưu.', 'Applies by seat type to bookings created after saving.'],
    ['Ghế thường (đồng)', 'Standard seat (VND)'],
    ['Ghế VIP (đồng)', 'VIP seat (VND)'],
    ['Lưu giá vé', 'Save ticket prices'],
    ['Giá hợp lệ từ 1.000 đến 10.000.000 đồng. Mỗi hóa đơn đã tạo giữ nguyên giá tại thời điểm đặt.', 'Prices must be 1,000–10,000,000 VND. Existing invoices keep the price set when booked.'],
    ['Phòng chiếu hiện có', 'Existing screening rooms'],
    ['Chọn phòng khi lập suất. Thêm phòng và sơ đồ ghế chưa hỗ trợ trên trang này.', 'Select a room for each showtime. Adding rooms or configuring seat maps is not supported here.'],
    ['Tạo suất chiếu', 'Create showtime'],
    ['Suất mới phải ở tương lai, không trùng giờ trong cùng phòng.', 'New showtimes must be in the future and not overlap in the same room.'],
    ['Thêm hoặc sửa thông tin phim', 'Add or edit movie details'],
    ['Phim', 'Movie'],
    ['Phòng chiếu', 'Screening room'],
    ['Quản lý suất chiếu', 'Showtime manager'],
    ['Ngày chiếu', 'Show date'],
    ['Giờ bắt đầu', 'Start time'],
    ['Giờ kết thúc', 'End time'],
    ['Lưu thay đổi', 'Save changes'],
    ['Hủy sửa', 'Cancel editing'],
    ['Không thể sửa hoặc xóa suất đã có vé, lịch sử đơn hàng hoặc ghế đang được giữ.', 'Showtimes with tickets, order history, or held seats cannot be edited or deleted.'],
    ['Muốn tạo nhiều suất, hãy thêm từng suất với giờ và phòng phù hợp.', 'Add each showtime separately, choosing an available room and time.'],
    ['Danh sách suất chiếu', 'Showtime list'],
    ['Tối đa 500 suất, lịch sắp tới được xếp trước.', 'Up to 500 showtimes; upcoming ones are listed first.'],
    ['Ngày / giờ', 'Date / time'],
    ['Quản lý', 'Manager'],
    ['Vé / đang giữ', 'Tickets / held'],
    ['Đang tải…', 'Loading…'],
    ['Chọn phim', 'Select a movie'],
    ['Chọn phòng', 'Select a room'],
    ['Chọn quản lý', 'Select a manager'],
    ['Chưa có phòng chiếu.', 'No screening rooms found.'],
    [' vé', ' tickets'],
    [' giữ', ' held'],
    ['Chưa có suất chiếu nào.', 'No showtimes found.'],
    ['Đã cập nhật giá vé. Đơn đã tạo không bị thay đổi.', 'Ticket prices updated. Existing orders are unchanged.'],
    ['Đã cập nhật suất chiếu.', 'Showtime updated.'],
    ['Đã tạo suất chiếu.', 'Showtime created.'],
    ['Đã xóa suất chiếu.', 'Showtime deleted.'],
    ['Xóa suất chiếu ', 'Delete showtime '],
    ['? Chỉ xóa được suất chưa có vé, đơn hoặc ghế đang giữ.', '? Only showtimes without tickets, orders, or held seats can be deleted.'],
    ['Không tìm thấy suất chiếu.', 'Showtime not found.'],
    ['Suất chiếu mới phải bắt đầu trong tương lai.', 'New showtimes must start in the future.'],
    ['Giờ kết thúc phải sau giờ bắt đầu trong cùng ngày.', 'The end time must be after the start time on the same day.'],
    ['Phòng chiếu đã có suất giao nhau trong khoảng thời gian này.', 'This room already has an overlapping showtime.'],
    ['Vui lòng nhập phim, ngày chiếu, giờ, phòng và quản lý suất chiếu.', 'Enter the movie, date, times, room, and showtime manager.'],
    ['Thanh toán & giao dịch', 'Payments & transactions'],
    ['Doanh thu và đối soát', 'Revenue and reconciliation'],
    ['Đối chiếu tiền đã thực nhận trong ngân hàng trước khi xác nhận đơn chuyển khoản.', 'Verify the transfer is received in the bank account before confirming the order.'],
    ['Tổng doanh thu đã xác nhận', 'Total confirmed revenue'],
    ['Doanh thu hôm nay', "Today's revenue"],
    ['Giao dịch đã thanh toán', 'Paid transactions'],
    ['Đơn chờ xác nhận', 'Orders awaiting confirmation'],
    ['Đơn hết hạn', 'Expired orders'],
    ['Lịch sử giao dịch', 'Transaction history'],
    ['Chỉ xác nhận khi đã thấy đúng số tiền và mã đơn trong tài khoản ngân hàng.', 'Confirm only after verifying the exact amount and order reference in the bank account.'],
    ['Tất cả trạng thái', 'All statuses'],
    ['Chờ thanh toán', 'Pending payment'],
    ['Đã thanh toán', 'Paid'],
    ['Hết hạn', 'Expired'],
    ['Đã hủy', 'Cancelled'],
    ['Làm mới', 'Refresh'],
    ['Đang tải giao dịch…', 'Loading transactions…'],
    ['Không có giao dịch phù hợp.', 'No matching transactions.'],
    ['Khách:', 'Customer:'],
    ['Tạo lúc:', 'Created:'],
    ['Thanh toán:', 'Paid:'],
    ['Hình thức:', 'Method:'],
    ['Nội dung CK:', 'Transfer reference:'],
    ['Hạn thanh toán:', 'Payment deadline:'],
    ['Xác nhận bởi:', 'Confirmed by:'],
    ['Không có chi tiết ghế.', 'No seat details.'],
    ['Đã nhận tiền · Xác nhận', 'Confirm received payment'],
    ['Hủy đơn', 'Cancel order'],
    ['Chỉ xác nhận nếu bạn đã thấy giao dịch chuyển khoản thành công trong tài khoản ngân hàng. Tiếp tục?', 'Only confirm if the successful transfer is visible in the bank account. Continue?'],
    ['Hủy đơn này và giải phóng ghế đang giữ?', 'Cancel this order and release its held seats?'],
    ['Đã xác nhận tiền vào tài khoản và hoàn tất đơn hàng.', 'Payment confirmed after funds were received.'],
    ['Đã hủy đơn chờ thanh toán và giải phóng ghế.', 'Pending order cancelled and held seats released.'],
    ['Đã hủy đơn.', 'Order cancelled.'],
    ['Chuyển khoản tạo đơn chờ admin xác nhận sau khi tiền đã vào tài khoản. Vé chỉ được xác nhận sau đối soát.', 'Bank transfers stay pending until an administrator confirms funds arrived. Tickets are issued only after reconciliation.'],
    ['Quét QR chuyển khoản', 'Scan transfer QR'],
    ['QR chuyển khoản hiện không khả dụng; vui lòng chọn tiền mặt tại quầy.', 'Bank transfer QR is currently unavailable; please choose cash at the counter.'],
    ['Quét QR tĩnh ', 'Scan the static QR for '],
    [', rồi nhập đúng số tiền và nội dung chuyển khoản của đơn. Admin xác nhận khi đối chiếu được tiền vào; ghế được giữ tối đa ', ', then enter the exact order amount and transfer reference. An administrator will confirm after reconciling the funds; seats are held for up to '],
    [' phút.', ' minutes.'],
    ['Đơn đang chờ thanh toán', 'Order awaiting payment'],
    ['Ngân hàng:', 'Bank:'],
    ['Số tiền:', 'Amount:'],
    ['Số tiền cần chuyển:', 'Amount to transfer:'],
    ['Nội dung chuyển khoản:', 'Transfer reference:'],
    ['Thời hạn:', 'Expires in:'],
    ['Ghế được giữ tạm thời. Chỉ xem là đã thanh toán khi admin đối chiếu được tiền vào ngân hàng.', 'Seats are held temporarily. The order is paid only after an administrator confirms funds arrived.'],
    ['Mã QR tĩnh Agribank', 'Static Agribank QR code'],
    ['Quét QR tĩnh, sau đó tự nhập chính xác số tiền và nội dung chuyển khoản ở trên. Admin chỉ xác nhận sau khi đối chiếu được tiền vào ngân hàng.', 'Scan the static QR, then manually enter the exact amount and transfer reference shown above. An administrator confirms only after reconciling the incoming payment.'],
    ['Quét QR tĩnh Agribank và nhập chính xác số tiền, nội dung trên trước ', 'Scan the static Agribank QR and enter the exact amount and transfer reference above before '],
    ['. Admin sẽ xác nhận sau khi đối soát.', '. An administrator will confirm after reconciling the transfer.'],
    ['Đã hết hạn; ghế sẽ được giải phóng.', 'Expired; seats will be released.'],
    ['Chỉ xác nhận được đơn chuyển khoản đang chờ thanh toán.', 'Only pending bank transfer orders can be confirmed.'],
    ['Đơn đã hết hạn; ghế đang được giải phóng và không thể xác nhận thanh toán.', 'The order expired; seats are being released and payment cannot be confirmed.'],
    ['Một hoặc nhiều ghế đã được bán sau khi hết hạn đơn. Không xác nhận được; cần hoàn tiền thủ công.', 'One or more seats were sold after the order expired. Confirmation failed; a manual refund is required.'],
    ['Chỉ hủy được đơn đang chờ thanh toán.', 'Only pending orders can be cancelled.'],
    ['Hãy nhập email vào ô đăng nhập trước.', 'Enter your email in the sign-in field first.']
  ];

  const toEnglish = new Map(pairs);
  const toVietnamese = new Map(pairs.map(([vietnamese, english]) => [english, vietnamese]));
  const translationPatterns = new Map([
    ['en', new RegExp([...toEnglish.keys()]
      .sort((left, right) => right.length - left.length)
      .map(source => source.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
      .join('|'), 'g')],
    ['vi', new RegExp([...toVietnamese.keys()]
      .sort((left, right) => right.length - left.length)
      .map(source => source.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
      .join('|'), 'g')]
  ]);
  const caches = {
    theme: 'ptit-cinema-theme',
    language: 'ptit-cinema-language'
  };
  let language = localStorage.getItem(caches.language) || 'vi';
  let theme = localStorage.getItem(caches.theme) || 'dark';

  function translate(value, targetLanguage) {
    const replacements = targetLanguage === 'en' ? toEnglish : toVietnamese;
    return value.replace(translationPatterns.get(targetLanguage), source => replacements.get(source));
  }

  function addControls() {
    const header = document.querySelector('header');
    const controls = document.createElement('div');
    controls.className = 'ui-preferences';
    controls.innerHTML = `
      <label class="sr-only" for="ui-language">Language</label>
      <select id="ui-language" aria-label="Language">
        <option value="vi">Tiếng Việt</option>
        <option value="en">English</option>
      </select>
      <button id="ui-theme" type="button"></button>`;

    if (!header) {
      controls.classList.add('ui-preferences-floating');
      document.body.append(controls);
    } else {
      const nav = header.querySelector('nav');
      const last = header.lastElementChild;
      const target = nav || (last && last.tagName !== 'A' && !last.classList.contains('brand') ? last : header);
      target.append(controls);
    }

    const languageSelect = controls.querySelector('#ui-language');
    const themeButton = controls.querySelector('#ui-theme');
    languageSelect.value = language;
    languageSelect.addEventListener('change', () => {
      language = languageSelect.value;
      localStorage.setItem(caches.language, language);
      applyLanguage();
    });
    themeButton.addEventListener('click', () => {
      theme = theme === 'dark' ? 'light' : 'dark';
      localStorage.setItem(caches.theme, theme);
      applyTheme();
    });
    applyTheme();
  }

  function applyTheme() {
    document.documentElement.dataset.theme = theme;
    const button = document.querySelector('#ui-theme');
    if (button) {
      button.textContent = theme === 'dark' ? '☀ Light' : '☾ Dark';
      button.setAttribute('aria-label', theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
      button.dataset.i18nLight = language === 'vi' ? '☀ Sáng' : '☀ Light';
      button.dataset.i18nDark = language === 'vi' ? '☾ Tối' : '☾ Dark';
      button.textContent = theme === 'dark' ? button.dataset.i18nLight : button.dataset.i18nDark;
    }
  }

  function translateAttributes(root) {
    const elements = [];
    if (root instanceof Element) elements.push(root);
    if (root.querySelectorAll) elements.push(...root.querySelectorAll('*'));
    for (const element of elements) {
      for (const attribute of ['placeholder', 'aria-label', 'title', 'alt']) {
        const value = element.getAttribute(attribute);
        if (value) element.setAttribute(attribute, translate(value, language));
      }
    }
  }

  function translateNodes(root) {
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    let node;
    while ((node = walker.nextNode())) {
      if (!node.nodeValue.trim()) continue;
      node.nodeValue = translate(node.nodeValue, language);
    }
    translateAttributes(root);
  }

  function applyLanguage() {
    document.documentElement.lang = language;
    const languageSelect = document.querySelector('#ui-language');
    if (languageSelect) {
      languageSelect.value = language;
      languageSelect.setAttribute('aria-label', language === 'vi' ? 'Ngôn ngữ' : 'Language');
      languageSelect.querySelector('option[value="vi"]').textContent = 'Tiếng Việt';
      languageSelect.querySelector('option[value="en"]').textContent = 'English';
    }
    translateNodes(document.body);
    document.title = translate(document.title, language);
    applyTheme();
  }

  addControls();
  applyLanguage();
  const observer = new MutationObserver(records => {
    for (const record of records) {
      for (const node of record.addedNodes) {
        if (node.nodeType === Node.ELEMENT_NODE) translateNodes(node);
        else if (node.nodeType === Node.TEXT_NODE) node.nodeValue = translate(node.nodeValue, language);
      }
    }
  });
  observer.observe(document.body, { childList: true, subtree: true });
})();
