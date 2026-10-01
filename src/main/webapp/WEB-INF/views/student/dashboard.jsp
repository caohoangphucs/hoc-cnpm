<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="row g-4 mb-4">
    <div class="col-md-4">
        <div class="card p-4 h-100 bg-white shadow-sm">
            <h5 class="fw-bold text-primary mb-3"><i class="fa-solid fa-address-card me-2"></i>Thông Tin Sinh Viên</h5>
            <div class="mb-2"><strong>Họ và tên:</strong> ${sessionScope.fullName}</div>
            <div class="mb-2"><strong>Mã số SV:</strong> ${sessionScope.studentId}</div>
            <div class="mb-2"><strong>Phòng hiện tại:</strong> 
                <c:choose>
                    <c:when test="${not empty sessionScope.roomId}">
                        <span class="badge bg-success fs-6">${sessionScope.roomId}</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge bg-secondary">Chưa có phòng</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <hr>
            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/student/rooms" class="btn btn-outline-primary btn-sm">
                    <i class="fa-solid fa-bed me-1"></i> Xem danh sách phòng & Đặt giường
                </a>
                <a href="${pageContext.request.contextPath}/student/invoices" class="btn btn-outline-success btn-sm">
                    <i class="fa-solid fa-file-invoice-dollar me-1"></i> Xem hóa đơn phòng
                </a>
            </div>
        </div>
    </div>

    <div class="col-md-8">
        <div class="card p-4 h-100 bg-white shadow-sm">
            <h5 class="fw-bold text-danger mb-3"><i class="fa-solid fa-triangle-exclamation me-2"></i>Gửi Yêu Cầu Báo Hỏng / Sửa Chữa (Kỹ Thuật)</h5>
            <form action="${pageContext.request.contextPath}/student/repair-request" method="POST">
                <div class="row g-3">
                    <div class="col-md-8">
                        <label class="form-label small fw-bold">Tiêu đề báo hỏng</label>
                        <input type="text" class="form-control" name="title" placeholder="VD: Hỏng bóng đèn, kẹt ổ khóa, rò rỉ nước..." required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Mức độ ưu tiên</label>
                        <select class="form-select" name="priority">
                            <option value="THAP">Thấp</option>
                            <option value="TRUNG_BINH" selected>Trung bình</option>
                            <option value="CAO">Cao</option>
                            <option value="KHAN_CAP">Khẩn cấp</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label small fw-bold">Mô tả chi tiết</label>
                        <textarea class="form-control" name="description" rows="3" placeholder="Mô tả cụ thể vị trí và tình trạng thiết bị hư hỏng..." required></textarea>
                    </div>
                    <div class="col-12 text-end">
                        <button type="submit" class="btn btn-danger px-4">
                            <i class="fa-solid fa-paper-plane me-1"></i> Gửi Yêu Cầu
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
