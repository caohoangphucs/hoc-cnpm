-- Run once on an existing database before deploying authenticator support.
ALTER TABLE TAI_KHOAN ADD COLUMN IF NOT EXISTS BiMatTOTP TEXT;
