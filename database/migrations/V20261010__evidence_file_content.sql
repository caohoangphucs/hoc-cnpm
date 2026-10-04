-- Encrypted image content shares the same persistent database volume as its metadata.
CREATE TABLE IF NOT EXISTS svc_file_content (
    file_id uuid PRIMARY KEY REFERENCES svc_file(id) ON DELETE CASCADE,
    encrypted_bytes bytea NOT NULL,
    CHECK (octet_length(encrypted_bytes) BETWEEN 17 AND 5242896)
);
