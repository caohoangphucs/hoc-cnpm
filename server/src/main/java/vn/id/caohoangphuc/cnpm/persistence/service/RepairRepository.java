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

  public RepairRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

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

  public Repair repair(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM yeu_cau_sua_chua WHERE mayeucau=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) -> map(r),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public int technicianRole(int id) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM tai_khoan WHERE mataikhoan=? AND vaitro='KYTHUAT' AND"
            + " trangthai='ACTIVE'",
        Integer.class,
        id);
  }

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

  public void schedule(int id, Instant when, LocalDate warranty, String note) {
    jdbc.update(
        "UPDATE svc_repair_assignment SET scheduled_at=?,warranty_until=? WHERE repair_id=?",
        java.sql.Timestamp.from(when),
        warranty,
        id);
    jdbc.update(
        "UPDATE yeu_cau_sua_chua SET version=version+1,ghichukythuat=? WHERE mayeucau=?", note, id);
  }

  public Supply supply(String id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT mavattu,soluongton FROM vat_tu WHERE mavattu=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) -> new Supply(r.getString(1), r.getInt(2)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public void purchase(int repair, String supply, int quantity, java.util.UUID request) {
    jdbc.update(
        "INSERT INTO svc_purchase_request(repair_id,supply_id,quantity,request_id) VALUES(?,?,?,?)"
            + " ON CONFLICT DO NOTHING",
        repair,
        supply,
        quantity,
        request);
  }

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

  public List<Repair> listForStudent(String studentId, int page, int size) {
    return jdbc.query(
        "SELECT * FROM yeu_cau_sua_chua WHERE masinhvien=? ORDER BY ngaytao DESC LIMIT ? OFFSET ?",
        (r, n) -> map(r),
        studentId,
        size,
        (page - 1) * size);
  }

  public List<Repair> assigned() {
    return jdbc.query(
        "SELECT r.* FROM yeu_cau_sua_chua r JOIN svc_repair_assignment a ON a.repair_id=r.mayeucau"
            + " WHERE r.trangthai='DANG_XU_LY' ORDER BY r.mayeucau",
        (r, n) -> map(r));
  }
}
