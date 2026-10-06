-- Giữ contract quyết định/lý do 1.000 ký tự và tiền tố REVIEW do service thêm.
-- Chỉ mở rộng cột; không reset hồ sơ hoặc lịch sử xử lý.
ALTER TABLE vi_pham ALTER COLUMN hinhthucxuly TYPE TEXT;
