# ==============================================================================
# SCRIPT TẠO NHÁNH TỰ ĐỘNG CHUẨN QUY TẮC NHÓM
# Cách dùng:
#   .\scripts\git-branch.ps1 -Type feat -Author dat -Feature vnpay
#   .\scripts\git-branch.ps1 -Type fix -Author dat -Feature invoice-calc
# ==============================================================================

param(
    [ValidateSet("feat", "fix", "refactor", "style", "docs", "test")]
    [string]$Type = "feat",

    [Parameter(Mandatory=$true, HelpMessage="Tên của bạn (viết liền không dấu, ví dụ: dat, linh, gam, khoa, phuc)")]
    [string]$Author,

    [Parameter(Mandatory=$true, HelpMessage="Tên chức năng (dùng gạch ngang, ví dụ: vnpay, room-booking, auth-login)")]
    [string]$Feature
)

# Chuẩn hóa tên (chữ thường, bỏ dấu cách và gạch dưới)
$cleanAuthor = $Author.ToLower().Trim() -replace "[^a-z0-9]", ""
$cleanFeature = $Feature.ToLower().Trim() -replace "[ _]", "-" -replace "[^a-z0-9-]", ""

$branchName = "$Type/$cleanAuthor-$cleanFeature"

Write-Host "`n🚀 Đang tạo nhánh làm việc mới: $branchName" -ForegroundColor Cyan

# Kiểm tra git status trước
$status = git status --porcelain
if ($status) {
    Write-Host "⚠️  Cảnh báo: Bạn đang có file chưa commit!" -ForegroundColor Yellow
    Write-Host "👉 Hãy stash hoặc commit trước khi chuyển nhánh." -ForegroundColor Yellow
    $confirm = Read-Host "Bạn có muốn tiếp tục tạo và chuyển nhánh không? (y/n)"
    if ($confirm -ne 'y') {
        exit 0
    }
}

# Tạo và chuyển nhánh
git checkout -b $branchName

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ ĐÃ TẠO VÀ CHUYỂN SANG NHÁNH: $branchName" -ForegroundColor Green
    Write-Host "💡 Hãy code chức năng của bạn. Khi commit dùng: git commit -m `"$Type($cleanFeature): ...`"" -ForegroundColor Yellow
} else {
    Write-Host "❌ Không thể tạo nhánh. Vui lòng kiểm tra lại trạng thái Git!" -ForegroundColor Red
}
