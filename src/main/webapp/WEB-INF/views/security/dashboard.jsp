<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="row g-4 mb-4">
    <div class="col-md-4">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold text-primary mb-3"><i class="fa-solid fa-id-badge me-2"></i>Ghi Nhận Ra / Vào Cổng</h5>
            <form action="${pageContext.request.contextPath}/security/record-entry" method="POST">
                <div class="mb-3">
                    <label class="form-label small fw-bold">Mã Số Sinh Viên (MSSV)</label>
                    <input type="text" name="studentId" class="form-control" placeholder="VD: 21110101" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-bold">Sự Kiện</label>
                    <select name="type" class="form-select" required>
                        <option value="VAO">Cổng Vào (Quét thẻ vào KTX)</option>
                        <option value="RA">Cổng Ra (Rời KTX)</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-bold">Ghi chú thêm</label>
                    <input type="text" name="note" class="form-control" placeholder="Xe máy, mang vác tài sản...">
                </div>
                <div class="alert alert-warning small">
                    <i class="fa-solid fa-clock me-1"></i> <strong>Quy định giờ giới nghiêm:</strong> Quét thẻ VÀO sau 23:00 sẽ tự động tạo biên bản vi phạm kỷ luật.
                </div>
                <button type="submit" class="btn btn-primary w-100 fw-bold">
                    <i class="fa-solid fa-barcode me-1"></i> Quét & Ghi Nhận
                </button>
            </form>
        </div>
    </div>

    <div class="col-md-8">
        <div class="card p-4 bg-white shadow-sm h-100 mb-4">
            <h5 class="fw-bold text-danger mb-3"><i class="fa-solid fa-triangle-exclamation me-2"></i>Danh Sách Vi Phạm Kỷ Luật KTX</h5>
            <div class="table-responsive">
                <table class="table table-sm table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Mã VP</th>
                            <th>Sinh Viên</th>
                            <th>Phòng</th>
                            <th>Nội Dung Vi Phạm</th>
                            <th>Mức Độ</th>
                            <th>Thời Gian</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="vp" items="${violations}">
                            <tr>
                                <td>#${vp.id}</td>
                                <td><strong>${vp.studentName}</strong> (${vp.studentId})</td>
                                <td><span class="badge bg-secondary">${vp.room}</span></td>
                                <td>${vp.content}</td>
                                <td>
                                    <span class="badge ${vp.level == 'CRITICAL' ? 'bg-danger' : (vp.level == 'HIGH' ? 'bg-warning text-dark' : 'bg-info text-dark')}">
                                        ${vp.level}
                                    </span>
                                </td>
                                <td><small><fmt:formatDate value="${vp.time}" pattern="dd/MM HH:mm"/></small></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty violations}">
                            <tr><td colspan="6" class="text-center text-muted py-3">Không có bản ghi vi phạm nào.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="card p-4 bg-white shadow-sm">
            <h5 class="fw-bold text-secondary mb-3"><i class="fa-solid fa-list-ol me-2"></i>Nhật Ký Quét Thẻ Ra Vào Gần Đây</h5>
            <div class="table-responsive">
                <table class="table table-sm table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Sinh Viên</th>
                            <th>Phòng</th>
                            <th>Sự Kiện</th>
                            <th>Thời Gian</th>
                            <th>Bảo Vệ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td>${log.studentName} (${log.studentId})</td>
                                <td>${log.room}</td>
                                <td>
                                    <span class="badge ${log.type == 'VAO' ? 'bg-success' : 'bg-warning text-dark'}">
                                        ${log.type}
                                    </span>
                                </td>
                                <td><fmt:formatDate value="${log.time}" pattern="dd/MM HH:mm:ss"/></td>
                                <td>${log.guard}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
