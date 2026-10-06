package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class DisciplineRepository {
  public record Violation(
      int id,
      String studentId,
      String content,
      String level,
      String status,
      long version,
      long semesterId,
      long count,
      boolean evidenceRequired) {}

  public record Decision(
      long id,
      int violationId,
      String studentId,
      long semesterId,
      int revision,
      String decision,
      String reason,
      int actorId,
      Instant createdAt, java.util.UUID evidenceFileId) {}

  public record Appeal(long id, long decisionId, String studentId, String state, long version) {}

  private final JdbcTemplate jdbc;

  /** Khởi tạo repository cho quyết định kỷ luật và khiếu nại. */
  public DisciplineRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Ánh xạ dữ liệu vi phạm cùng thống kê học kỳ sang mô hình service. */
  private Violation map(java.sql.ResultSet r) throws java.sql.SQLException {
    return new Violation(
        r.getInt("mavipham"),
        r.getString("masinhvien"),
        r.getString("noidungvipham"),
        r.getString("mucdo"),
        r.getString("trangthai"),
        r.getLong("version"),
        r.getLong("semester_id"),
        r.getLong("semester_count"),
        r.getBoolean("evidence_required"));
  }

  /** Khóa sinh viên trước khi ghi quyết định hoặc xử lý khiếu nại. */
  public void lockStudent(String id) {
    jdbc.queryForList("SELECT masinhvien FROM sinh_vien WHERE masinhvien=? FOR UPDATE", id);
  }

  /** Tìm mã vi phạm gắn với một quyết định kỷ luật. */
  public int violationIdForDecision(long decisionId) {
    return jdbc.queryForObject(
        "SELECT violation_id FROM svc_discipline_decision WHERE id=?", Integer.class, decisionId);
  }

  /** Trả về mã sinh viên của các vi phạm theo thứ tự khóa ổn định. */
  public List<String> studentIds(List<Long> ids) {
    String sql =
        "SELECT DISTINCT masinhvien FROM vi_pham WHERE mavipham IN ("
            + String.join(",", java.util.Collections.nCopies(ids.size(), "?"))
            + ") ORDER BY masinhvien";
    return jdbc.queryForList(sql, String.class, ids.toArray());
  }

  /** Đọc ID bằng chứng đã lưu cho một quyết định kỷ luật. */
  public java.util.UUID evidence(long decisionId) {
    var rows =
        jdbc.queryForList(
            "SELECT evidence_id FROM svc_discipline_decision WHERE id=?",
            java.util.UUID.class,
            decisionId);
    if (rows.isEmpty()) throw vn.id.caohoangphuc.cnpm.service.ServiceException.notFound("Decision");
    return rows.get(0);
  }

  /** Kiểm tra quyết định thuộc về sinh viên được nêu trong yêu cầu. */
  public long decisionStudent(long id, String student) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM svc_discipline_decision d JOIN vi_pham v ON v.mavipham=d.violation_id"
            + " WHERE d.id=? AND v.masinhvien=?",
        Long.class,
        id,
        student);
  }

  /** Đọc các vi phạm được chọn; có thể khóa chúng trước khi áp dụng hàng loạt. */
  public List<Violation> violations(List<Long> ids, boolean lock) {
    String query =
        """
SELECT v.*,h.public_id semester_id,
  (SELECT count(*) FROM vi_pham v2 WHERE v2.masinhvien=v.masinhvien
     AND v2.ngayvipham::date BETWEEN h.ngaybatdau AND h.ngayketthuc AND v2.trangthai='DA_XU_LY') semester_count,
  (v.maravao IS NOT NULL) evidence_required
FROM vi_pham v JOIN sinh_vien s USING(masinhvien)
JOIN hoc_ky h ON v.ngayvipham::date BETWEEN h.ngaybatdau AND h.ngayketthuc
WHERE v.mavipham IN (%s) ORDER BY v.masinhvien,v.mavipham
"""
                .formatted(String.join(",", java.util.Collections.nCopies(ids.size(), "?")))
            + (lock ? " FOR UPDATE OF v" : "");
    return jdbc.query(query, (r, n) -> map(r), ids.toArray());
  }

  /** Liệt kê vi phạm chưa xử lý kèm số lần vi phạm trong học kỳ. */
  public List<Violation> pending(int page, int size) {
    return jdbc.query(
        """
SELECT v.*,h.public_id semester_id,
 (SELECT count(*) FROM vi_pham v2 WHERE v2.masinhvien=v.masinhvien AND v2.ngayvipham::date BETWEEN h.ngaybatdau AND h.ngayketthuc AND v2.trangthai='DA_XU_LY') semester_count,
 (v.maravao IS NOT NULL) evidence_required
        FROM vi_pham v JOIN hoc_ky h ON v.ngayvipham::date BETWEEN h.ngaybatdau AND h.ngayketthuc
WHERE v.trangthai='CHUA_XU_LY' ORDER BY v.ngayvipham DESC LIMIT ? OFFSET ?
""",
        (r, n) -> map(r),
        size,
        (page - 1) * size);
  }

  /** Lists all violations that fall within a configured semester. */
  public List<Violation> all(int page, int size) {
    var ids = jdbc.queryForList("""
        SELECT v.mavipham FROM vi_pham v JOIN hoc_ky h
          ON v.ngayvipham::date BETWEEN h.ngaybatdau AND h.ngayketthuc
        ORDER BY v.ngayvipham DESC,v.mavipham DESC LIMIT ? OFFSET ?
        """, Integer.class, size, (page - 1) * size);
    if (ids.isEmpty()) return List.of();
    return violations(ids.stream().map(Long::valueOf).toList(), false);
  }

  /** Creates an unprocessed violation without attaching it to a gate event. */
  public int create(String student, String content, String level, Instant occurredAt) {
    return jdbc.queryForObject("""
        INSERT INTO vi_pham(masinhvien,noidungvipham,mucdo,ngayvipham,trangthai)
        VALUES(?,?,?,?,'CHUA_XU_LY') RETURNING mavipham
        """, Integer.class, student, content, level, java.sql.Timestamp.from(occurredAt));
  }

  /** Updates the editable description and severity of a violation. */
  public boolean update(int id, String content, String level) {
    return jdbc.update("UPDATE vi_pham SET noidungvipham=?,mucdo=?,version=version+1 WHERE mavipham=?",
        content, level, id) == 1;
  }

  /** Confirms student and incident date are valid for a manually recorded violation. */
  public boolean canCreate(String student, Instant occurredAt) {
    return Boolean.TRUE.equals(jdbc.queryForObject("""
        SELECT EXISTS (SELECT 1 FROM sinh_vien WHERE masinhvien=?)
          AND EXISTS (SELECT 1 FROM hoc_ky WHERE ?::date BETWEEN ngaybatdau AND ngayketthuc)
        """, Boolean.class, student, java.sql.Timestamp.from(occurredAt)));
  }

  /** Deletes only an unprocessed manual violation with no decision history or gate event. */
  public boolean delete(int id) {
    return jdbc.update("""
        DELETE FROM vi_pham v WHERE v.mavipham=? AND v.trangthai='CHUA_XU_LY'
          AND v.maravao IS NULL
          AND NOT EXISTS (SELECT 1 FROM svc_discipline_decision d WHERE d.violation_id=v.mavipham)
        """, id) == 1;
  }

  /** Tính số thứ tự revision tiếp theo cho một vi phạm. */
  public int nextRevision(int id) {
    return jdbc.queryForObject(
        "SELECT coalesce(max(revision),0)+1 FROM svc_discipline_decision WHERE violation_id=?",
        Integer.class,
        id);
  }

  /** Lưu quyết định và cập nhật trạng thái xử lý của vi phạm cùng transaction. */
  public void decide(
      int id,
      long semester,
      int revision,
      int actor,
      String decision,
      String reason,
      java.util.UUID evidence,
      Instant at) {
    jdbc.update(
        "INSERT INTO"
            + " svc_discipline_decision(violation_id,semester_id,revision,actor_id,decision,reason,evidence_id,created_at)"
            + " VALUES(?,?,?,?,?,?,?,?)",
        id,
        semester,
        revision,
        actor,
        decision,
        reason,
        evidence,
        java.sql.Timestamp.from(at));
    jdbc.update(
        "UPDATE vi_pham SET trangthai='DA_XU_LY',hinhthucxuly=?,version=version+1 WHERE mavipham=?",
        decision,
        id);
  }

  /** Đọc lịch sử các quyết định của một vi phạm theo revision. */
  public List<Decision> decisions(int id) {
    return jdbc.query(
        "SELECT"
            + " d.id,d.violation_id,v.masinhvien,d.semester_id,d.revision,d.decision,d.reason,d.actor_id,d.created_at,d.evidence_id"
            + " FROM svc_discipline_decision d JOIN vi_pham v ON v.mavipham=d.violation_id WHERE"
            + " d.violation_id=? ORDER BY d.revision",
        (r, n) ->
            new Decision(
                r.getLong(1),
                r.getInt(2),
                r.getString(3),
                r.getLong(4),
                r.getInt(5),
                r.getString(6),
                r.getString(7),
                r.getInt(8),
                r.getTimestamp(9).toInstant(), r.getObject(10,java.util.UUID.class)),
        id);
  }

  /** Tạo khiếu nại mới cho quyết định của sinh viên. */
  public long createAppeal(long decision, String student, String content) {
    return jdbc.queryForObject(
        "INSERT INTO svc_appeal(decision_id,student_id,content) VALUES(?,?,?) RETURNING id",
        Long.class,
        decision,
        student,
        content);
  }

  /** Đọc khiếu nại; khóa dòng nếu service chuẩn bị phản hồi. */
  public Appeal appeal(long id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT id,decision_id,student_id,state,version FROM svc_appeal WHERE id=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Appeal(
                    r.getLong(1), r.getLong(2), r.getString(3), r.getString(4), r.getLong(5)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lưu kết quả phản hồi, người xử lý và version mới của khiếu nại. */
  public void respond(long id, String status, String reason, int actor) {
    jdbc.update(
        "UPDATE svc_appeal SET state=?,response=?,handled_by=?,version=version+1 WHERE id=?",
        status,
        reason,
        actor,
        id);
  }
}
