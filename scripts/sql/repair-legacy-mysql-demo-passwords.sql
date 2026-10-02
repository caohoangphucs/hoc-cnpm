-- Manual repair of known bad seed hashes for the five documented demo accounts.
-- Apply only after backup. Accounts with user-changed hashes are preserved.
USE cnpm_db;
UPDATE TAI_KHOAN
SET MatKhauHash = '$2b$10$bzSt4ZQZn5e6T.8sC8FwWemE5XHWv/tCsZEPv2vXgWICD.wy6ug0i'
WHERE TenDangNhap IN ('admin', 'sv001', 'ketoan1', 'kythuat1', 'baove1')
  AND MatKhauHash IN ('$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m');
