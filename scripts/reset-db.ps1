# ==============================================================================
# SCRIPT 1-CLICK RESET & RE-SEED DATABASE (KTX SMART HUB)
# Cách dùng: Mở PowerShell và gõ: .\scripts\reset-db.ps1
# ==============================================================================

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "🔄 DỌN SẠCH VÀ TÁI NẠP DỮ LIỆU CƠ SỞ DỮ LIỆU (CNPM_DB)..." -ForegroundColor Yellow
Write-Host "======================================================" -ForegroundColor Cyan

# 1. Kiểm tra container cnpm-mysql có đang chạy không
$containerStatus = docker ps --filter "name=cnpm-mysql" --format "{{.Status}}"
if (-not $containerStatus) {
    Write-Host "⚠️  Container 'cnpm-mysql' chưa khởi động!" -ForegroundColor Red
    Write-Host "👉 Đang thử khởi động container database..." -ForegroundColor Yellow
    docker compose up -d cnpm-db
    Start-Sleep -Seconds 5
}

# 2. Áp dụng Table Schema (01_schema.sql)
Write-Host "`n[1/3] Đang nạp Table Schema (database/01_schema.sql)..." -ForegroundColor Green
Get-Content -Raw "database/01_schema.sql" | docker exec -i cnpm-mysql mysql -u root -pcnpmrootpassword123 cnpm_db
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Lỗi khi nạp 01_schema.sql!" -ForegroundColor Red
    exit 1
}

# 3. Nạp Seed Data (02_seed.sql)
Write-Host "[2/3] Đang nạp Dữ liệu mẫu (database/02_seed.sql)..." -ForegroundColor Green
Get-Content -Raw "database/02_seed.sql" | docker exec -i cnpm-mysql mysql -u root -pcnpmrootpassword123 cnpm_db
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Lỗi khi nạp 02_seed.sql!" -ForegroundColor Red
    exit 1
}

# 4. Kiểm tra tổng số bản ghi
Write-Host "[3/3] Xác thực số lượng dữ liệu sau khi reset:" -ForegroundColor Green
docker exec -i cnpm-mysql mysql -u root -pcnpmrootpassword123 cnpm_db -e "
    SELECT 'SINH_VIEN' AS Bang, COUNT(*) AS SoLuong FROM SINH_VIEN
    UNION ALL
    SELECT 'PHONG', COUNT(*) FROM PHONG
    UNION ALL
    SELECT 'GIUONG', COUNT(*) FROM GIUONG
    UNION ALL
    SELECT 'HOA_DON', COUNT(*) FROM HOA_DON
    UNION ALL
    SELECT 'VAT_TU', COUNT(*) FROM VAT_TU
    UNION ALL
    SELECT 'RA_VAO', COUNT(*) FROM RA_VAO;
"

Write-Host "`n✅ ĐÃ RESET VÀ TÁI TẠO CSDL THÀNH CÔNG!" -ForegroundColor Green
Write-Host "🎉 Bạn có thể tiếp tục kiểm thử hoặc test Use Cases." -ForegroundColor Cyan
