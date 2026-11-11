USE QuanLyRapChieuPhim;

ALTER TABLE TaiKhoan
    MODIFY Email VARCHAR(254) NULL,
    ADD COLUMN SoDienThoaiDangNhap VARCHAR(20) NULL AFTER Email,
    ADD UNIQUE KEY UQ_TaiKhoan_SoDienThoaiDangNhap (SoDienThoaiDangNhap);

UPDATE TaiKhoan A
JOIN KhachHang K ON K.MaKhachHang = A.MaKhachHang
SET A.SoDienThoaiDangNhap = REGEXP_REPLACE(TRIM(K.Sdt), '[^0-9+]', '')
WHERE A.VaiTro = 'CUSTOMER'
  AND K.Sdt IS NOT NULL
  AND REGEXP_REPLACE(TRIM(K.Sdt), '[^0-9+]', '') REGEXP '^[+]?[0-9]{7,15}$'
  AND (
      SELECT COUNT(*)
      FROM KhachHang K2
      WHERE REGEXP_REPLACE(TRIM(K2.Sdt), '[^0-9+]', '')
          = REGEXP_REPLACE(TRIM(K.Sdt), '[^0-9+]', '')
  ) = 1;

UPDATE TaiKhoan
SET EmailDaXacMinh = TRUE
WHERE VaiTro = 'CUSTOMER';

DELETE T
FROM TokenTaiKhoan T
JOIN TaiKhoan A ON A.MaTaiKhoan = T.MaTaiKhoan
WHERE A.VaiTro = 'CUSTOMER'
  AND T.MucDich = 'VERIFY_EMAIL';
