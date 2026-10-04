package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class RepairRepository {
  public record Repair(
      int id,
      String roomId,
      String studentId,
      String title,
      String priority,
      String status,
      long version) {}

  public record Assignment(int technicianId, Instant scheduledAt, LocalDate warrantyUntil) {}

  public record Supply(String id, int stock) {}

  public record Line(String supplyId, int quantity) {}

  private final JdbcTemplate jdbc;

  public record Material(String id, String name, String unit, int stock, java.math.BigDecimal price) {}

  /** Khóa hồ sơ sinh viên để phòng không thay đổi giữa kiểm tra và tạo phiếu. */
  public boolean hasCurrentRoom(String studentId) {
    var rooms = jdbc.query("SELECT maphonghientai FROM sinh_vien WHERE masinhvien=? FOR UPDATE",
        (r, n) -> r.getString(1), studentId);
    return !rooms.isEmpty() && rooms.get(0) != null && !rooms.get(0).isBlank();
  }

  /** Danh mục và tồn thực tế dùng chung cho kho và form nghiệm thu. */
  public List<Material> materials() {
    return jdbc.query("SELECT mavattu,tenvattu,donvitinh,soluongton,dongianhap FROM vat_tu ORDER BY tenvattu,mavattu",
        (r,n) -> new Material(r.getString(1),r.getString(2),r.getString(3),r.getInt(4),r.getBigDecimal(5)));
  }

  /** Chỉ thêm mã mới; tồn kho được trừ bởi luồng hoàn thành sửa chữa. */
  public boolean createMaterial(vn.id.caohoangphuc.cnpm.api.dto.MaterialDto input) {
    return jdbc.update("INSERT INTO vat_tu(mavattu,tenvattu,donvitinh,soluongton,dongianhap) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING",
        input.id(),input.name(),input.unit(),input.stock(),input.price()) == 1;
  }

  /** Gộp ảnh nghiệm thu vào hồ sơ phiếu, không ghi đè ảnh báo sự cố. */
  public void attachCompletionFiles(int id, String files) {
    jdbc.update("INSERT INTO svc_repair_files(repair_id,file_ids) VALUES(?,?::jsonb) ON CONFLICT(repair_id) DO UPDATE SET file_ids=svc_repair_files.file_ids || EXCLUDED.file_ids", id, files);
  }

  /** Đọc các UUID ảnh đã gắn vào phiếu. */
  public List<java.util.UUID> files(int id) {
    return jdbc.query("SELECT jsonb_array_elements_text(file_ids) FROM svc_repair_files WHERE repair_id=?",
        (r,n) -> java.util.UUID.fromString(r.getString(1)), id);
  }

  /** Lưu mức ưu tiên mới và tăng phiên bản, lịch sử do service ghi cùng transaction. */
  public void priority(int id, String priority) {
    jdbc.update("UPDATE yeu_cau_sua_chua SET mucdouutien=?,version=version+1 WHERE mayeucau=?", priority,id);
  }

  /** Khởi tạo repository cho yêu cầu sửa chữa, phân công và xuất vật tư. */
  public RepairRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Ánh xạ dòng yêu cầu sửa chữa từ truy vấn SQL sang mô hình service. */
  private Repair map(java.sql.ResultSet r) throws java.sql.SQLException {
    return new Repair(
        r.getInt("mayeucau"),
        r.getString("maphong"),
        r.getString("masinhvien"),
        r.getString("tieude"),
        r.getString("mucdouutien"),
        r.getString("trangthai"),
        r.getLong("version"));
  }

  /** Liệt kê yêu cầu theo vai trò; kỹ thuật viên chỉ thấy việc được phân công. */
  public List<Repair> list(String role, int actor, int page, int size) {
    String sql =
        role.equals("KYTHUAT")
            ? "SELECT r.* FROM yeu_cau_sua_chua r JOIN svc_repair_assignment a ON"
                + " a.repair_id=r.mayeucau WHERE a.technician_id=? ORDER BY r.ngaytao DESC LIMIT"
                + " ? OFFSET ?"
            : "SELECT r.* FROM yeu_cau_sua_chua r ORDER BY r.ngaytao DESC LIMIT ? OFFSET ?";
    return role.equals("KYTHUAT")
        ? jdbc.query(sql, (r, n) -> map(r), actor, size, (page - 1) * size)
        : jdbc.query(sql, (r, n) -> map(r), size, (page - 1) * size);
  }

  /** Đọc yêu cầu sửa chữa theo ID và tùy chọn khóa trước khi cập nhật. */
  public Repair repair(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM yeu_cau_sua_chua WHERE mayeucau=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) -> map(r),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Deletes an unassigned repair request and its uploaded file metadata. */
  public boolean deleteUnassigned(int id) {
    jdbc.update("DELETE FROM svc_repair_files WHERE repair_id=?", id);
    jdbc.update("DELETE FROM svc_purchase_request WHERE repair_id=?", id);
    jdbc.update("DELETE FROM svc_repair_history WHERE repair_id=? AND to_state='MOI'", id);
    return jdbc.update("DELETE FROM yeu_cau_sua_chua WHERE mayeucau=?", id) == 1;
  }

  /** Checks whether a repair has technician, inventory, or audit history that must be retained. */
  public boolean hasHistory(int id) {
    Integer count = jdbc.queryForObject("""
        SELECT (EXISTS(SELECT 1 FROM svc_repair_assignment WHERE repair_id=?))::int
             + (EXISTS(SELECT 1 FROM svc_repair_history WHERE repair_id=? AND to_state<>'MOI'))::int
             + (EXISTS(SELECT 1 FROM xuat_vat_tu WHERE mayeucau=?))::int
        """, Integer.class, id, id, id);
    return count != null && count > 0;
  }

  /** Kiểm tra tài khoản có vai trò kỹ thuật viên và đang hoạt động. */
  public int technicianRole(int id) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM tai_khoan WHERE mataikhoan=? AND vaitro='KYTHUAT' AND"
            + " trangthai='ACTIVE'",
        Integer.class,
        id);
  }

  /** Đọc lịch phân công và thời hạn bảo hành; có thể khóa dòng phân công. */
  public Assignment assignment(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT technician_id,scheduled_at,warranty_until FROM svc_repair_assignment WHERE"
                + " repair_id=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Assignment(
                    r.getInt(1),
                    r.getTimestamp(2) == null ? null : r.getTimestamp(2).toInstant(),
                    r.getObject(3, LocalDate.class)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Phân công kỹ thuật viên và chuyển yêu cầu sang trạng thái đang xử lý. */
  public void assign(int id, int technician, Instant when) {
    jdbc.update(
        "INSERT INTO svc_repair_assignment(repair_id,technician_id,scheduled_at) VALUES(?,?,?)",
        id,
        technician,
        java.sql.Timestamp.from(when));
    jdbc.update(
        "UPDATE yeu_cau_sua_chua SET trangthai='DANG_XU_LY',version=version+1 WHERE mayeucau=?",
        id);
  }

  /** Cập nhật lịch hẹn, bảo hành và ghi chú kỹ thuật. */
  public void schedule(int id, Instant when, LocalDate warranty, String note) {
    jdbc.update(
        "UPDATE svc_repair_assignment SET scheduled_at=?,warranty_until=? WHERE repair_id=?",
        java.sql.Timestamp.from(when),
        warranty,
        id);
    jdbc.update(
        "UPDATE yeu_cau_sua_chua SET version=version+1,ghichukythuat=? WHERE mayeucau=?", note, id);
  }

  /** Đọc tồn kho một vật tư và tùy chọn khóa dòng khi xuất kho. */
  public Supply supply(String id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT mavattu,soluongton FROM vat_tu WHERE mavattu=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) -> new Supply(r.getString(1), r.getInt(2)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lưu yêu cầu mua vật tư; request ID chống tạo yêu cầu trùng khi retry. */
  public void purchase(int repair, String supply, int quantity, java.util.UUID request) {
    jdbc.update(
        "INSERT INTO svc_purchase_request(repair_id,supply_id,quantity,request_id) VALUES(?,?,?,?)"
            + " ON CONFLICT DO NOTHING",
        repair,
        supply,
        quantity,
        request);
  }

  /** Trừ kho, ghi phiếu xuất và hoàn tất yêu cầu trong transaction hiện tại. */
  public void complete(
      int id, List<Line> supplies, int actor, String note, LocalDate warranty, Instant now) {
    for (Line line : supplies) {
      jdbc.update(
          "UPDATE vat_tu SET soluongton=soluongton-? WHERE mavattu=?",
          line.quantity(),
          line.supplyId());
      jdbc.update(
          "INSERT INTO xuat_vat_tu(mavattu,mayeucau,soluong,nguoixuat,ghichu) VALUES(?,?,?,?,?)",
          line.supplyId(),
          id,
          line.quantity(),
          Integer.toString(actor),
          note);
    }
    jdbc.update(
        "UPDATE yeu_cau_sua_chua SET"
            + " trangthai='HOAN_THANH',version=version+1,ngayhoanthanh=?,ghichukythuat=? WHERE"
            + " mayeucau=?",
        java.sql.Timestamp.from(now),
        note,
        id);
    jdbc.update(
        "UPDATE svc_repair_assignment SET warranty_until=? WHERE repair_id=?", warranty, id);
  }

  /** Ghi lịch sử chuyển trạng thái để truy vết xử lý sửa chữa. */
  public void history(int id, int actor, String from, String to, String note, Instant at) {
    jdbc.update(
        "INSERT INTO svc_repair_history(repair_id,actor_id,from_state,to_state,note,created_at)"
            + " VALUES(?,?,?,?,?,?)",
        id,
        actor,
        from,
        to,
        note,
        java.sql.Timestamp.from(at));
  }

  /** Tạo yêu cầu gắn với phòng hiện tại của sinh viên và lưu hồ sơ file. */
  public int create(
      String studentId,
      String title,
      String description,
      String priority,
      Instant created,
      String files) {
    int id =
        jdbc.queryForObject(
            "INSERT INTO"
                + " yeu_cau_sua_chua(maphong,masinhvien,tieude,motachitiet,mucdouutien,ngaytao)"
                + " SELECT MaPhongHienTai,masinhvien,?,?,?,? FROM sinh_vien"
                + " WHERE masinhvien=? RETURNING mayeucau",
            Integer.class,
            title,
            description,
            priority,
            java.sql.Timestamp.from(created),
            studentId);
    jdbc.update("INSERT INTO svc_repair_files(repair_id,file_ids) VALUES(?,?::jsonb)", id, files);
    return id;
  }

  /** Liệt kê các yêu cầu sửa chữa của sinh viên theo thời gian tạo. */
  public List<Repair> listForStudent(String studentId, int page, int size) {
    return jdbc.query(
        "SELECT * FROM yeu_cau_sua_chua WHERE masinhvien=? ORDER BY ngaytao DESC LIMIT ? OFFSET ?",
        (r, n) -> map(r),
        studentId,
        size,
        (page - 1) * size);
  }

  /** Liệt kê các yêu cầu đang xử lý đã được phân công. */
  public List<Repair> assigned() {
    return jdbc.query(
        "SELECT r.* FROM yeu_cau_sua_chua r JOIN svc_repair_assignment a ON a.repair_id=r.mayeucau"
            + " WHERE r.trangthai='DANG_XU_LY' ORDER BY r.mayeucau",
        (r, n) -> map(r));
  }
}
