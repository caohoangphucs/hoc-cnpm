<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="row g-4 mb-4">
    <div class="col-md-3">
        <div class="card p-3 bg-white shadow-sm border-start border-4 border-primary">
            <div class="text-muted small">YÊU CẦU ĐĂNG KÝ CHỜ DUYỆT</div>
            <h3 class="fw-bold my-1 text-primary">${pendingList.size()}</h3>
            <a href="${pageContext.request.contextPath}/admin/registrations" class="small text-decoration-none">Xem danh sách →</a>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3 bg-white shadow-sm border-start border-4 border-success">
            <div class="text-muted small">HỆ THỐNG PHÒNG</div>
            <h3 class="fw-bold my-1 text-success">3 Tòa Nhà</h3>
            <span class="small text-muted">Tòa A, Tòa B, Tòa C</span>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3 bg-white shadow-sm border-start border-4 border-warning">
            <div class="text-muted small">CƠ SỞ DỮ LIỆU</div>
            <h3 class="fw-bold my-1 text-warning">MySQL 8.0</h3>
            <span class="small text-muted">Connection Pool Ready</span>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3 bg-white shadow-sm border-start border-4 border-danger">
            <div class="text-muted small">KIẾN TRÚC</div>
            <h3 class="fw-bold my-1 text-danger">3 Tầng</h3>
            <span class="small text-muted">Web • Service • SP/DB</span>
        </div>
    </div>
</div>

<div class="card p-4 bg-white shadow-sm mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="fw-bold mb-0"><i class="fa-solid fa-list-check me-2 text-primary"></i>Danh Sách Đăng Ký Chờ Duyệt (UC-04)</h5>
        <a href="${pageContext.request.contextPath}/admin/registrations" class="btn btn-outline-primary btn-sm">Xem tất cả</a>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th>Mã ĐK</th>
                    <th>Sinh Viên</th>
                    <th>Email</th>
                    <th>SĐT</th>
                    <th>Phòng / Giường</th>
                    <th>Ngày Đăng Ký</th>
                    <th>Hành Động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${pendingList}">
                    <tr>
                        <td>#${item.id}</td>
                        <td><strong>${item.studentName}</strong> (${item.studentId})</td>
                        <td>${item.email}</td>
                        <td>${item.phone}</td>
                        <td><span class="badge bg-info text-dark">${item.roomId} - ${item.bedNumber}</span></td>
                        <td><fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/admin/approve-registration" method="POST" class="d-inline">
                                <input type="hidden" name="registrationId" value="${item.id}">
                                <input type="hidden" name="decision" value="DUYET">
                                <button type="submit" class="btn btn-success btn-sm"><i class="fa-solid fa-check"></i> Duyệt</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/approve-registration" method="POST" class="d-inline ms-1">
                                <input type="hidden" name="registrationId" value="${item.id}">
                                <input type="hidden" name="decision" value="TU_CHOI">
                                <button type="submit" class="btn btn-danger btn-sm"><i class="fa-solid fa-xmark"></i> Từ chối</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pendingList}">
                    <tr><td colspan="7" class="text-center text-muted py-3">Hiện không có yêu cầu đăng ký nào cần duyệt.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
