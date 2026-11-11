USE QuanLyRapChieuPhim;

ALTER TABLE HoaDon
    ADD COLUMN TrangThaiThanhToan VARCHAR(20) NOT NULL DEFAULT 'PAID' AFTER HinhThucThanhToan,
    ADD COLUMN TaoLuc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER TrangThaiThanhToan,
    ADD COLUMN MaThamChieuChuyenKhoan VARCHAR(20) NULL AFTER TrangThaiThanhToan,
    ADD COLUMN HetHanThanhToan DATETIME NULL AFTER ThoiGianThanhToan,
    ADD COLUMN XacNhanBoi CHAR(36) NULL AFTER HetHanThanhToan,
    ADD COLUMN XacNhanLuc DATETIME NULL AFTER XacNhanBoi,
    ADD UNIQUE KEY UQ_HoaDon_MaThamChieu (MaThamChieuChuyenKhoan),
    ADD KEY IX_HoaDon_TrangThai_ThanhToan (TrangThaiThanhToan, HetHanThanhToan),
    ADD CONSTRAINT CK_HoaDon_TrangThaiThanhToan
        CHECK (TrangThaiThanhToan IN ('PENDING', 'PAID', 'EXPIRED', 'CANCELLED')),
    ADD CONSTRAINT FK_HoaDon_XacNhanBoi
        FOREIGN KEY (XacNhanBoi) REFERENCES TaiKhoan(MaTaiKhoan) ON DELETE SET NULL;

CREATE TABLE ChiTietGheHoaDon (
    MaHoaDon VARCHAR(50) NOT NULL,
    MaSuatChieu VARCHAR(50) NOT NULL,
    MaGhe VARCHAR(50) NOT NULL,
    MaPhong VARCHAR(50) NOT NULL,
    Gia INT NOT NULL,
    PRIMARY KEY (MaHoaDon, MaSuatChieu, MaGhe, MaPhong),
    CONSTRAINT FK_ChiTietGheHoaDon_HoaDon FOREIGN KEY (MaHoaDon)
        REFERENCES HoaDon(MaHoaDon) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_ChiTietGheHoaDon_SuatChieu FOREIGN KEY (MaSuatChieu, MaPhong)
        REFERENCES SuatChieu(MaSuatChieu, MaPhong) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT FK_ChiTietGheHoaDon_Ghe FOREIGN KEY (MaGhe, MaPhong)
        REFERENCES Ghe(MaGhe, MaPhong) ON DELETE RESTRICT ON UPDATE CASCADE,
    CHECK (Gia >= 0)
) ENGINE=InnoDB;

CREATE TABLE GheGiuThanhToan (
    MaSuatChieu VARCHAR(50) NOT NULL,
    MaGhe VARCHAR(50) NOT NULL,
    MaPhong VARCHAR(50) NOT NULL,
    MaHoaDon VARCHAR(50) NOT NULL,
    HetHanLuc DATETIME NOT NULL,
    PRIMARY KEY (MaSuatChieu, MaGhe, MaPhong),
    UNIQUE KEY UQ_GheGiuThanhToan_HoaDonGhe (MaHoaDon, MaGhe),
    KEY IX_GheGiuThanhToan_HetHan (HetHanLuc),
    CONSTRAINT FK_GheGiuThanhToan_ChiTiet FOREIGN KEY (MaHoaDon, MaSuatChieu, MaGhe, MaPhong)
        REFERENCES ChiTietGheHoaDon(MaHoaDon, MaSuatChieu, MaGhe, MaPhong)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

INSERT INTO ChiTietGheHoaDon (MaHoaDon, MaSuatChieu, MaGhe, MaPhong, Gia)
SELECT V.MaHoaDon, V.MaSuatChieu, V.MaGhe, V.MaPhong, V.Gia
FROM Ve V;
