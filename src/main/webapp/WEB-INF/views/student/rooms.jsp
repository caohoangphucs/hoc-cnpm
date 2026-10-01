<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../common/_header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="fw-bold"><i class="fa-solid fa-bed me-2 text-primary"></i>Danh Sách Phòng & Đặt Giữ Chỗ (UC-03)</h4>
    <span class="badge bg-primary fs-6">Sơ Đồ KTX Trực Tuyến</span>
</div>

<div class="row g-4">
    <c:forEach var="room" items="${rooms}">
        <div class="col-md-6 col-lg-4">
            <div class="card h-100 p-3 bg-white shadow-sm border-top border-4 border-primary">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <h5 class="fw-bold mb-0">${room.roomNumber} - ${room.buildingName}</h5>
                        <small class="text-muted">Tầng ${room.floor} • Sức chứa: ${room.capacity} người</small>
                    </div>
                    <span class="badge ${room.available > 0 ? 'bg-success' : 'bg-danger'}">
                        ${room.available > 0 ? 'Còn ' + room.available + ' chỗ' : 'Hết chỗ'}
                    </span>
                </div>
                <div class="my-2 py-2 border-top border-bottom small">
                    <div><strong>Giá phòng:</strong> <fmt:formatNumber value="${room.price}" pattern="#,###"/> VNĐ/tháng</div>
                    <div><strong>Đang ở:</strong> ${room.occupied}/${room.capacity} sinh viên</div>
                </div>
                <div class="mt-2">
                    <h6 class="small fw-bold text-muted mb-2">Chọn Giường:</h6>
                    <form action="${pageContext.request.contextPath}/student/book-bed" method="POST">
                        <div class="input-group">
                            <select name="bedId" class="form-select form-select-sm" required>
                                <option value="G-${room.roomId}-1">Giường G-01 (Tầng dưới)</option>
                                <option value="G-${room.roomId}-2">Giường G-02 (Tầng trên)</option>
                                <option value="G-${room.roomId}-3">Giường G-03 (Tầng dưới)</option>
                                <option value="G-${room.roomId}-4">Giường G-04 (Tầng trên)</option>
                            </select>
                            <button type="submit" class="btn btn-primary btn-sm px-3">
                                <i class="fa-solid fa-bookmark me-1"></i> Đặt Giữ
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<%@ include file="../common/_footer.jspf" %>
