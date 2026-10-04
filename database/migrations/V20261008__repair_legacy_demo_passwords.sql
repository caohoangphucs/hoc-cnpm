-- Restore the documented demo password only for students with the known original seed hash.
-- Do not change authenticator keys or passwords already changed from that legacy value.
UPDATE tai_khoan
SET matkhauhash = '$2a$10$bzSt4ZQZn5e6T.8sC8FwWemE5XHWv/tCsZEPv2vXgWICD.wy6ug0i'
WHERE vaitro = 'SINHVIEN'
  AND tendangnhap ~ '^sv[0-9]{3}$'
  AND tendangnhap BETWEEN 'sv002' AND 'sv055'
  AND md5(matkhauhash) = '69a5516555b1c42b4898af7305e696d7';
