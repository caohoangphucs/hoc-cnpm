-- Restore UTF-8 room type labels that were stored after UTF-8 bytes were read as Latin-1.
-- Limit the conversion to rows with the mojibake markers so valid Vietnamese remains unchanged.
UPDATE loai_phong
SET tenloaiphong = convert_from(convert_to(tenloaiphong, 'LATIN1'), 'UTF8')
WHERE tenloaiphong LIKE '%Ã%'
  AND maloaiphong IN ('LP-2', 'LP-4', 'LP-6');
