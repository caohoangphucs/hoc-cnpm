<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="fw-bold"><i class="fa-solid fa-file-invoice-dollar me-2 text-success"></i>Hóa Đơn & Thanh Toán Trực Tuyến (UC-07)</h4>
</div>

<c:choose>
    <c:when test="${empty invoices}">
        <div class="alert alert-info py-4 text-center">
            <i class="fa-solid fa-circle-check fs-3 mb-2 d-block text-primary"></i>
            <h5>Phòng của bạn chưa có hóa đơn cần thanh toán.</h5>
            <p class="text-muted mb-0">Hóa đơn điện nước và tiền phòng sẽ được phát hành định kỳ hàng tháng.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card p-0 bg-white shadow-sm overflow-hidden">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Mã HĐ</th>
                            <th>Kỳ Tháng</th>
                            <th>Tiền Phòng</th>
                            <th>Tiền Điện</th>
                            <th>Tiền Nước</th>
                            <th>Tổng Tiền</th>
                            <th>Đã Trả</th>
                            <th>Hạn Nộp</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inv" items="${invoices}">
                            <tr>
                                <td><strong>#HĐ-${inv.id}</strong></td>
                                <td>${inv.month}</td>
                                <td><fmt:formatNumber value="${inv.roomFee}" pattern="#,###"/> đ</td>
                                <td><fmt:formatNumber value="${inv.electricFee}" pattern="#,###"/> đ</td>
                                <td><fmt:formatNumber value="${inv.waterFee}" pattern="#,###"/> đ</td>
                                <td><strong class="text-primary"><fmt:formatNumber value="${inv.totalAmount}" pattern="#,###"/> đ</strong></td>
                                <td><fmt:formatNumber value="${inv.paidAmount}" pattern="#,###"/> đ</td>
                                <td><fmt:formatDate value="${inv.dueDate}" pattern="dd/MM/yyyy"/></td>
                                <td>
                                    <span class="badge ${inv.status == 'DA_TT' ? 'bg-success' : (inv.status == 'TT_MOT_PHAN' ? 'bg-warning text-dark' : 'bg-danger')}">
                                        ${inv.status == 'DA_TT' ? 'Đã thanh toán' : (inv.status == 'TT_MOT_PHAN' ? 'Trả một phần' : 'Chưa thanh toán')}
                                    </span>
                                </td>
                                <td>
                                    <c:if test="${inv.status != 'DA_TT'}">
                                        <button class="btn btn-sm btn-primary" onclick="payVnpay('${inv.id}', '${inv.totalAmount - inv.paidAmount}')">
                                            <i class="fa-solid fa-credit-card me-1"></i> VNPAY
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<script>
function payVnpay(id, amount) {
    alert("Đang chuyển hướng tới cổng thanh toán VNPAY Sandbox cho Hóa đơn #" + id + " (Số tiền: " + Number(amount).toLocaleString() + " VNĐ)...");
}
</script>

<%@ include file="../common/_footer.jspf" %>
