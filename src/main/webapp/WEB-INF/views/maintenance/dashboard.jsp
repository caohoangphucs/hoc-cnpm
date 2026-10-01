<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="row g-4 mb-4">
    <div class="col-md-5">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold text-primary mb-3"><i class="fa-solid fa-boxes-stacked me-2"></i>Kho Vật Tư Sửa Chữa (Kỹ Thuật)</h5>
            <div class="table-responsive">
                <table class="table table-sm table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Mã VT</th>
                            <th>Tên Vật Tư</th>
                            <th>ĐVT</th>
                            <th>Tồn Kho</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${supplies}">
                            <tr>
                                <td><code>${item.id}</code></td>
                                <td>${item.name}</td>
                                <td>${item.unit}</td>
                                <td><span class="badge ${item.stock > 10 ? 'bg-success' : 'bg-danger'}">${item.stock}</span></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-md-7">
        <div class="card p-4 bg-white shadow-sm h-100">
            <h5 class="fw-bold text-danger mb-3"><i class="fa-solid fa-screwdriver-wrench me-2"></i>Danh Sách Báo Hỏng Cần Xử Lý</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Phòng</th>
                            <th>Tiêu Đề</th>
                            <th>Mức Độ</th>
                            <th>Trạng Thái</th>
                            <th>Xử Lý & Xuất Kho</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="reqItem" items="${requests}">
                            <tr>
                                <td><span class="badge bg-secondary">${reqItem.roomId}</span></td>
                                <td><strong>${reqItem.title}</strong><br><small class="text-muted">${reqItem.description}</small></td>
                                <td>
                                    <span class="badge ${reqItem.priority == 'KHAN_CAP' ? 'bg-danger' : (reqItem.priority == 'CAO' ? 'bg-warning text-dark' : 'bg-info text-dark')}">
                                        ${reqItem.priority}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge ${reqItem.status == 'HOAN_THANH' ? 'bg-success' : 'bg-primary'}">
                                        ${reqItem.status}
                                    </span>
                                </td>
                                <td>
                                    <c:if test="${reqItem.status != 'HOAN_THANH'}">
                                        <form action="${pageContext.request.contextPath}/maintenance/complete-repair" method="POST" class="d-flex flex-column gap-1">
                                            <input type="hidden" name="requestId" value="${reqItem.id}">
                                            <select name="supplyId" class="form-select form-select-sm">
                                                <option value="">-- Chọn vật tư xuất --</option>
                                                <c:forEach var="sup" items="${supplies}">
                                                    <option value="${sup.id}">${sup.name} (Tồn: ${sup.stock})</option>
                                                </c:forEach>
                                            </select>
                                            <div class="d-flex gap-1">
                                                <input type="number" name="quantity" class="form-control form-control-sm" value="1" min="0" style="width: 70px;">
                                                <input type="text" name="note" class="form-control form-control-sm" placeholder="Ghi chú">
                                                <button type="submit" class="btn btn-sm btn-success text-nowrap"><i class="fa-solid fa-check"></i> Xong</button>
                                            </div>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/_footer.jspf" %>
