-- Chạy trên DB CI cô lập: mô phỏng schema cũ và kiểm tra migration chạy lặp an toàn.
BEGIN;
ALTER TABLE vi_pham ALTER COLUMN hinhthucxuly TYPE VARCHAR(255);
\ir ../migrations/V20261013__discipline_decision_text.sql
\ir ../migrations/V20261013__discipline_decision_text.sql
UPDATE vi_pham SET hinhthucxuly='REVIEW: ' || repeat('R',1000)
WHERE mavipham=(SELECT min(mavipham) FROM vi_pham);
DO $$
BEGIN
  IF NOT EXISTS(SELECT 1 FROM vi_pham WHERE length(hinhthucxuly)=1008) THEN
    RAISE EXCEPTION 'Decision storage must preserve the full review reason';
  END IF;
END $$;
ROLLBACK;
