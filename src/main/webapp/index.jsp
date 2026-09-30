<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hệ Thống Quản Lý Ký Túc Xá (KTX) - CNPM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .hero-banner {
            background: linear-gradient(135deg, #1e3a8a 0%, #0284c7 100%);
            color: white;
            padding: 70px 0;
            border-radius: 0 0 30px 30px;
        }
        .login-card {
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
        }
        .feature-card {
            transition: transform 0.2s;
            border-radius: 12px;
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
    </style>
</head>
<body class="bg-light">

<div class="hero-banner text-center mb-5">
    <div class="container">
        <h1 class="display-5 fw-bold mb-3"><i class="fa-solid fa-hotel me-2"></i>HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ</h1>
        <p class="lead mb-4">Giải pháp quản lý ký túc xá thông minh, tích hợp sơ đồ phòng, điện nước, báo hỏng & thanh toán VNPAY.</p>
        <div class="badge bg-warning text-dark px-3 py-2 fs-6">
            <i class="fa-solid fa-code me-1"></i> Kiến trúc 3 Tầng • 8 Use Cases • Multi-Role CSDL
        </div>
    </div>
</div>

<div class="container mb-5">
    <div class="row justify-content-center">
        <div class="col-lg-5 col-md-8">
            <div class="card login-card p-4 border-0 mb-4">
                <h4 class="fw-bold text-center mb-3"><i class="fa-solid fa-lock me-2 text-primary"></i>Đăng Nhập Hệ Thống</h4>
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger py-2 small" role="alert">
                        <i class="fa-solid fa-circle-exclamation me-1"></i> ${error}
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/auth/login" method="POST">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Tên đăng nhập</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fa-solid fa-user"></i></span>
                            <input type="text" class="form-control" name="username" id="inputUsername" placeholder="Nhập username..." required>
                        </div>
                    </div>
                    <div class="mb-4">
                        <label class="form-label small fw-bold">Mật khẩu</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fa-solid fa-key"></i></span>
                            <input type="password" class="form-control" name="password" id="inputPassword" placeholder="Nhập mật khẩu..." required value="123456">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 py-2 fw-bold shadow-sm">
                        <i class="fa-solid fa-arrow-right-to-bracket me-2"></i> Đăng Nhập
                    </button>
                </form>

                <hr class="my-4">

                <div class="text-center">
                    <p class="text-muted small mb-2 fw-bold">Chọn nhanh tài khoản Demo (5 Vai trò):</p>
                    <div class="d-flex flex-wrap gap-2 justify-content-center">
                        <button class="btn btn-outline-danger btn-sm" onclick="quickLogin('admin', '123456')">
                            <i class="fa-solid fa-user-shield me-1"></i> Admin
                        </button>
                        <button class="btn btn-outline-success btn-sm" onclick="quickLogin('sv001', '123456')">
                            <i class="fa-solid fa-graduation-cap me-1"></i> Sinh Viên
                        </button>
                        <button class="btn btn-outline-primary btn-sm" onclick="quickLogin('ketoan1', '123456')">
                            <i class="fa-solid fa-calculator me-1"></i> Kế Toán
                        </button>
                        <button class="btn btn-outline-warning btn-sm text-dark" onclick="quickLogin('kythuat1', '123456')">
                            <i class="fa-solid fa-wrench me-1"></i> Kỹ Thuật
                        </button>
                        <button class="btn btn-outline-dark btn-sm" onclick="quickLogin('baove1', '123456')">
                            <i class="fa-solid fa-shield-halved me-1"></i> Bảo Vệ
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <h4 class="fw-bold mb-3"><i class="fa-solid fa-layer-group me-2 text-primary"></i>Phân Hệ Nghiệp Vụ Chính (8 UC)</h4>
            <div class="row g-3">
                <div class="col-md-6">
                    <div class="card p-3 border-0 feature-card h-100 shadow-sm">
                        <div class="text-primary fs-3 mb-2"><i class="fa-solid fa-bed"></i></div>
                        <h6 class="fw-bold">UC-02 & UC-03: Sơ Đồ Phòng & Đặt Giường</h6>
                        <p class="text-muted small">Khóa chống tranh chấp giường (UPDLOCK), hiển thị giường trống theo tòa và thời gian thực.</p>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card p-3 border-0 feature-card h-100 shadow-sm">
                        <div class="text-success fs-3 mb-2"><i class="fa-solid fa-id-card-clip"></i></div>
                        <h6 class="fw-bold">UC-04: Duyệt Đăng Ký & Gán Giường</h6>
                        <p class="text-muted small">Quy trình duyệt đăng ký, trigger tự động ghi lưu trú và chuyển trạng thái phòng/giường.</p>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card p-3 border-0 feature-card h-100 shadow-sm">
                        <div class="text-warning fs-3 mb-2"><i class="fa-solid fa-bolt"></i></div>
                        <h6 class="fw-bold">UC-05 & UC-06: Điện Nước & Hóa Đơn</h6>
                        <p class="text-muted small">Ghi chỉ số theo kỳ, phát hành hóa đơn theo phòng và kiểm tra toàn vẹn công thức tính tiền.</p>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card p-3 border-0 feature-card h-100 shadow-sm">
                        <div class="text-info fs-3 mb-2"><i class="fa-solid fa-credit-card"></i></div>
                        <h6 class="fw-bold">UC-07 & UC-08: Thanh Toán & Thống Kê</h6>
                        <p class="text-muted small">Cổng thanh toán trực tuyến VNPAY/Tiền mặt, biểu đồ tỷ lệ lấp đầy và doanh thu.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="footer py-3 bg-white border-top text-center text-muted small">
    <div class="container">
        <span>© 2026 Hệ Thống Quản Lý Ký Túc Xá - Đồ Án CNPM 3 Tầng Chuẩn Hóa.</span>
    </div>
</footer>

<script>
function quickLogin(u, p) {
    document.getElementById('inputUsername').value = u;
    document.getElementById('inputPassword').value = p;
}
</script>
</body>
</html>