package com.ptit.cinema.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.function.Supplier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/danh-muc")
public class CatalogController {
    private record Source(String table, String columns, String order) {}

    private static final Map<String, Source> SOURCES = Map.ofEntries(
        Map.entry("nhan-vien", new Source("NhanVien", "MaNhanVien,HoTen,NgaySinh,Sdt,Email,MaChucVu", "MaNhanVien")),
        Map.entry("phong-chieu", new Source("Phong", "MaPhong,TenPhong,LoaiPhong,Tang,SucChua", "MaPhong")),
        Map.entry("ghe", new Source("Ghe", "MaPhong,MaGhe,LoaiGhe,HangGhe", "MaPhong,MaGhe")),
        Map.entry("suat-chieu", new Source("SuatChieu", "MaSuatChieu,MaPhim,MaPhong,NgayChieu,GioBatDau,GioKetThuc,MaQuanLy", "NgayChieu,GioBatDau,MaSuatChieu")),
        Map.entry("ve", new Source("Ve", "MaVe,MaSuatChieu,MaPhong,MaGhe,Gia,MaHoaDon,MaNVBanve", "MaVe")),
        Map.entry("hoa-don", new Source("HoaDon", "MaHoaDon,MaKhachHang,ThoiGianThanhToan,HinhThucThanhToan,ThanhTien", "MaHoaDon")),
        Map.entry("san-pham", new Source("SanPham", "MaSanPham,TenSanPham,Gia", "MaSanPham")),
        Map.entry("chi-tiet-san-pham", new Source("BaoGom", "MaHoaDon,MaSanPham,SoLuong", "MaHoaDon,MaSanPham")),
        Map.entry("the-loai", new Source("TheLoaiPhim", "MaPhim,TheLoai", "MaPhim,TheLoai")),
        Map.entry("dao-dien", new Source("DaoDienPhim", "MaPhim,DaoDien", "MaPhim,DaoDien")),
        Map.entry("chuc-vu", new Source("ChucVu", "MaChucVu,TenChucVu", "MaChucVu")),
        Map.entry("quan-ly", new Source("NhanVienQuanLy", "MaQuanLy,LuongCung", "MaQuanLy")),
        Map.entry("nhan-vien-thuong", new Source("NhanVienThuong", "MaNVThuong,LuongTheoGio", "MaNVThuong")),
        Map.entry("loai-ca", new Source("LoaiCaLam", "MaLoaiCaLam,ThoiGianBatDau,ThoiGianKetThuc", "MaLoaiCaLam")),
        Map.entry("ca-lam", new Source("CaLam", "MaCaLam,MaLoaiCaLam,Ngay", "Ngay,MaCaLam")),
        Map.entry("phan-ca", new Source("Lam", "MaNV,MaCaLam", "MaNV,MaCaLam"))
    );

    private final JdbcTemplate jdbc;
    private static final Set<String> TEMPORAL = Set.of("NgaySinh","NgayChieu","GioBatDau","GioKetThuc",
        "ThoiGianThanhToan","ThoiGianBatDau","ThoiGianKetThuc","Ngay");

    private static final Map<String, List<String>> TYPES = Map.of(
        "nhan-vien", List.of("s", "s", "d", "s", "s", "s"),
        "phong-chieu", List.of("s", "s", "s", "i", "i"),
        "ve", List.of("s", "s", "s", "s", "i", "s", "s"),
        "hoa-don", List.of("s", "s", "dt", "s", "i"),
        "san-pham", List.of("s", "s", "i")
    );
    private static final Map<String, List<String>> DEPENDENCIES = Map.of(
        "phim", List.of("SuatChieu WHERE MaPhim=?"),
        "khach-hang", List.of("HoaDon WHERE MaKhachHang=?", "TaiKhoan WHERE MaKhachHang=?"),
        "nhan-vien", List.of("SuatChieu WHERE MaQuanLy=?", "Ve WHERE MaNVBanve=?", "Lam WHERE MaNV=?", "TaiKhoan WHERE MaNhanVien=?"),
        "phong-chieu", List.of("SuatChieu WHERE MaPhong=?", "Ghe WHERE MaPhong=?"),
        "suat-chieu", List.of("Ve WHERE MaSuatChieu=?"),
        "hoa-don", List.of("Ve WHERE MaHoaDon=?", "BaoGom WHERE MaHoaDon=?"),
        "san-pham", List.of("BaoGom WHERE MaSanPham=?")
    );

    public CatalogController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/{name}")
    public List<Map<String, Object>> list(@PathVariable String name) {
        Source s = SOURCES.get(name);
        if (s == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String projection = Arrays.stream(s.columns().split(","))
            .map(c -> TEMPORAL.contains(c) ? "CAST(" + c + " AS CHAR) AS " + c : c)
            .collect(Collectors.joining(","));
        if (name.equals("ve") || name.equals("hoa-don")) {
            projection += ", EXISTS(SELECT 1 FROM ChiTietGheHoaDon C WHERE C.MaHoaDon="
                + s.table() + ".MaHoaDon) AS DatTrucTuyen";
        }
        return jdbc.queryForList("SELECT " + projection + " FROM " + s.table() + " ORDER BY " + s.order());
    }

    @PostMapping("/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void create(@PathVariable String name, @RequestBody Map<String, Object> body) {
        Source s = writable(name);
        String[] columns = s.columns().split(",");
        Object[] values = values(name, columns, body);
        if (name.equals("ve")) checkTicketTarget(values);
        save(() -> {
            int count = jdbc.update("INSERT INTO " + s.table() + " (" + s.columns() + ") VALUES ("
                + String.join(",", Collections.nCopies(columns.length, "?")) + ")", values);
            if (name.equals("nhan-vien")) addEmployeeRole((String) values[0], (String) values[5]);
            return count;
        });
    }

    @PutMapping("/{name}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void update(@PathVariable String name, @PathVariable String id, @RequestBody Map<String, Object> body) {
        Source s = writable(name);
        String[] columns = s.columns().split(",");
        Object[] values = values(name, columns, body);
        if (!id.equals(values[0])) throw problem(HttpStatus.BAD_REQUEST, "Không được đổi mã khi sửa.");
        lock(s, id);
        checkManualRecord(name, id);
        if (name.equals("ve")) checkTicketTarget(values);
        Object[] args = Arrays.copyOfRange(values, 1, values.length + 1);
        args[args.length - 1] = id;
        String assignments = String.join("=?,", Arrays.copyOfRange(columns, 1, columns.length)) + "=?";
        save(() -> {
            if (name.equals("nhan-vien")) {
                String previous = jdbc.queryForObject("SELECT MaChucVu FROM NhanVien WHERE MaNhanVien=?", String.class, id);
                String next = (String) values[5];
                if (!java.util.Objects.equals(previous, next)) {
                    checkUnused(name, id);
                    jdbc.update("DELETE FROM NhanVienQuanLy WHERE MaQuanLy=?", id);
                    jdbc.update("DELETE FROM NhanVienThuong WHERE MaNVThuong=?", id);
                    addEmployeeRole(id, next);
                }
            }
            return jdbc.update("UPDATE " + s.table() + " SET " + assignments + " WHERE " + columns[0] + "=?", args);
        });
    }

    @DeleteMapping("/{name}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable String name, @PathVariable String id) {
        Source s = switch (name) {
            case "phim" -> new Source("Phim", "MaPhim", "MaPhim");
            case "khach-hang" -> new Source("KhachHang", "MaKhachHang", "MaKhachHang");
            default -> writable(name);
        };
        lock(s, id);
        checkManualRecord(name, id);
        checkUnused(name, id);
        save(() -> jdbc.update("DELETE FROM " + s.table() + " WHERE " + s.columns().split(",")[0] + "=?", id));
    }

    private Source writable(String name) {
        if (!TYPES.containsKey(name)) throw problem(HttpStatus.NOT_FOUND, "Danh mục không hỗ trợ chỉnh sửa.");
        return SOURCES.get(name);
    }

    private void lock(Source source, String id) {
        String key = source.columns().split(",")[0];
        if (jdbc.queryForList("SELECT " + key + " FROM " + source.table() + " WHERE " + key + "=? FOR UPDATE", id).isEmpty())
            throw problem(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi.");
    }

    private void checkUnused(String name, String id) {
        for (String ref : DEPENDENCIES.getOrDefault(name, List.of())) {
            if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM " + ref + ")", Boolean.class, id)))
                throw problem(HttpStatus.CONFLICT, "Bản ghi đang được sử dụng ở mục khác. Hãy xử lý dữ liệu liên quan trước.");
        }
    }

    private void checkManualRecord(String name, String id) {
        if (name.equals("hoa-don")) checkManualInvoice(id);
        if (name.equals("ve")) {
            String invoice = jdbc.queryForObject("SELECT MaHoaDon FROM Ve WHERE MaVe=?", String.class, id);
            checkManualInvoice(invoice);
        }
    }

    private void checkManualInvoice(String invoice) {
        lock(SOURCES.get("hoa-don"), invoice);
        if (Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM ChiTietGheHoaDon WHERE MaHoaDon=?)
            """, Boolean.class, invoice))) {
            throw problem(HttpStatus.CONFLICT, "Đơn đặt trực tuyến được xử lý trong mục quản trị thanh toán.");
        }
    }

    private void checkTicketTarget(Object[] values) {
        if (values[1] == null || values[2] == null || values[3] == null || values[4] == null
            || values[5] == null || values[6] == null) {
            throw problem(HttpStatus.BAD_REQUEST, "Nhập đủ thông tin suất, ghế, giá vé, hóa đơn và nhân viên.");
        }
        lock(SOURCES.get("suat-chieu"), (String) values[1]);
        checkManualInvoice((String) values[5]);
        if (!jdbc.queryForList("""
            SELECT MaHoaDon FROM GheGiuThanhToan
            WHERE MaSuatChieu=? AND MaPhong=? AND MaGhe=? AND HetHanLuc > CURRENT_TIMESTAMP
            FOR UPDATE
            """, values[1], values[2], values[3]).isEmpty()) {
            throw problem(HttpStatus.CONFLICT, "Ghế đang được giữ cho một đơn chuyển khoản.");
        }
    }

    private void addEmployeeRole(String id, String role) {
        if ("CV01".equals(role)) jdbc.update("INSERT INTO NhanVienQuanLy (MaQuanLy) VALUES (?)", id);
        if ("CV02".equals(role)) jdbc.update("INSERT INTO NhanVienThuong (MaNVThuong) VALUES (?)", id);
    }

    private Object[] values(String name, String[] columns, Map<String, Object> body) {
        Object[] result = new Object[columns.length];
        List<String> types = TYPES.get(name);
        for (int i = 0; i < columns.length; i++) {
            Object raw = body.get(columns[i]);
            if (raw == null || raw.toString().isBlank()) continue;
            String str = raw.toString().strip();
            try {
                result[i] = switch (types.get(i)) {
                    case "i" -> Integer.valueOf(str);
                    case "d" -> LocalDate.parse(str);
                    case "t" -> LocalTime.parse(str);
                    case "dt" -> LocalDateTime.parse(str);
                    default -> str;
                };
            } catch (RuntimeException ex) {
                throw problem(HttpStatus.BAD_REQUEST, "Giá trị không hợp lệ: " + columns[i]);
            }
        }
        if (!(result[0] instanceof String id) || !id.matches("[A-Za-z0-9_-]+"))
            throw problem(HttpStatus.BAD_REQUEST, "Mã không hợp lệ.");
        return result;
    }

    private void save(Supplier<Integer> action) {
        try { action.get(); }
        catch (DataIntegrityViolationException ex) {
            String state = ex.getMostSpecificCause() instanceof java.sql.SQLException sql ? sql.getSQLState() : "";
            int code = ex.getMostSpecificCause() instanceof java.sql.SQLException sql ? sql.getErrorCode() : 0;
            String message = switch (code) {
                case 1062 -> "Mã đã tồn tại hoặc ghế đã được bán cho suất chiếu này.";
                case 1451, 1452 -> "Mã liên quan không hợp lệ hoặc dữ liệu đang được sử dụng.";
                case 3819 -> "Dữ liệu không thỏa điều kiện của bảng. Kiểm tra giờ, số tiền hoặc hình thức thanh toán.";
                default -> "Dữ liệu thiếu, quá dài hoặc không hợp lệ.";
            };
            throw problem("23000".equals(state) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST, message);
        }
    }

    private ResponseStatusException problem(HttpStatus code, String message) {
        return new ResponseStatusException(code, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> explain(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason()));
    }
}
