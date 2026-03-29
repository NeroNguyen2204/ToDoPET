ToDoPET - Gamified Task Manager
ToDoPET là một ứng dụng quản lý công việc (To-do List) kết hợp yếu tố nhập vai (RPG) và nuôi thú ảo, giúp việc quản lý thời gian trở nên thú vị và bớt nhàm chán hơn.
Tính năng nổi bật
1. Quản lý công việc thông minh
•
CRUD Task: Thêm, sửa, xóa và tìm kiếm công việc dễ dàng.
•
Phân loại ưu tiên: Gắn mức độ ưu tiên (High, Medium, Low) để quản lý hiệu quả.
•
Lọc trạng thái: Xem danh sách công việc theo trạng thái (Tất cả, Đã xong, Chưa xong).
•
Thông báo nhắc nhở: Tự động gửi thông báo khi đến hạn chót của công việc.
2. Hệ thống RPG & Thú ảo (Gamification)
•
Hệ thống XP & Level: Hoàn thành công việc để nhận kinh nghiệm (XP) và thăng cấp. Mức độ ưu tiên càng cao, XP nhận được càng nhiều.
•
Thú ảo (Pet):
◦
Thú cưng sẽ Vui vẻ khi bạn hoàn thành tốt công việc.
◦
Thú cưng sẽ Buồn bã nếu bạn để công việc bị trễ hạn.
3. Chế độ Tập trung (Focus Mode)
•
Đồng hồ đếm ngược: Thiết lập thời gian để tập trung hoàn toàn vào công việc.
•
Khóa ứng dụng (Screen Pinning): Ngăn chặn việc vuốt thoát ứng dụng để đảm bảo sự tập trung cao độ.
•
Thưởng & Phạt XP:
◦
Hoàn thành thời gian tập trung: Nhận 2 XP/phút.
◦
Thoát sớm giữa chừng: Bị trừ 1 XP/phút.
4. Chia sẻ công việc
•
Dễ dàng chia sẻ chi tiết công việc và hạn chót cho bạn bè qua Zalo hoặc Messenger chỉ với một chạm qua Bottom Sheet.
🛠 Công nghệ sử dụng
•
Ngôn ngữ: Java
•
Cơ sở dữ liệu: Room Persistence Library (Lưu trữ task cục bộ)
•
Lưu trữ RPG: SharedPreferences (Lưu Level và XP).
•
Thông báo: AlarmManager & NotificationCompat.
•
Màn hình chính: Hiển thị Pet và danh sách Task.
•
Chế độ Focus: Đồng hồ đếm ngược phong cách tối giản.
•
Form thêm Task: Lựa chọn ngày giờ hiện đại với Material Date/Time Picker.
Cài đặt
1.
Clone project về máy:
git clone https://github.com/your-username/ToDoPET.git
2.
Mở project bằng Android Studio.
3.
Đảm bảo đã cài đặt SDK version 34 trở lên.
4.
Nhấn Run để chạy ứng dụng trên Emulator hoặc thiết bị thật.
📌 Lưu ý
•
Để sử dụng tính năng Ghim màn hình trong chế độ Focus, ứng dụng sẽ yêu cầu quyền hệ thống trong lần đầu tiên sử dụng.
•
Tính năng thông báo cần quyền POST_NOTIFICATIONS trên Android 13+.