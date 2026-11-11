-- ============================================================
-- PTIT - CSDL Quản lý Rạp chiếu phim - Nhóm 30
-- Bản hoàn chỉnh: CREATE DATABASE -> TABLE -> INSERT -> 13 queries
-- MySQL 8.x / utf8mb4
-- ============================================================

DROP DATABASE IF EXISTS QuanLyRapChieuPhim;
CREATE DATABASE QuanLyRapChieuPhim
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE QuanLyRapChieuPhim;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Ve, BaoGom, Lam, SuatChieu, HoaDon,
                    CaLam, LoaiCaLam, NhanVienThuong, NhanVienQuanLy,
                    NhanVien, KhachHang, SanPham, Ghe, Phong,
                    DaoDienPhim, TheLoaiPhim, Phim, ChucVu;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. CREATE TABLE
-- ============================================================

CREATE TABLE Phim (
    MaPhim VARCHAR(10) NOT NULL,
    TenPhim VARCHAR(100) NOT NULL,
    ThoiLuong INT,
    NgayKhoiChieu DATE,
    DoanhThuUSD BIGINT,
    PRIMARY KEY (MaPhim)
) ENGINE=InnoDB;

CREATE TABLE TheLoaiPhim (
    TheLoai VARCHAR(30) NOT NULL,
    MaPhim VARCHAR(10) NOT NULL,
    PRIMARY KEY (TheLoai, MaPhim),
    FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE DaoDienPhim (
    DaoDien VARCHAR(100) NOT NULL,
    MaPhim VARCHAR(10) NOT NULL,
    PRIMARY KEY (DaoDien, MaPhim),
    FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Phong (
    MaPhong VARCHAR(25) NOT NULL,
    TenPhong VARCHAR(50),
    LoaiPhong VARCHAR(50),
    Tang INT,
    SucChua INT,
    PRIMARY KEY (MaPhong)
) ENGINE=InnoDB;

CREATE TABLE Ghe (
    MaGhe VARCHAR(12) NOT NULL,
    LoaiGhe VARCHAR(10),
    HangGhe VARCHAR(10),
    MaPhong VARCHAR(25) NOT NULL,
    PRIMARY KEY (MaGhe, MaPhong),
    FOREIGN KEY (MaPhong) REFERENCES Phong(MaPhong)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE SanPham (
    MaSanPham VARCHAR(8) NOT NULL,
    TenSanPham VARCHAR(35),
    Gia INT,
    PRIMARY KEY (MaSanPham),
    CHECK (Gia >= 0)
) ENGINE=InnoDB;

CREATE TABLE KhachHang (
    MaKhachHang VARCHAR(50) NOT NULL,
    HoTen VARCHAR(50),
    NgaySinh DATE,
    Sdt VARCHAR(50),
    Email VARCHAR(50),
    PRIMARY KEY (MaKhachHang),
    CHECK (Email LIKE '%@%')
) ENGINE=InnoDB;

CREATE TABLE ChucVu (
    MaChucVu VARCHAR(50) NOT NULL,
    TenChucVu VARCHAR(50) NOT NULL,
    PRIMARY KEY (MaChucVu)
) ENGINE=InnoDB;

CREATE TABLE NhanVien (
    MaNhanVien VARCHAR(50) NOT NULL,
    HoTen VARCHAR(50),
    NgaySinh DATE,
    Sdt VARCHAR(50),
    Email VARCHAR(50),
    MaChucVu VARCHAR(50),
    PRIMARY KEY (MaNhanVien),
    FOREIGN KEY (MaChucVu) REFERENCES ChucVu(MaChucVu)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CHECK (Email LIKE '%@%')
) ENGINE=InnoDB;

CREATE TABLE NhanVienQuanLy (
    MaQuanLy VARCHAR(50) NOT NULL,
    LuongCung INT,
    PRIMARY KEY (MaQuanLy),
    FOREIGN KEY (MaQuanLy) REFERENCES NhanVien(MaNhanVien)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (LuongCung >= 0)
) ENGINE=InnoDB;

CREATE TABLE NhanVienThuong (
    MaNVThuong VARCHAR(50) NOT NULL,
    LuongTheoGio INT,
    PRIMARY KEY (MaNVThuong),
    FOREIGN KEY (MaNVThuong) REFERENCES NhanVien(MaNhanVien)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (LuongTheoGio >= 0)
) ENGINE=InnoDB;

CREATE TABLE LoaiCaLam (
    MaLoaiCaLam VARCHAR(50) NOT NULL,
    ThoiGianBatDau TIME,
    ThoiGianKetThuc TIME,
    PRIMARY KEY (MaLoaiCaLam),
    CHECK (ThoiGianBatDau < ThoiGianKetThuc)
) ENGINE=InnoDB;

CREATE TABLE CaLam (
    MaCaLam VARCHAR(50) NOT NULL,
    MaLoaiCaLam VARCHAR(50) NOT NULL,
    Ngay DATE,
    PRIMARY KEY (MaCaLam),
    FOREIGN KEY (MaLoaiCaLam) REFERENCES LoaiCaLam(MaLoaiCaLam)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE HoaDon (
    MaHoaDon VARCHAR(50) NOT NULL,
    HinhThucThanhToan VARCHAR(50),
    ThanhTien INT,
    ThoiGianThanhToan DATETIME,
    MaKhachHang VARCHAR(50),
    PRIMARY KEY (MaHoaDon),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (HinhThucThanhToan IN ('Tiền mặt', 'Chuyển khoản')),
    CHECK (ThanhTien >= 0)
) ENGINE=InnoDB;

CREATE TABLE BaoGom (
    MaHoaDon VARCHAR(50) NOT NULL,
    MaSanPham VARCHAR(50) NOT NULL,
    SoLuong INT,
    PRIMARY KEY (MaHoaDon, MaSanPham),
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (SoLuong > 0)
) ENGINE=InnoDB;

CREATE TABLE Lam (
    MaNV VARCHAR(50) NOT NULL,
    MaCaLam VARCHAR(50) NOT NULL,
    PRIMARY KEY (MaNV, MaCaLam),
    FOREIGN KEY (MaNV) REFERENCES NhanVienThuong(MaNVThuong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaCaLam) REFERENCES CaLam(MaCaLam)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE SuatChieu (
    MaSuatChieu VARCHAR(50) NOT NULL,
    NgayChieu DATE,
    GioBatDau TIME,
    GioKetThuc TIME,
    MaQuanLy VARCHAR(50) NOT NULL,
    MaPhim VARCHAR(50) NOT NULL,
    MaPhong VARCHAR(50) NOT NULL,
    PRIMARY KEY (MaSuatChieu),
    UNIQUE (MaSuatChieu, MaPhong),
    FOREIGN KEY (MaQuanLy) REFERENCES NhanVienQuanLy(MaQuanLy)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaPhong) REFERENCES Phong(MaPhong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (GioBatDau < GioKetThuc)
) ENGINE=InnoDB;

CREATE TABLE Ve (
    MaVe VARCHAR(50) NOT NULL,
    Gia INT,
    MaNVBanve VARCHAR(50) NOT NULL,
    MaSuatChieu VARCHAR(50) NOT NULL,
    MaHoaDon VARCHAR(50) NOT NULL,
    MaGhe VARCHAR(50) NOT NULL,
    MaPhong VARCHAR(50) NOT NULL,
    PRIMARY KEY (MaVe),
    UNIQUE (MaSuatChieu, MaGhe, MaPhong),
    FOREIGN KEY (MaNVBanve) REFERENCES NhanVienThuong(MaNVThuong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaSuatChieu, MaPhong) REFERENCES SuatChieu(MaSuatChieu, MaPhong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaGhe, MaPhong) REFERENCES Ghe(MaGhe, MaPhong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (Gia >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- 2. INSERT DỮ LIỆU MẪU
-- Dữ liệu được thiết kế để 13 truy vấn phía dưới đều có kết quả.
-- ============================================================

INSERT INTO Phim (MaPhim, TenPhim, ThoiLuong, NgayKhoiChieu, DoanhThuUSD) VALUES
('P001', 'Avengers: Endgame', 181, '2019-04-26', 2797800564),
('P002', 'Us', 116, '2019-03-22', 256000000),
('P003', 'John Wick 3', 130, '2019-05-17', 326000000),
('P004', 'Inside Out', 95, '2015-06-19', 850000000),
('P005', 'The Lion King', 118, '2019-07-19', 1657000000),
('P006', 'Avengers: Infinity War', 149, '2018-04-27', 2050000000);

INSERT INTO TheLoaiPhim (TheLoai, MaPhim) VALUES
('Hành động', 'P001'),
('Khoa học viễn tưởng', 'P001'),
('Kinh dị', 'P002'),
('Tâm lý', 'P002'),
('Hành động', 'P003'),
('Hành động', 'P006'),
('Khoa học viễn tưởng', 'P006'),
('Hoạt hình', 'P004'),
('Hoạt hình', 'P005');

-- Hai đạo diễn cùng xuất hiện trong ít nhất 2 phim để Query 5 có kết quả.
INSERT INTO DaoDienPhim (DaoDien, MaPhim) VALUES
('Anthony Russo', 'P001'),
('Joe Russo', 'P001'),
('Anthony Russo', 'P006'),
('Joe Russo', 'P006'),
('Jordan Peele', 'P002'),
('Chad Stahelski', 'P003'),
('Pete Docter', 'P004'),
('Jon Favreau', 'P005');

INSERT INTO Phong (MaPhong, TenPhong, LoaiPhong, Tang, SucChua) VALUES
('R01', 'Phòng IMAX', 'IMAX', 2, 10),
('R02', 'Phòng Premium', '2D Premium', 2, 10),
('R03', 'Phòng Standard', '2D Standard', 1, 10);

INSERT INTO Ghe (MaGhe, LoaiGhe, HangGhe, MaPhong) VALUES
('A01', 'Thường', 'A', 'R01'), ('A02', 'Thường', 'A', 'R01'), ('A03', 'VIP', 'A', 'R01'), ('A04', 'VIP', 'A', 'R01'), ('A05', 'Thường', 'A', 'R01'),
('B01', 'Thường', 'B', 'R01'), ('B02', 'Thường', 'B', 'R01'), ('B03', 'VIP', 'B', 'R01'), ('B04', 'VIP', 'B', 'R01'), ('B05', 'Thường', 'B', 'R01'),
('A01', 'Thường', 'A', 'R02'), ('A02', 'Thường', 'A', 'R02'), ('A03', 'VIP', 'A', 'R02'), ('A04', 'VIP', 'A', 'R02'), ('A05', 'Thường', 'A', 'R02'),
('B01', 'Thường', 'B', 'R02'), ('B02', 'Thường', 'B', 'R02'), ('B03', 'VIP', 'B', 'R02'), ('B04', 'VIP', 'B', 'R02'), ('B05', 'Thường', 'B', 'R02'),
('A01', 'Thường', 'A', 'R03'), ('A02', 'Thường', 'A', 'R03'), ('A03', 'VIP', 'A', 'R03'), ('A04', 'VIP', 'A', 'R03'), ('A05', 'Thường', 'A', 'R03'),
('B01', 'Thường', 'B', 'R03'), ('B02', 'Thường', 'B', 'R03'), ('B03', 'VIP', 'B', 'R03'), ('B04', 'VIP', 'B', 'R03'), ('B05', 'Thường', 'B', 'R03');

INSERT INTO SanPham (MaSanPham, TenSanPham, Gia) VALUES
('SP01', 'Bắp rang bơ', 50000),
('SP02', 'Nước suối', 20000),
('SP03', 'Coca Cola', 25000),
('SP04', 'Combo Bắp + Nước', 80000);

INSERT INTO KhachHang (MaKhachHang, HoTen, NgaySinh, Sdt, Email) VALUES
('KH001', 'Nguyễn Minh An', '1995-03-12', '0901000001', 'an.nguyen@gmail.com'),
('KH002', 'Trần Hoàng Nam', '1992-07-25', '0901000002', 'nam.tran@gmail.com'),
('KH003', 'Lê Thu Hà', '2005-09-10', '0901000003', 'ha.le@gmail.com'),
('KH004', 'Phạm Gia Bảo', '1988-01-18', '0901000004', 'bao.pham@gmail.com'),
('KH005', 'Đỗ Ngọc Mai', '1990-11-02', '0901000005', 'mai.do@gmail.com');

INSERT INTO ChucVu (MaChucVu, TenChucVu) VALUES
('CV01', 'Quản lý'),
('CV02', 'Nhân viên bán vé');

INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, Sdt, Email, MaChucVu) VALUES
('NV001', 'Lê Văn Tài', '1985-04-20', '0912000001', 'tai.le@gmail.com', 'CV01'),
('NV002', 'Trần Minh Anh', '1998-05-11', '0912000002', 'anh.tran@gmail.com', 'CV02'),
('NV003', 'Trần Quốc Huy', '1996-10-05', '0912000003', 'huy.tran@gmail.com', 'CV02'),
('NV004', 'Nguyễn Lan', '1999-02-27', '0912000004', 'lan.nguyen@gmail.com', 'CV02'),
('NV005', 'Phạm Đức', '1997-08-19', '0912000005', 'duc.pham@gmail.com', 'CV02');

INSERT INTO NhanVienQuanLy (MaQuanLy, LuongCung) VALUES
('NV001', 18000000);

INSERT INTO NhanVienThuong (MaNVThuong, LuongTheoGio) VALUES
('NV002', 45000),
('NV003', 50000),
('NV004', 42000),
('NV005', 48000);

INSERT INTO LoaiCaLam (MaLoaiCaLam, ThoiGianBatDau, ThoiGianKetThuc) VALUES
('LC01', '08:00:00', '16:00:00'),
('LC02', '16:00:00', '23:00:00'),
('LC03', '10:00:00', '18:00:00');

INSERT INTO CaLam (MaCaLam, MaLoaiCaLam, Ngay) VALUES
('CA01', 'LC01', '2024-11-25'),
('CA02', 'LC02', '2024-11-25'),
('CA03', 'LC01', '2024-11-26'),
('CA04', 'LC02', '2024-11-26'),
('CA05', 'LC03', '2024-11-28'),
('CA06', 'LC01', '2024-11-30'),
('CA07', 'LC02', '2024-12-01'),
('CA08', 'LC01', '2024-12-05');

INSERT INTO Lam (MaNV, MaCaLam) VALUES
('NV002', 'CA01'), ('NV002', 'CA03'), ('NV002', 'CA06'),
('NV003', 'CA02'), ('NV003', 'CA04'), ('NV003', 'CA07'),
('NV004', 'CA01'), ('NV004', 'CA05'),
('NV005', 'CA03'), ('NV005', 'CA08');

INSERT INTO SuatChieu (MaSuatChieu, NgayChieu, GioBatDau, GioKetThuc, MaQuanLy, MaPhim, MaPhong) VALUES
('SC001', '2024-11-25', '09:00:00', '12:01:00', 'NV001', 'P001', 'R01'),
('SC002', '2024-11-25', '19:00:00', '21:00:00', 'NV001', 'P002', 'R02'),
('SC003', '2024-11-26', '10:00:00', '13:01:00', 'NV001', 'P001', 'R01'),
('SC004', '2024-11-26', '20:00:00', '22:30:00', 'NV001', 'P006', 'R03'),
('SC005', '2024-11-28', '18:00:00', '20:10:00', 'NV001', 'P003', 'R01'),
('SC006', '2024-11-30', '10:00:00', '13:01:00', 'NV001', 'P001', 'R02'),
('SC007', '2024-11-30', '19:00:00', '21:00:00', 'NV001', 'P002', 'R03'),
('SC008', '2024-12-01', '09:00:00', '11:10:00', 'NV001', 'P003', 'R01'),
('SC009', '2024-12-01', '20:00:00', '22:30:00', 'NV001', 'P006', 'R02'),
('SC010', '2024-12-02', '11:00:00', '12:35:00', 'NV001', 'P004', 'R03'),
('SC011', '2024-12-05', '17:00:00', '20:01:00', 'NV001', 'P001', 'R01');

-- ============================================================
-- Hóa đơn: KH001 có giao dịch cả tháng 11 và tháng 12;
-- KH001/002 > 200k doanh số tháng 11; KH001 xem Avengers nhưng không xem Us.
-- Một hóa đơn được gắn với 1 suất chiếu trong dữ liệu mẫu để Query 13 rõ ràng.
-- ============================================================
INSERT INTO HoaDon (MaHoaDon, HinhThucThanhToan, ThanhTien, ThoiGianThanhToan, MaKhachHang) VALUES
('HD001', 'Chuyển khoản', 250000, '2024-11-25 09:10:00', 'KH001'),
('HD002', 'Tiền mặt',      150000, '2024-11-25 19:05:00', 'KH002'),
('HD003', 'Chuyển khoản', 300000, '2024-11-26 10:15:00', 'KH001'),
('HD004', 'Tiền mặt',      200000, '2024-11-26 20:20:00', 'KH002'),
('HD005', 'Chuyển khoản', 240000, '2024-11-28 18:05:00', 'KH004'),
('HD006', 'Tiền mặt',      180000, '2024-11-30 10:05:00', 'KH005'),
('HD007', 'Chuyển khoản', 220000, '2024-11-30 10:10:00', 'KH002'),
('HD008', 'Tiền mặt',       90000, '2024-11-30 19:05:00', 'KH003'),
('HD009', 'Chuyển khoản', 200000, '2024-12-01 09:05:00', 'KH001'),
('HD010', 'Tiền mặt',      220000, '2024-12-01 20:05:00', 'KH003'),
('HD011', 'Chuyển khoản', 100000, '2024-12-02 11:05:00', 'KH005'),
('HD012', 'Tiền mặt',      100000, '2024-12-05 17:05:00', 'KH004');

INSERT INTO BaoGom (MaHoaDon, MaSanPham, SoLuong) VALUES
('HD001', 'SP01', 3),
('HD001', 'SP02', 2),
('HD003', 'SP01', 4),
('HD003', 'SP03', 2),
('HD005', 'SP01', 2),
('HD006', 'SP02', 2),
('HD007', 'SP01', 2),
('HD009', 'SP01', 3),
('HD010', 'SP02', 2),
('HD011', 'SP04', 1),
('HD012', 'SP03', 1);

-- Vé: tổng thể tích hợp đủ các trường hợp cho 13 truy vấn.
INSERT INTO Ve (MaVe, Gia, MaNVBanve, MaSuatChieu, MaHoaDon, MaGhe, MaPhong) VALUES
-- 25/11 SC001 (Avengers, sáng): 5 vé
('V001', 100000, 'NV002', 'SC001', 'HD001', 'A01', 'R01'),
('V002', 100000, 'NV002', 'SC001', 'HD001', 'A02', 'R01'),
('V003',  50000, 'NV002', 'SC001', 'HD001', 'A03', 'R01'),
-- 25/11 SC002 (Us, tối)
('V004', 100000, 'NV003', 'SC002', 'HD002', 'A01', 'R02'),
('V005',  50000, 'NV003', 'SC002', 'HD002', 'A02', 'R02'),
-- 26/11 SC003 (Avengers, sáng)
('V006', 100000, 'NV002', 'SC003', 'HD003', 'A01', 'R01'),
('V007', 100000, 'NV002', 'SC003', 'HD003', 'A02', 'R01'),
('V008', 100000, 'NV002', 'SC003', 'HD003', 'A03', 'R01'),
-- 26/11 SC004 (Infinity War)
('V009', 100000, 'NV003', 'SC004', 'HD004', 'A01', 'R03'),
('V010', 100000, 'NV003', 'SC004', 'HD004', 'A02', 'R03'),
-- 28/11 SC005 (John Wick)
('V011', 100000, 'NV003', 'SC005', 'HD005', 'A04', 'R01'),
('V012', 100000, 'NV003', 'SC005', 'HD005', 'A05', 'R01'),
-- 30/11 SC006 (Avengers, sáng)
('V013', 100000, 'NV002', 'SC006', 'HD006', 'A01', 'R02'),
('V014',  80000, 'NV002', 'SC006', 'HD006', 'A02', 'R02'),
('V015', 100000, 'NV003', 'SC006', 'HD007', 'A03', 'R02'),
('V016', 100000, 'NV003', 'SC006', 'HD007', 'A04', 'R02'),
-- 30/11 SC007 (Us, tối)
('V017',  90000, 'NV004', 'SC007', 'HD008', 'A01', 'R03'),
-- 01/12 SC008 (John Wick, sáng)
('V018', 100000, 'NV003', 'SC008', 'HD009', 'A01', 'R01'),
('V019', 100000, 'NV003', 'SC008', 'HD009', 'A02', 'R01'),
-- 01/12 SC009 (Infinity War)
('V020', 100000, 'NV004', 'SC009', 'HD010', 'A01', 'R02'),
('V021', 100000, 'NV004', 'SC009', 'HD010', 'A02', 'R02'),
-- 02/12 SC010 (Inside Out, sáng)
('V022', 100000, 'NV005', 'SC010', 'HD011', 'A01', 'R03'),
-- 05/12 SC011 (Avengers)
('V023', 100000, 'NV004', 'SC011', 'HD012', 'A01', 'R01');

-- ============================================================
-- 3. 13 TRUY VẤN ĐÃ HIỆU CHỈNH
-- ============================================================

-- Q1. Liệt kê thông tin các suất chiếu tại 26/11/2024
SELECT
    SC.MaSuatChieu,
    PH.TenPhong,
    SC.GioBatDau,
    P.TenPhim,
    P.ThoiLuong,
    GROUP_CONCAT(DISTINCT DP.DaoDien ORDER BY DP.DaoDien SEPARATOR ', ') AS DaoDien,
    GROUP_CONCAT(DISTINCT TL.TheLoai ORDER BY TL.TheLoai SEPARATOR ', ') AS TheLoai
FROM SuatChieu SC
JOIN Phong PH ON PH.MaPhong = SC.MaPhong
JOIN Phim P ON SC.MaPhim = P.MaPhim
LEFT JOIN DaoDienPhim DP ON P.MaPhim = DP.MaPhim
LEFT JOIN TheLoaiPhim TL ON P.MaPhim = TL.MaPhim
WHERE SC.NgayChieu = '2024-11-26'
GROUP BY SC.MaSuatChieu, PH.TenPhong, SC.GioBatDau, P.TenPhim, P.ThoiLuong;

-- Q2. Suất chiếu có doanh số lớn nhất do Lê Văn Tài quản lý
SELECT
    SC.MaSuatChieu,
    PH.TenPhong,
    P.TenPhim,
    SUM(V.Gia) AS DoanhThu,
    COUNT(V.MaVe) AS SoVe
FROM SuatChieu SC
JOIN Ve V ON SC.MaSuatChieu = V.MaSuatChieu
JOIN Phim P ON SC.MaPhim = P.MaPhim
JOIN Phong PH ON SC.MaPhong = PH.MaPhong
JOIN NhanVienQuanLy QL ON QL.MaQuanLy = SC.MaQuanLy
JOIN NhanVien NV ON NV.MaNhanVien = QL.MaQuanLy
WHERE SC.NgayChieu >= '2024-11-25'
  AND SC.NgayChieu <= '2024-12-05'
  AND NV.HoTen = 'Lê Văn Tài'
GROUP BY SC.MaSuatChieu, PH.TenPhong, P.TenPhim
ORDER BY DoanhThu DESC
LIMIT 1;

-- Q3. Liệt kê số ghế trống của từng suất chiếu ngày 25/11/2024
SELECT
    SC.MaSuatChieu,
    SC.GioBatDau,
    PH.MaPhong,
    PH.TenPhong,
    COUNT(G.MaGhe) AS TongGheTrong,
    GROUP_CONCAT(G.MaGhe ORDER BY G.MaGhe) AS DanhSachGheTrong
FROM SuatChieu SC
JOIN Phong PH ON PH.MaPhong = SC.MaPhong
JOIN Ghe G ON G.MaPhong = PH.MaPhong
LEFT JOIN Ve V
    ON V.MaSuatChieu = SC.MaSuatChieu
   AND V.MaGhe = G.MaGhe
   AND V.MaPhong = G.MaPhong
WHERE SC.NgayChieu = '2024-11-25'
  AND V.MaVe IS NULL
GROUP BY SC.MaSuatChieu, SC.GioBatDau, PH.MaPhong, PH.TenPhong;

-- Q4. Phòng có số vé > 3 trong 20/11/2024 - 05/12/2024
SELECT
    PH.TenPhong,
    SUM(V.Gia) AS DoanhThu,
    COUNT(V.MaVe) AS SoVeBan
FROM Phong PH
JOIN SuatChieu SC ON SC.MaPhong = PH.MaPhong
JOIN Ve V ON SC.MaSuatChieu = V.MaSuatChieu
WHERE SC.NgayChieu >= '2024-11-20'
  AND SC.NgayChieu <= '2024-12-05'
GROUP BY PH.MaPhong, PH.TenPhong
HAVING SoVeBan > 3
ORDER BY DoanhThu DESC;

-- Q5. Các cặp đạo diễn làm chung ít nhất 2 phim
SELECT
    D1.DaoDien AS DaoDien1,
    D2.DaoDien AS DaoDien2,
    GROUP_CONCAT(P.TenPhim ORDER BY P.TenPhim SEPARATOR ', ') AS DanhSachPhimChung
FROM DaoDienPhim D1
JOIN DaoDienPhim D2
    ON D1.MaPhim = D2.MaPhim
JOIN Phim P ON D1.MaPhim = P.MaPhim
WHERE D1.DaoDien < D2.DaoDien
GROUP BY D1.DaoDien, D2.DaoDien
HAVING COUNT(*) >= 2;

-- Q6. Tính giờ làm và tiền lương 25/11/2024 - 05/12/2024
SELECT
    NVT.MaNVThuong,
    NV.HoTen,
    CV.TenChucVu AS ViTri,
    SUM(TIMESTAMPDIFF(HOUR,
        LoaiCa.ThoiGianBatDau,
        LoaiCa.ThoiGianKetThuc)) AS TongGioLam,
    NVT.LuongTheoGio,
    SUM(TIMESTAMPDIFF(HOUR,
        LoaiCa.ThoiGianBatDau,
        LoaiCa.ThoiGianKetThuc) * NVT.LuongTheoGio) AS TongLuong
FROM Lam
JOIN NhanVienThuong NVT ON Lam.MaNV = NVT.MaNVThuong
JOIN NhanVien NV ON NVT.MaNVThuong = NV.MaNhanVien
LEFT JOIN ChucVu CV ON NV.MaChucVu = CV.MaChucVu
JOIN CaLam Ca ON Lam.MaCaLam = Ca.MaCaLam
JOIN LoaiCaLam LoaiCa ON Ca.MaLoaiCaLam = LoaiCa.MaLoaiCaLam
WHERE Ca.Ngay >= '2024-11-25'
  AND Ca.Ngay <= '2024-12-05'
GROUP BY NVT.MaNVThuong, NV.HoTen, CV.TenChucVu, NVT.LuongTheoGio
ORDER BY NVT.MaNVThuong;

-- Q7. Nhân viên họ Trần có doanh số bán vé >= 200000 trong tháng 11/2024.
SELECT
    NV.MaNhanVien,
    NV.HoTen,
    NV.NgaySinh,
    NV.Sdt,
    NV.Email,
    CV.TenChucVu AS ViTri,
    SUM(V.Gia) AS DoanhSoVe
FROM Ve V
JOIN HoaDon HD ON V.MaHoaDon = HD.MaHoaDon
JOIN NhanVien NV ON NV.MaNhanVien = V.MaNVBanve
LEFT JOIN ChucVu CV ON NV.MaChucVu = CV.MaChucVu
WHERE NV.HoTen LIKE 'Trần%'
  AND HD.ThoiGianThanhToan >= '2024-11-01'
  AND HD.ThoiGianThanhToan < '2024-12-01'
GROUP BY NV.MaNhanVien, NV.HoTen, NV.NgaySinh, NV.Sdt, NV.Email, CV.TenChucVu
HAVING DoanhSoVe >= 200000
ORDER BY DoanhSoVe DESC;

-- Q8. Khách hàng > 20 tuổi, tổng hóa đơn tháng 11/2024 > 200000
SELECT
    K.*,
    TIMESTAMPDIFF(YEAR, K.NgaySinh, '2024-11-30') AS TuoiTaiNgayBaoCao,
    SUM(HD.ThanhTien) AS TongHoaDon
FROM HoaDon HD
JOIN KhachHang K ON HD.MaKhachHang = K.MaKhachHang
WHERE HD.ThoiGianThanhToan >= '2024-11-01'
  AND HD.ThoiGianThanhToan < '2024-12-01'
  AND TIMESTAMPDIFF(YEAR, K.NgaySinh, '2024-11-30') > 20
GROUP BY K.MaKhachHang, K.HoTen, K.NgaySinh, K.Sdt, K.Email
HAVING TongHoaDon > 200000
ORDER BY TongHoaDon DESC;

-- Q9. Khách hàng mua vé cả tháng 11 và tháng 12/2024
SELECT
    KH.*,
    COUNT(CASE
        WHEN MONTH(HD.ThoiGianThanhToan) = 11 THEN V.MaVe
    END) AS SoVeThang11,
    COUNT(CASE
        WHEN MONTH(HD.ThoiGianThanhToan) = 12 THEN V.MaVe
    END) AS SoVeThang12
FROM KhachHang KH
JOIN HoaDon HD ON KH.MaKhachHang = HD.MaKhachHang
JOIN Ve V ON HD.MaHoaDon = V.MaHoaDon
WHERE HD.ThoiGianThanhToan >= '2024-11-01'
  AND HD.ThoiGianThanhToan < '2025-01-01'
GROUP BY KH.MaKhachHang, KH.HoTen, KH.NgaySinh, KH.Sdt, KH.Email
HAVING COUNT(DISTINCT MONTH(HD.ThoiGianThanhToan)) = 2;

-- Q10. Khách hàng xem Avengers: Endgame nhưng chưa xem Us
SELECT DISTINCT K.*
FROM KhachHang K
JOIN HoaDon HD ON K.MaKhachHang = HD.MaKhachHang
JOIN Ve V ON HD.MaHoaDon = V.MaHoaDon
JOIN SuatChieu SC ON V.MaSuatChieu = SC.MaSuatChieu
JOIN Phim P ON SC.MaPhim = P.MaPhim
WHERE P.TenPhim = 'Avengers: Endgame'
  AND K.MaKhachHang NOT IN (
      SELECT DISTINCT K2.MaKhachHang
      FROM KhachHang K2
      JOIN HoaDon HD2 ON K2.MaKhachHang = HD2.MaKhachHang
      JOIN Ve V2 ON HD2.MaHoaDon = V2.MaHoaDon
      JOIN SuatChieu SC2 ON V2.MaSuatChieu = SC2.MaSuatChieu
      JOIN Phim P2 ON SC2.MaPhim = P2.MaPhim
      WHERE P2.TenPhim = 'Us'
  );

-- Q11. Phim doanh số vé cao nhất trong thể loại Hành động 30/11 - 01/12
SELECT
    TL.TheLoai,
    P.MaPhim,
    P.TenPhim,
    SUM(V.Gia) AS TongTienVe,
    COUNT(V.MaVe) AS SoVeBan
FROM TheLoaiPhim TL
JOIN Phim P ON TL.MaPhim = P.MaPhim
JOIN SuatChieu SC ON SC.MaPhim = P.MaPhim
JOIN Ve V ON V.MaSuatChieu = SC.MaSuatChieu
WHERE TL.TheLoai = 'Hành động'
  AND SC.NgayChieu >= '2024-11-30'
  AND SC.NgayChieu <= '2024-12-01'
GROUP BY TL.TheLoai, P.MaPhim, P.TenPhim
ORDER BY TongTienVe DESC
LIMIT 1;

-- Q12. Phim có thanh toán chuyển khoản > tiền mặt 25/11 - 30/11
SELECT
    P.MaPhim,
    P.TenPhim,
    SUM(CASE
        WHEN HD.HinhThucThanhToan = 'Tiền mặt' THEN V.Gia ELSE 0
    END) AS ThanhToanTienMat,
    SUM(CASE
        WHEN HD.HinhThucThanhToan = 'Chuyển khoản' THEN V.Gia ELSE 0
    END) AS ThanhToanChuyenKhoan
FROM Phim P
JOIN SuatChieu SC ON P.MaPhim = SC.MaPhim
JOIN Ve V ON SC.MaSuatChieu = V.MaSuatChieu
JOIN HoaDon HD ON V.MaHoaDon = HD.MaHoaDon
WHERE HD.ThoiGianThanhToan >= '2024-11-25 00:00:00'
  AND HD.ThoiGianThanhToan < '2024-12-01 00:00:00'
GROUP BY P.MaPhim, P.TenPhim
HAVING ThanhToanChuyenKhoan > ThanhToanTienMat;

-- Q13. Sản phẩm bán chạy nhất trên hóa đơn có vé của suất chiếu sáng (trước 12h).
-- BaoGom liên kết sản phẩm với hóa đơn, không trực tiếp với suất chiếu;
-- EXISTS tránh nhân số lượng khi một hóa đơn có nhiều suất chiếu buổi sáng.
SELECT
    SP.*,
    SUM(BG.SoLuong) AS TongSoLuongBan,
    SUM(BG.SoLuong * SP.Gia) AS TongDoanhThu
FROM SanPham SP
JOIN BaoGom BG ON SP.MaSanPham = BG.MaSanPham
JOIN HoaDon HD ON BG.MaHoaDon = HD.MaHoaDon
WHERE EXISTS (
    SELECT 1
    FROM Ve V
    JOIN SuatChieu SC ON SC.MaSuatChieu = V.MaSuatChieu
    WHERE V.MaHoaDon = HD.MaHoaDon
      AND SC.GioBatDau < '12:00:00'
      AND SC.NgayChieu >= '2024-11-25'
      AND SC.NgayChieu <= '2024-12-01'
)
GROUP BY SP.MaSanPham, SP.TenSanPham, SP.Gia
ORDER BY TongSoLuongBan DESC
LIMIT 1;

-- ============================================================
-- 4. QUICK CHECKS
-- ============================================================
SELECT 'Phim' AS Bang, COUNT(*) AS SoDong FROM Phim
UNION ALL SELECT 'Phong', COUNT(*) FROM Phong
UNION ALL SELECT 'Ghe', COUNT(*) FROM Ghe
UNION ALL SELECT 'SanPham', COUNT(*) FROM SanPham
UNION ALL SELECT 'KhachHang', COUNT(*) FROM KhachHang
UNION ALL SELECT 'NhanVien', COUNT(*) FROM NhanVien
UNION ALL SELECT 'HoaDon', COUNT(*) FROM HoaDon
UNION ALL SELECT 'SuatChieu', COUNT(*) FROM SuatChieu
UNION ALL SELECT 'Ve', COUNT(*) FROM Ve;
