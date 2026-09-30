<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="card p-4 bg-white shadow-sm mb-4">
    <h4 class="fw-bold mb-3"><i class="fa-solid fa-id-card me-2 text-primary"></i>Phê Duyệt Đăng Ký Phòng KTX (UC-04)</h4>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th>Mã ĐK</th>
                    <th>Sinh Viên</th>
                    <th>Email</th>
                    <th>SĐT</th>
                    <th>Phòng / Giường</th>
                    <th>Hạn Giữ</th>
                    <th>Hành Động & Ghi Chú</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${pendingList}">
                    <tr>
                        <td>#${item.id}</td>
                        <td><strong>${item.studentName}</strong><br><small class="text-muted">MSSV: ${item.studentId}</small></td>
                        <td>${item.email}</td>
                        <td>${item.phone}</td>
                        <td><span class="badge bg-primary">${item.roomId} - ${item.bedNumber}</span></td>
                        <td><small class="text-danger"><fmt:formatDate value="${item.expireAt}" pattern="dd/MM/yyyy HH:mm"/></small></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/admin/approve-registration" method="POST" class="d-flex gap-2 align-items-center">
                                <input type="hidden" name="registrationId" value="${item.id}">
                                <input type="text" name="note" class="form-control form-control-sm" placeholder="Ghi chú lý do...">
                                <button type="submit" name="decision" value="DUYET" class="btn btn-success btn-sm text-nowrap"><i class="fa-solid fa-check"></i> Duyệt</button>
                                <button type="submit" name="decision" value="TU_CHOI" class="btn btn-danger btn-sm text-nowrap"><i class="fa-solid fa-xmark"></i> Từ chối</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pendingList}">
                    <tr><td colspan="7" class="text-center text-muted py-4">Tất cả yêu cầu đăng ký đã được xử lý hoàn tất.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
