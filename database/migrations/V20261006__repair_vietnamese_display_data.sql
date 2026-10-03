SET client_encoding = 'UTF8';
-- Temporary migration helper: recover mixed Windows-1252/Latin-1 decoding.
-- Keep valid Vietnamese and stop when a string cannot be reversed losslessly.
CREATE OR REPLACE FUNCTION pg_temp.repair_vietnamese(value text)
RETURNS text LANGUAGE plpgsql AS $repair$
DECLARE
  repaired text := value;
  candidate text;
BEGIN
  IF value IS NULL THEN RETURN NULL; END IF;
  FOR attempt IN 1..3 LOOP
    IF repaired !~ '[ÃÂÆÄ]|á[º»]|[-]' THEN EXIT; END IF;
    BEGIN
      candidate := convert_from(convert_to(translate(repaired, '€‚ƒ„…†‡ˆ‰Š‹ŒŽ‘’“”•–—˜™š›œžŸ', ''), 'LATIN1'), 'UTF8');
    EXCEPTION WHEN character_not_in_repertoire OR untranslatable_character THEN
      EXIT;
    END;
    IF candidate = repaired THEN EXIT; END IF;
    repaired := candidate;
  END LOOP;
  RETURN repaired;
END;
$repair$;

-- Only display fields are repaired; identifiers, credentials and business states stay intact.
UPDATE chi_tiet_hoa_don
SET tenmuc = pg_temp.repair_vietnamese(tenmuc);

UPDATE don_gia_dich_vu
SET tendichvu = pg_temp.repair_vietnamese(tendichvu),
    donvitinh = pg_temp.repair_vietnamese(donvitinh);

UPDATE giuong
SET vitri = pg_temp.repair_vietnamese(vitri);

UPDATE loai_phong
SET tenloaiphong = pg_temp.repair_vietnamese(tenloaiphong),
    mota = pg_temp.repair_vietnamese(mota);

UPDATE ra_vao
SET ghichu = pg_temp.repair_vietnamese(ghichu);

UPDATE sinh_vien
SET hoten = pg_temp.repair_vietnamese(hoten),
    khoa = pg_temp.repair_vietnamese(khoa),
    quequan = pg_temp.repair_vietnamese(quequan);

UPDATE tai_khoan
SET hoten = pg_temp.repair_vietnamese(hoten);

UPDATE toa_nha
SET tentoa = pg_temp.repair_vietnamese(tentoa),
    ghichu = pg_temp.repair_vietnamese(ghichu);

UPDATE vat_tu
SET tenvattu = pg_temp.repair_vietnamese(tenvattu),
    donvitinh = pg_temp.repair_vietnamese(donvitinh),
    ghichu = pg_temp.repair_vietnamese(ghichu);

UPDATE vi_pham
SET noidungvipham = pg_temp.repair_vietnamese(noidungvipham),
    hinhthucxuly = pg_temp.repair_vietnamese(hinhthucxuly);

UPDATE yeu_cau_sua_chua
SET tieude = pg_temp.repair_vietnamese(tieude),
    motachitiet = pg_temp.repair_vietnamese(motachitiet),
    ghichukythuat = pg_temp.repair_vietnamese(ghichukythuat);

