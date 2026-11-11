USE QuanLyRapChieuPhim;

CREATE TABLE TaiKhoan (
    MaTaiKhoan CHAR(36) NOT NULL,
    Email VARCHAR(254) NOT NULL,
    MatKhauHash VARCHAR(100) NULL,
    VaiTro VARCHAR(20) NOT NULL,
    MaKhachHang VARCHAR(50) NULL,
    MaNhanVien VARCHAR(50) NULL,
    EmailDaXacMinh BOOLEAN NOT NULL DEFAULT FALSE,
    SoLanDangNhapSai TINYINT UNSIGNED NOT NULL DEFAULT 0,
    BiKhoa BOOLEAN NOT NULL DEFAULT FALSE,
    NgayTao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MaTaiKhoan),
    UNIQUE KEY UQ_TaiKhoan_Email (Email),
    UNIQUE KEY UQ_TaiKhoan_KhachHang (MaKhachHang),
    UNIQUE KEY UQ_TaiKhoan_NhanVien (MaNhanVien),
    KEY IX_TaiKhoan_Khoa (BiKhoa, NgayTao),
    CONSTRAINT CK_TaiKhoan_VaiTro CHECK (VaiTro IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    CONSTRAINT CK_TaiKhoan_LienKet CHECK (
        (VaiTro = 'CUSTOMER' AND MaKhachHang IS NOT NULL AND MaNhanVien IS NULL)
        OR (VaiTro = 'STAFF' AND MaNhanVien IS NOT NULL AND MaKhachHang IS NULL)
        OR (VaiTro = 'ADMIN' AND MaKhachHang IS NULL AND MaNhanVien IS NULL)
    ),
    CONSTRAINT FK_TaiKhoan_KhachHang FOREIGN KEY (MaKhachHang)
        REFERENCES KhachHang(MaKhachHang) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (MaNhanVien)
        REFERENCES NhanVien(MaNhanVien) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE TokenTaiKhoan (
    MaToken CHAR(64) NOT NULL,
    MaTaiKhoan CHAR(36) NOT NULL,
    MucDich VARCHAR(20) NOT NULL,
    HetHanLuc DATETIME NOT NULL,
    TaoLuc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MaToken),
    KEY IX_TokenTaiKhoan_TaiKhoan (MaTaiKhoan, MucDich),
    CONSTRAINT CK_TokenTaiKhoan_MucDich CHECK (MucDich IN ('VERIFY_EMAIL', 'SET_PASSWORD')),
    CONSTRAINT FK_TokenTaiKhoan_TaiKhoan FOREIGN KEY (MaTaiKhoan)
        REFERENCES TaiKhoan(MaTaiKhoan) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE NhatKyTaiKhoan (
    MaNhatKy BIGINT NOT NULL AUTO_INCREMENT,
    MaTaiKhoan CHAR(36) NOT NULL,
    HanhDong VARCHAR(30) NOT NULL,
    NguoiThucHien CHAR(36) NULL,
    ThoiGian TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MaNhatKy),
    KEY IX_NhatKyTaiKhoan_TaiKhoan (MaTaiKhoan, ThoiGian),
    CONSTRAINT FK_NhatKyTaiKhoan_TaiKhoan FOREIGN KEY (MaTaiKhoan)
        REFERENCES TaiKhoan(MaTaiKhoan) ON DELETE CASCADE,
    CONSTRAINT FK_NhatKyTaiKhoan_NguoiThucHien FOREIGN KEY (NguoiThucHien)
        REFERENCES TaiKhoan(MaTaiKhoan) ON DELETE SET NULL
) ENGINE=InnoDB;
