<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<h4 class="fw-bold mb-4"><i class="fa-solid fa-chart-pie me-2 text-primary"></i>Báo Cáo & Thống Kê Tổng Hợp (UC-08)</h4>

<div class="row g-4 mb-4">
    <div class="col-md-6">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold mb-3"><i class="fa-solid fa-building me-2 text-info"></i>Tỷ Lệ Lấp Đầy Giường Theo Tòa (vw_TyLeLapDayPhong)</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Tòa Nhà</th>
                            <th>Tổng Phòng</th>
                            <th>Tổng Sức Chứa</th>
                            <th>Đang Ở</th>
                            <th>Tỷ Lệ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="occ" items="${occupancy}">
                            <tr>
                                <td><strong>${occ.buildingName}</strong></td>
                                <td>${occ.totalRooms}</td>
                                <td>${occ.totalCapacity} chỗ</td>
                                <td>${occ.totalOccupied} sv</td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="progress flex-grow-1" style="height: 8px;">
                                            <div class="progress-bar bg-success" role="progressbar" style="width: ${occ.rate}%"></div>
                                        </div>
                                        <span class="small fw-bold">${occ.rate}%</span>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-md-6">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold mb-3"><i class="fa-solid fa-money-bill-trend-up me-2 text-success"></i>Doanh Thu Hóa Đơn Hàng Tháng (vw_DoanhThuThang)</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Kỳ Tháng</th>
                            <th>Số HĐ</th>
                            <th>Phát Hành</th>
                            <th>Thực Thu</th>
                            <th>Công Nợ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="rev" items="${revenue}">
                            <tr>
                                <td><strong>${rev.month}</strong></td>
                                <td>${rev.totalInvoices}</td>
                                <td><fmt:formatNumber value="${rev.totalBilled}" pattern="#,###"/> đ</td>
                                <td class="text-success fw-bold"><fmt:formatNumber value="${rev.totalCollected}" pattern="#,###"/> đ</td>
                                <td class="text-danger"><fmt:formatNumber value="${rev.debtAmount}" pattern="#,###"/> đ</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
