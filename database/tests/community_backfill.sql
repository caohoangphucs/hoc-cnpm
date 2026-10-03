-- Disposable CI database only. Simulate the missing supplemental seed without persisting changes.
BEGIN;
DELETE FROM so_diem_cong_ich WHERE madangkycongich IN
  (SELECT madangkycongich FROM dang_ky_cong_ich WHERE mahoatdong IN ('ACT-01','ACT-02','ACT-03'));
DELETE FROM dang_ky_cong_ich WHERE mahoatdong IN ('ACT-01','ACT-02','ACT-03');
DELETE FROM hoat_dong WHERE mahoatdong IN ('ACT-01','ACT-02','ACT-03');
\ir ../migrations/V20261007__backfill_demo_community_activities.sql
DO $$ BEGIN
  IF (SELECT count(*) FROM hoat_dong WHERE mahoatdong IN ('ACT-01','ACT-02','ACT-03')
      AND trangthai='MO_DANG_KY' AND dadangky=0) <> 3 THEN
    RAISE EXCEPTION 'Missing activities not restored';
  END IF;
END $$;
-- Re-running a deployment must not overwrite a changed activity or invent registrations.
UPDATE hoat_dong SET tieude='Hoạt động đã chỉnh sửa',trangthai='DONG_DANG_KY' WHERE mahoatdong='ACT-01';
\ir ../migrations/V20261007__backfill_demo_community_activities.sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM hoat_dong WHERE mahoatdong='ACT-01'
      AND tieude='Hoạt động đã chỉnh sửa' AND trangthai='DONG_DANG_KY') THEN
    RAISE EXCEPTION 'Existing activity overwritten';
  END IF;
  IF (SELECT count(*) FROM hoat_dong WHERE mahoatdong IN ('ACT-01','ACT-02','ACT-03')) <> 3 THEN
    RAISE EXCEPTION 'Duplicate activities';
  END IF;
END $$;
ROLLBACK;
