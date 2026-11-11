package com.ptit.cinema.service;

import com.ptit.cinema.model.AdminScheduleModels.ManagerOption;
import com.ptit.cinema.model.AdminScheduleModels.MovieOption;
import com.ptit.cinema.model.AdminScheduleModels.Options;
import com.ptit.cinema.model.AdminScheduleModels.PriceUpdate;
import com.ptit.cinema.model.AdminScheduleModels.RoomOption;
import com.ptit.cinema.model.AdminScheduleModels.ScheduleRequest;
import com.ptit.cinema.model.AdminScheduleModels.ScheduleView;
import com.ptit.cinema.model.AdminScheduleModels.TicketPrices;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AdminScheduleService {
    private static final int MIN_TICKET_PRICE = 1_000;
    private static final int MAX_TICKET_PRICE = 10_000_000;

    private final JdbcTemplate jdbc;

    public AdminScheduleService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Options options() {
        List<MovieOption> movies = jdbc.query("""
                SELECT MaPhim, TenPhim FROM Phim ORDER BY TenPhim
                """, (rs, rowNum) -> new MovieOption(rs.getString("MaPhim"), rs.getString("TenPhim")));
        List<RoomOption> rooms = jdbc.query("""
                SELECT PH.MaPhong, PH.TenPhong, PH.LoaiPhong, PH.SucChua,
                       COUNT(DISTINCT G.MaGhe) AS SoGhe,
                       COUNT(DISTINCT SC.MaSuatChieu) AS SoSuatChieu
                FROM Phong PH
                LEFT JOIN Ghe G ON G.MaPhong = PH.MaPhong
                LEFT JOIN SuatChieu SC ON SC.MaPhong = PH.MaPhong
                GROUP BY PH.MaPhong, PH.TenPhong, PH.LoaiPhong, PH.SucChua
                ORDER BY PH.MaPhong
                """, (rs, rowNum) -> new RoomOption(
                rs.getString("MaPhong"), rs.getString("TenPhong"), rs.getString("LoaiPhong"),
                (Integer) rs.getObject("SucChua"), rs.getInt("SoGhe"), rs.getLong("SoSuatChieu")
        ));
        List<ManagerOption> managers = jdbc.query("""
                SELECT QL.MaQuanLy, NV.HoTen
                FROM NhanVienQuanLy QL
                JOIN NhanVien NV ON NV.MaNhanVien = QL.MaQuanLy
                ORDER BY NV.HoTen
                """, (rs, rowNum) -> new ManagerOption(rs.getString("MaQuanLy"), rs.getString("HoTen")));
        return new Options(movies, rooms, managers, ticketPrices());
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> schedules() {
        return jdbc.query("""
                SELECT SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                       TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau,
                       TIME_FORMAT(SC.GioKetThuc, '%H:%i') AS GioKetThuc,
                       SC.MaPhong, PH.TenPhong, SC.MaQuanLy, NV.HoTen AS TenQuanLy,
                       PH.SucChua, COUNT(DISTINCT G.MaGhe) AS SoGhe,
                       COUNT(DISTINCT V.MaVe) AS SoVeDaBan,
                       COUNT(DISTINCT H.MaGhe) AS SoGheDangGiu,
                       COUNT(DISTINCT C.MaHoaDon) AS SoDonLienQuan
                FROM SuatChieu SC
                JOIN Phim P ON P.MaPhim = SC.MaPhim
                JOIN Phong PH ON PH.MaPhong = SC.MaPhong
                JOIN NhanVien NV ON NV.MaNhanVien = SC.MaQuanLy
                LEFT JOIN Ghe G ON G.MaPhong = SC.MaPhong
                LEFT JOIN Ve V ON V.MaSuatChieu = SC.MaSuatChieu
                              AND V.MaPhong = SC.MaPhong AND V.MaGhe = G.MaGhe
                LEFT JOIN GheGiuThanhToan H ON H.MaSuatChieu = SC.MaSuatChieu
                              AND H.MaPhong = SC.MaPhong AND H.MaGhe = G.MaGhe
                              AND H.HetHanLuc > CURRENT_TIMESTAMP
                LEFT JOIN ChiTietGheHoaDon C ON C.MaSuatChieu = SC.MaSuatChieu
                GROUP BY SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                         SC.GioBatDau, SC.GioKetThuc, SC.MaPhong, PH.TenPhong,
                         SC.MaQuanLy, NV.HoTen, PH.SucChua
                ORDER BY (SC.NgayChieu < CURRENT_DATE()) ASC, SC.NgayChieu, SC.GioBatDau
                LIMIT 500
                """, (rs, rowNum) -> new ScheduleView(
                rs.getString("MaSuatChieu"), rs.getString("MaPhim"), rs.getString("TenPhim"),
                rs.getDate("NgayChieu").toLocalDate(), rs.getString("GioBatDau"), rs.getString("GioKetThuc"),
                rs.getString("MaPhong"), rs.getString("TenPhong"), rs.getString("MaQuanLy"),
                rs.getString("TenQuanLy"), (Integer) rs.getObject("SucChua"), rs.getInt("SoGhe"),
                rs.getInt("SoVeDaBan"), rs.getInt("SoGheDangGiu"), rs.getLong("SoDonLienQuan")
        ));
    }

    @Transactional
    public TicketPrices updatePrices(PriceUpdate request, String adminId) {
        if (request == null) throw new IllegalArgumentException("Thiếu thông tin giá vé.");
        validatePrice(request.giaThuong(), "thường");
        validatePrice(request.giaVip(), "VIP");
        jdbc.update("""
                UPDATE CauHinhGiaVe
                SET Gia = CASE LoaiGhe WHEN 'THUONG' THEN ? WHEN 'VIP' THEN ? END,
                    CapNhatBoi = ?
                WHERE LoaiGhe IN ('THUONG', 'VIP')
                """, request.giaThuong(), request.giaVip(), adminId);
        ensurePriceRowsPresent();
        audit(adminId, "TICKET_PRICE_UPDATED");
        return ticketPrices();
    }

    @Transactional
    public ScheduleView create(ScheduleRequest request, String adminId) {
        ValidatedSchedule value = validate(request);
        lockRooms(List.of(value.roomId()));
        ensureReferences(value);
        ensureNoOverlap(value, null);
        String showtimeId = "SCM" + java.util.UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(java.util.Locale.ROOT);
        jdbc.update("""
                INSERT INTO SuatChieu
                    (MaSuatChieu, NgayChieu, GioBatDau, GioKetThuc, MaQuanLy, MaPhim, MaPhong)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, showtimeId, value.date(), value.start(), value.end(),
                value.managerId(), value.movieId(), value.roomId());
        audit(adminId, "SHOWTIME_CREATED");
        return schedule(showtimeId);
    }

    @Transactional
    public ScheduleView update(String showtimeId, ScheduleRequest request, String adminId) {
        ValidatedSchedule value = validate(request);
        String previousRoom = roomForUpdate(showtimeId);
        lockRooms(List.of(previousRoom, value.roomId()));
        String lockedRoom = roomForUpdate(showtimeId);
        if (!previousRoom.equals(lockedRoom)) {
            throw new IllegalStateException("Suất chiếu vừa được thay đổi; hãy tải lại danh sách rồi thử lại.");
        }
        ensureNoReservations(showtimeId);
        ensureReferences(value);
        ensureNoOverlap(value, showtimeId);
        jdbc.update("""
                UPDATE SuatChieu
                SET NgayChieu = ?, GioBatDau = ?, GioKetThuc = ?,
                    MaQuanLy = ?, MaPhim = ?, MaPhong = ?
                WHERE MaSuatChieu = ?
                """, value.date(), value.start(), value.end(), value.managerId(),
                value.movieId(), value.roomId(), showtimeId);
        audit(adminId, "SHOWTIME_UPDATED");
        return schedule(showtimeId);
    }

    @Transactional
    public void delete(String showtimeId, String adminId) {
        String roomId = roomForUpdate(showtimeId);
        lockRooms(List.of(roomId));
        if (!roomId.equals(roomForUpdate(showtimeId))) {
            throw new IllegalStateException("Suất chiếu vừa được thay đổi; hãy tải lại danh sách rồi thử lại.");
        }
        ensureNoReservations(showtimeId);
        jdbc.update("DELETE FROM SuatChieu WHERE MaSuatChieu = ?", showtimeId);
        audit(adminId, "SHOWTIME_DELETED");
    }

    private TicketPrices ticketPrices() {
        List<TicketPriceRow> rows = jdbc.query("""
                SELECT LoaiGhe, Gia, CapNhatLuc
                FROM CauHinhGiaVe
                ORDER BY LoaiGhe
                """, (rs, rowNum) -> new TicketPriceRow(
                rs.getString("LoaiGhe"), rs.getInt("Gia"),
                rs.getTimestamp("CapNhatLuc").toLocalDateTime()
        ));
        int standard = rows.stream().filter(row -> "THUONG".equals(row.seatType())).mapToInt(TicketPriceRow::price).findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu cấu hình giá ghế thường."));
        int vip = rows.stream().filter(row -> "VIP".equals(row.seatType())).mapToInt(TicketPriceRow::price).findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu cấu hình giá ghế VIP."));
        LocalDateTime updatedAt = rows.stream().map(TicketPriceRow::updatedAt).max(LocalDateTime::compareTo).orElseThrow();
        return new TicketPrices(standard, vip, updatedAt);
    }

    static void validatePrice(int amount, String label) {
        if (amount < MIN_TICKET_PRICE || amount > MAX_TICKET_PRICE) {
            throw new IllegalArgumentException("Giá ghế " + label + " phải từ 1.000 đến 10.000.000 đồng.");
        }
    }

    private void ensurePriceRowsPresent() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM CauHinhGiaVe WHERE LoaiGhe IN ('THUONG', 'VIP')", Integer.class);
        if (count == null || count != 2) {
            throw new IllegalStateException("Thiếu cấu hình loại ghế; hãy chạy lại migration V4 sau khi kiểm tra dữ liệu.");
        }
    }

    static ValidatedSchedule validate(ScheduleRequest request) {
        if (request == null || blank(request.maPhim()) || blank(request.maPhong())
                || blank(request.maQuanLy()) || request.ngayChieu() == null) {
            throw new IllegalArgumentException("Vui lòng nhập phim, ngày chiếu, giờ, phòng và quản lý suất chiếu.");
        }
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(request.gioBatDau());
            end = LocalTime.parse(request.gioKetThuc());
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không hợp lệ.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu trong cùng ngày.");
        }
        if (!request.ngayChieu().atTime(start).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Suất chiếu mới phải bắt đầu trong tương lai.");
        }
        return new ValidatedSchedule(
                request.maPhim().trim(), request.maPhong().trim(), request.maQuanLy().trim(),
                request.ngayChieu(), start, end
        );
    }

    private void ensureReferences(ValidatedSchedule value) {
        ensureExists("SELECT COUNT(*) FROM Phim WHERE MaPhim = ?", value.movieId(), "Không tìm thấy phim đã chọn.");
        ensureExists("SELECT COUNT(*) FROM Phong WHERE MaPhong = ?", value.roomId(), "Không tìm thấy phòng chiếu đã chọn.");
        ensureExists("SELECT COUNT(*) FROM NhanVienQuanLy WHERE MaQuanLy = ?", value.managerId(),
                "Người quản lý suất chiếu không hợp lệ.");
    }

    private void ensureExists(String sql, String id, String message) {
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        if (count == null || count == 0) throw new IllegalArgumentException(message);
    }

    private void lockRooms(List<String> roomIds) {
        for (String roomId : roomIds.stream().distinct().sorted().toList()) {
            String lockedRoom = jdbc.query("""
                    SELECT MaPhong FROM Phong WHERE MaPhong = ? FOR UPDATE
                    """, rs -> rs.next() ? rs.getString(1) : null, roomId);
            if (lockedRoom == null) throw new IllegalArgumentException("Không tìm thấy phòng chiếu.");
        }
    }

    private void ensureNoOverlap(ValidatedSchedule value, String exceptId) {
        String sql = exceptId == null ? """
                SELECT COUNT(*) FROM SuatChieu
                WHERE MaPhong = ? AND NgayChieu = ?
                  AND GioBatDau < ? AND GioKetThuc > ?
                """ : """
                SELECT COUNT(*) FROM SuatChieu
                WHERE MaPhong = ? AND NgayChieu = ?
                  AND GioBatDau < ? AND GioKetThuc > ?
                  AND MaSuatChieu <> ?
                """;
        Integer conflicts = exceptId == null
                ? jdbc.queryForObject(sql, Integer.class, value.roomId(), value.date(), value.end(), value.start())
                : jdbc.queryForObject(sql, Integer.class, value.roomId(), value.date(), value.end(), value.start(), exceptId);
        if (conflicts != null && conflicts > 0) {
            throw new IllegalStateException("Phòng chiếu đã có suất giao nhau trong khoảng thời gian này.");
        }
    }

    private String roomForUpdate(String showtimeId) {
        return jdbc.query("""
                SELECT MaPhong FROM SuatChieu WHERE MaSuatChieu = ? FOR UPDATE
                """, rs -> {
            if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy suất chiếu.");
            return rs.getString("MaPhong");
        }, showtimeId);
    }

    private void ensureNoReservations(String showtimeId) {
        Integer details = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ChiTietGheHoaDon WHERE MaSuatChieu = ?", Integer.class, showtimeId);
        Integer tickets = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Ve WHERE MaSuatChieu = ?", Integer.class, showtimeId);
        Integer holds = jdbc.queryForObject("""
                SELECT COUNT(*) FROM GheGiuThanhToan
                WHERE MaSuatChieu = ? AND HetHanLuc > CURRENT_TIMESTAMP
                """, Integer.class, showtimeId);
        if ((details != null && details > 0) || (tickets != null && tickets > 0)
                || (holds != null && holds > 0)) {
            throw new IllegalStateException("Không thể sửa hoặc xóa suất đã có vé, đơn hàng hoặc ghế đang được giữ.");
        }
    }

    private ScheduleView schedule(String showtimeId) {
        return jdbc.query("""
                SELECT SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                       TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau,
                       TIME_FORMAT(SC.GioKetThuc, '%H:%i') AS GioKetThuc,
                       SC.MaPhong, PH.TenPhong, SC.MaQuanLy, NV.HoTen AS TenQuanLy,
                       PH.SucChua, COUNT(DISTINCT G.MaGhe) AS SoGhe,
                       COUNT(DISTINCT V.MaVe) AS SoVeDaBan,
                       COUNT(DISTINCT H.MaGhe) AS SoGheDangGiu,
                       COUNT(DISTINCT C.MaHoaDon) AS SoDonLienQuan
                FROM SuatChieu SC
                JOIN Phim P ON P.MaPhim = SC.MaPhim
                JOIN Phong PH ON PH.MaPhong = SC.MaPhong
                JOIN NhanVien NV ON NV.MaNhanVien = SC.MaQuanLy
                LEFT JOIN Ghe G ON G.MaPhong = SC.MaPhong
                LEFT JOIN Ve V ON V.MaSuatChieu = SC.MaSuatChieu AND V.MaPhong = SC.MaPhong AND V.MaGhe = G.MaGhe
                LEFT JOIN GheGiuThanhToan H ON H.MaSuatChieu = SC.MaSuatChieu AND H.MaPhong = SC.MaPhong
                              AND H.MaGhe = G.MaGhe AND H.HetHanLuc > CURRENT_TIMESTAMP
                LEFT JOIN ChiTietGheHoaDon C ON C.MaSuatChieu = SC.MaSuatChieu
                WHERE SC.MaSuatChieu = ?
                GROUP BY SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                         SC.GioBatDau, SC.GioKetThuc, SC.MaPhong, PH.TenPhong,
                         SC.MaQuanLy, NV.HoTen, PH.SucChua
                """, (rs, rowNum) -> new ScheduleView(
                rs.getString("MaSuatChieu"), rs.getString("MaPhim"), rs.getString("TenPhim"),
                rs.getDate("NgayChieu").toLocalDate(), rs.getString("GioBatDau"), rs.getString("GioKetThuc"),
                rs.getString("MaPhong"), rs.getString("TenPhong"), rs.getString("MaQuanLy"),
                rs.getString("TenQuanLy"), (Integer) rs.getObject("SucChua"), rs.getInt("SoGhe"),
                rs.getInt("SoVeDaBan"), rs.getInt("SoGheDangGiu"), rs.getLong("SoDonLienQuan")
        ), showtimeId).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu."));
    }

    private void audit(String adminId, String action) {
        jdbc.update("""
                INSERT INTO NhatKyTaiKhoan (MaTaiKhoan, HanhDong, NguoiThucHien)
                VALUES (?, ?, ?)
                """, adminId, action, adminId);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    static record ValidatedSchedule(
            String movieId,
            String roomId,
            String managerId,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {}

    private record TicketPriceRow(String seatType, int price, LocalDateTime updatedAt) {}
}
