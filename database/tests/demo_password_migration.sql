BEGIN;
UPDATE tai_khoan SET matkhauhash='$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m' WHERE tendangnhap IN ('sv041','sv042');
-- A separately changed password must remain unchanged.
UPDATE tai_khoan SET matkhauhash='custom-password-fixture' WHERE tendangnhap='sv043';
\ir ../migrations/V20261008__repair_legacy_demo_passwords.sql
\ir ../migrations/V20261008__repair_legacy_demo_passwords.sql
DO $$ BEGIN
  IF (SELECT count(*) FROM tai_khoan WHERE tendangnhap IN ('sv041','sv042')
      AND matkhauhash='$2a$10$bzSt4ZQZn5e6T.8sC8FwWemE5XHWv/tCsZEPv2vXgWICD.wy6ug0i') <> 2 THEN RAISE EXCEPTION 'Demo passwords not restored'; END IF;
  IF NOT EXISTS (SELECT 1 FROM tai_khoan WHERE tendangnhap='sv043' AND matkhauhash='custom-password-fixture')
    THEN RAISE EXCEPTION 'Changed password overwritten'; END IF;
END $$;
ROLLBACK;
