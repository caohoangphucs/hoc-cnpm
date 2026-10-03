-- Run on the disposable CI database. Roll back every deliberately corrupted value.
BEGIN;
UPDATE chi_tiet_hoa_don SET tenmuc = convert_from(convert_to(tenmuc, 'UTF8'), 'LATIN1');
UPDATE yeu_cau_sua_chua SET tieude = convert_from(convert_to(tieude, 'UTF8'), 'LATIN1');
\ir ../migrations/V20261006__repair_vietnamese_display_data.sql
DO $$
DECLARE
  original text := 'Điện, nước, Phòng Tiêu Chuẩn, Nguyễn, sửa chữa';
  broken text;
BEGIN
  broken := convert_from(convert_to(original, 'UTF8'), 'LATIN1');
  IF pg_temp.repair_vietnamese(broken) <> original THEN RAISE EXCEPTION 'Latin-1 repair failed'; END IF;
  -- Windows-1252 punctuation mixed with Latin-1 control characters, as found in production.
  broken := replace(broken, chr(135), '‡');
  IF pg_temp.repair_vietnamese(broken) <> original THEN RAISE EXCEPTION 'Mixed encoding repair failed'; END IF;
  broken := convert_from(convert_to(broken, 'UTF8'), 'LATIN1');
  IF pg_temp.repair_vietnamese(broken) <> original THEN RAISE EXCEPTION 'Repeated encoding repair failed'; END IF;
  IF pg_temp.repair_vietnamese(original) <> original THEN RAISE EXCEPTION 'Valid Vietnamese changed'; END IF;
  IF pg_temp.repair_vietnamese('') <> '' OR pg_temp.repair_vietnamese(NULL) IS NOT NULL THEN RAISE EXCEPTION 'Empty boundary failed'; END IF;
  IF pg_temp.repair_vietnamese('Ã') <> 'Ã' THEN RAISE EXCEPTION 'Non-reversible input changed'; END IF;
  IF EXISTS (SELECT 1 FROM chi_tiet_hoa_don WHERE tenmuc ~ '[ÃÆÄ]|á[º»]') THEN RAISE EXCEPTION 'Invoice text still corrupted'; END IF;
  IF EXISTS (SELECT 1 FROM yeu_cau_sua_chua WHERE tieude ~ '[ÃÆÄ]|á[º»]') THEN RAISE EXCEPTION 'Repair text still corrupted'; END IF;
END $$;
\ir ../migrations/V20261006__repair_vietnamese_display_data.sql
ROLLBACK;
