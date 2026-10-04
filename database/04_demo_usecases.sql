-- Additive demo cases after schema/base seed, including Docker entrypoint autocommit.
BEGIN;
\ir migrations/V20261009__complete_demo_usecases.sql
COMMIT;
