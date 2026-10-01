<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="row g-4 mb-4">
    <div class="col-md-6">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold text-primary mb-3"><i class="fa-solid fa-calculator me-2"></i>Ghi Chỉ Số Điện Nước (UC-05)</h5>
            <form action="${pageContext.request.contextPath}/accounting/meters" method="POST">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Chọn Phòng</label>
                        <select name="roomId" class="form-select" required>
                            <option value="A-101">Phòng A-101 (Tòa A)</option>
                            <option value="A-102">Phòng A-102 (Tòa A)</option>
                            <option value="B-201">Phòng B-201 (Tòa B)</option>
                            <option value="C-301">Phòng C-301 (Tòa C)</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Kỳ Tháng (YYYY-MM)</label>
                        <input type="month" name="month" class="form-control" value="2026-10" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Điện Cũ (kWh)</label>
                        <input type="number" name="oldE" class="form-control" value="185" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Điện Mới (kWh)</label>
                        <input type="number" name="newE" class="form-control" value="250" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Nước Cũ (m3)</label>
                        <input type="number" name="oldW" class="form-control" value="52" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Nước Mới (m3)</label>
                        <input type="number" name="newW" class="form-control" value="68" required>
                    </div>
                    <div class="col-12 text-end">
                        <button type="submit" class="btn btn-primary px-4"><i class="fa-solid fa-save me-1"></i> Lưu Chỉ Số</button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <div class="col-md-6">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold text-success mb-3"><i class="fa-solid fa-file-circle-plus me-2"></i>Phát Hành Hóa Đơn Tháng (UC-06)</h5>
            <form action="${pageContext.request.contextPath}/accounting/generate-invoice" method="POST">
                <div class="mb-3">
                    <label class="form-label small fw-bold">Chọn Phòng Cần Phát Hành</label>
                    <select name="roomId" class="form-select" required>
                        <option value="A-101">Phòng A-101 (Tòa A)</option>
                        <option value="B-201">Phòng B-201 (Tòa B)</option>
                        <option value="C-301">Phòng C-301 (Tòa C)</option>
                    </select>
                </div>
                <div class="mb-4">
                    <label class="form-label small fw-bold">Kỳ Hóa Đơn</label>
                    <input type="month" name="month" class="form-control" value="2026-10" required>
                </div>
                <div class="alert alert-secondary small">
                    <i class="fa-solid fa-info-circle me-1"></i> Hệ thống sẽ tự động tính: <code>Tiền Phòng + Điện + Nước - Giảm Giá</code> từ dữ liệu chỉ số đã chốt.
                </div>
                <button type="submit" class="btn btn-success w-100 py-2 fw-bold">
                    <i class="fa-solid fa-paper-plane me-1"></i> Phát Hành Hóa Đơn
                </button>
            </form>
        </div>
    </div>
</div>

<div class="card p-4 bg-white shadow-sm mb-4">
    <h5 class="fw-bold mb-3"><i class="fa-solid fa-receipt me-2 text-primary"></i>Danh Sách Hóa Đơn KTX</h5>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th>Mã HĐ</th>
                    <th>Phòng</th>
                    <th>Kỳ Tháng</th>
                    <th>Tiền Phòng</th>
                    <th>Điện</th>
                    <th>Nước</th>
                    <th>Tổng Tiền</th>
                    <th>Đã Trả</th>
                    <th>Trạng Thái</th>
                    <th>Thu Tiền Mặt</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="inv" items="${invoices}">
                    <tr>
                        <td>#${inv.id}</td>
                        <td><span class="badge bg-secondary">${inv.roomId}</span></td>
                        <td>${inv.month}</td>
                        <td><fmt:formatNumber value="${inv.roomFee}" pattern="#,###"/> đ</td>
                        <td><fmt:formatNumber value="${inv.electricFee}" pattern="#,###"/> đ</td>
                        <td><fmt:formatNumber value="${inv.waterFee}" pattern="#,###"/> đ</td>
                        <td><strong><fmt:formatNumber value="${inv.totalAmount}" pattern="#,###"/> đ</strong></td>
                        <td><fmt:formatNumber value="${inv.paidAmount}" pattern="#,###"/> đ</td>
                        <td>
                            <span class="badge ${inv.status == 'DA_TT' ? 'bg-success' : (inv.status == 'TT_MOT_PHAN' ? 'bg-warning text-dark' : 'bg-danger')}">
                                ${inv.status == 'DA_TT' ? 'Đã thu đủ' : (inv.status == 'TT_MOT_PHAN' ? 'Thu một phần' : 'Chưa thu')}
                            </span>
                        </td>
                        <td>
                            <c:if test="${inv.status != 'DA_TT'}">
                                <form action="${pageContext.request.contextPath}/accounting/confirm-payment" method="POST" class="d-inline">
                                    <input type="hidden" name="txnRef" value="CASH-${inv.id}-${inv.month}">
                                    <input type="hidden" name="method" value="TIEN_MAT">
                                    <input type="hidden" name="amount" value="${inv.totalAmount - inv.paidAmount}">
                                    <button type="submit" class="btn btn-sm btn-outline-success"><i class="fa-solid fa-hand-holding-dollar"></i> Xác nhận</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
