package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.api.dto.EntryResourceDto;
import vn.id.caohoangphuc.cnpm.service.ServiceContext;

public class GateRepository {
  public record Card(
      String studentId,
      String name,
      String roomId,
      String residencyState,
      Instant validFrom,
      Instant validTo,
      String state) {}

  public record Entry(
      UUID eventId,
      String studentId,
      String type,
      Instant time,
      String barrierStatus,
      Integer violationId,
      String payloadHash) {}

  public record Visitor(
      long id,
      String hostStudentId,
      String state,
      Instant validFrom,
      Instant validTo,
      long version) {}

  public record EntryPage(java.util.List<EntryResourceDto> items, long total) {}

  private final JdbcTemplate jdbc;

  /** Khởi tạo repository cho thẻ, lượt ra vào và khách thăm. */
  public GateRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Băm token thẻ trước khi tra cứu để không lưu token thô trong cơ sở dữ liệu. */
  public String hashCard(String token) {
    return vn.id.caohoangphuc.cnpm.service.ServiceContext.hash(token);
  }

  /** Tìm và khóa thẻ cùng thông tin sinh viên, lưu trú và hiệu lực. */
  public Card card(String hash) {
    var rows =
        jdbc.query(
            """
            SELECT c.student_id,s.hoten,s.maphonghientai,l.trangthai,c.valid_from,c.valid_to,c.state
            FROM svc_card c JOIN sinh_vien s ON s.masinhvien=c.student_id
            LEFT JOIN luu_tru l ON l.masinhvien=s.masinhvien AND l.trangthai='DANG_O'
            WHERE c.token_hash=? FOR UPDATE OF c
            """,
            (r, n) ->
                new Card(
                    r.getString(1),
                    r.getString(2),
                    r.getString(3),
                    r.getString(4),
                    r.getTimestamp(5).toInstant(),
                    r.getTimestamp(6).toInstant(),
                    r.getString(7)),
            hash);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Đọc sự kiện cổng theo UUID; có thể khóa trước khi hoàn tất xử lý. */
  public Entry event(UUID id, boolean lock) {
    var rows =
        jdbc.query(
            """
SELECT e.event_id,r.masinhvien,r.loaisukien,r.thoigian,e.barrier_status,v.mavipham,e.payload_hash
FROM svc_gate_event e JOIN ra_vao r ON r.maravao=e.entry_id
LEFT JOIN vi_pham v ON v.maravao=r.maravao WHERE e.event_id=?
"""
                + (lock ? " FOR UPDATE OF e" : ""),
            (r, n) ->
                new Entry(
                    r.getObject(1, UUID.class),
                    r.getString(2),
                    r.getString(3),
                    r.getTimestamp(4).toInstant(),
                    r.getString(5),
                    (Integer) r.getObject(6),
                    r.getString(7)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lists gate events, optionally restricted to a student's own events. */
  public EntryPage entries(String studentId, int page, int size) {
    var items = jdbc.query("""
        SELECT e.event_id,r.masinhvien,r.loaisukien,r.thoigian,e.barrier_status,v.mavipham
        FROM svc_gate_event e JOIN ra_vao r ON r.maravao=e.entry_id
        LEFT JOIN vi_pham v ON v.maravao=r.maravao
        WHERE (CAST(? AS varchar) IS NULL OR r.masinhvien=?)
        ORDER BY r.thoigian DESC,e.event_id DESC LIMIT ? OFFSET ?
        """, (r, n) -> new EntryResourceDto(r.getObject(1, UUID.class), r.getString(2), r.getString(3),
        r.getObject(4, LocalDateTime.class).atZone(ServiceContext.ZONE).toOffsetDateTime(),
        r.getString(5), (Integer) r.getObject(6)), studentId, studentId, size, (page - 1) * size);
    Long total = jdbc.queryForObject("""
        SELECT count(*) FROM svc_gate_event e JOIN ra_vao r ON r.maravao=e.entry_id
        WHERE (CAST(? AS varchar) IS NULL OR r.masinhvien=?)
        """, Long.class, studentId, studentId);
    return new EntryPage(items, total == null ? 0 : total);
  }

  /** Ghi một lượt ra vào và trả về mã lượt mới. */
  public int entry(String student, String type, String guard, String note, LocalDateTime time) {
    return jdbc.queryForObject(
        "INSERT INTO ra_vao(masinhvien,loaisukien,baoveghinhan,ghichu,thoigian) VALUES(?,?,?,?,?)"
            + " RETURNING maravao",
        Integer.class,
        student,
        type,
        guard,
        note,
        time);
  }

  /** Tạo vi phạm mức thấp khi sinh viên về trễ. */
  public int lateViolation(String student, int entryId, String note, Instant at) {
    return jdbc.queryForObject(
        "INSERT INTO vi_pham(masinhvien,maravao,noidungvipham,mucdo,ngayvipham)"
            + " VALUES(?,?,?,'LOW',?) RETURNING mavipham",
        Integer.class,
        student,
        entryId,
        note,
        java.sql.Timestamp.from(at));
  }

  /** Chỉ đọc vi phạm đúng ID và đúng sinh viên của lượt cổng đã được kiểm tra quyền. */
  public EntryResourceDto.GateViolation violation(Integer id, String student) {
    if (id == null) return null;
    var rows = jdbc.query(
        "SELECT mavipham,noidungvipham,mucdo,trangthai,ngayvipham FROM vi_pham WHERE mavipham=? AND masinhvien=?",
        (r, n) -> new EntryResourceDto.GateViolation(r.getInt("mavipham"),
            r.getString("noidungvipham"), r.getString("mucdo"), r.getString("trangthai"),
            r.getObject("ngayvipham", LocalDateTime.class).atZone(ServiceContext.ZONE).toOffsetDateTime()),
        id, student);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lưu dấu vết xử lý sự kiện thiết bị và trạng thái barrier ban đầu. */
  public void gateEvent(
      UUID eventId, int entryId, String device, String hash, int actor, String status) {
    jdbc.update(
        "INSERT INTO"
            + " svc_gate_event(event_id,entry_id,device_id,payload_hash,actor_id,barrier_status)"
            + " VALUES(?,?,?,?,?,?)",
        eventId,
        entryId,
        device,
        hash,
        actor,
        status);
  }

  /** Cập nhật trạng thái barrier đã nhận cho sự kiện cổng. */
  public void barrier(UUID id, String status) {
    jdbc.update("UPDATE svc_gate_event SET barrier_status=? WHERE event_id=?", status, id);
  }

  /** Đọc khách thăm; khóa dòng nếu service đang duyệt hoặc cập nhật lượt thăm. */
  public Visitor visitor(long id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT id,host_student_id,state,valid_from,valid_to,version FROM svc_visitor WHERE"
                + " id=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Visitor(
                    r.getLong(1),
                    r.getString(2),
                    r.getString(3),
                    r.getTimestamp(4).toInstant(),
                    r.getTimestamp(5).toInstant(),
                    r.getLong(6)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Returns visitor requests for a student, or all requests when no owner is supplied. */
  public java.util.List<Visitor> visitors(String hostStudentId, int page, int size) {
    return jdbc.query("""
        SELECT id,host_student_id,state,valid_from,valid_to,version FROM svc_visitor
        WHERE (CAST(? AS varchar) IS NULL OR host_student_id=?) ORDER BY id DESC LIMIT ? OFFSET ?
        """, (r, n) -> new Visitor(r.getLong(1), r.getString(2), r.getString(3),
        r.getTimestamp(4).toInstant(), r.getTimestamp(5).toInstant(), r.getLong(6)),
        hostStudentId, hostStudentId, size, (page - 1) * size);
  }

  /** Deletes a visitor request that has not yet been approved. */
  public boolean deletePendingVisitor(long id) {
    return jdbc.update("DELETE FROM svc_visitor WHERE id=? AND state='PENDING'", id) == 1;
  }

  /** Kiểm tra tài khoản hiện tại có sở hữu hồ sơ sinh viên được nêu hay không. */
  public boolean ownsStudent(int accountId, String studentId) {
    return !jdbc.queryForList(
            "SELECT masinhvien FROM sinh_vien WHERE mataikhoan=? AND masinhvien=?",
            String.class,
            accountId,
            studentId)
        .isEmpty();
  }

  /** Tìm tài khoản chủ hồ sơ; trả về 0 khi không có sinh viên tương ứng. */
  public int studentAccount(String studentId) {
    var rows =
        jdbc.queryForList(
            "SELECT mataikhoan FROM sinh_vien WHERE masinhvien=?", Integer.class, studentId);
    return rows.isEmpty() ? 0 : rows.get(0);
  }

  /** Lưu khách thăm với thông tin nhận dạng đã mã hóa và thời hạn được duyệt. */
  public long visitor(String host, String name, String identity, Instant from, Instant to) {
    return jdbc.queryForObject(
        "INSERT INTO"
            + " svc_visitor(host_student_id,name_encrypted,identity_encrypted,valid_from,valid_to)"
            + " VALUES(?,?,?,?,?) RETURNING id",
        Long.class,
        host,
        name,
        identity,
        java.sql.Timestamp.from(from),
        java.sql.Timestamp.from(to));
  }

  /** Cập nhật trạng thái, người duyệt và version của yêu cầu thăm. */
  public void visitorState(long id, String state, int approver) {
    jdbc.update(
        "UPDATE svc_visitor SET state=?,approved_by=?,version=version+1 WHERE id=?",
        state,
        approver,
        id);
  }

  /** Ghi nhận check-in hoặc check-out đúng một lần cho khách thăm. */
  public void visitorEntry(long id, boolean entering, Instant at) {
    String sql =
        entering
            ? "UPDATE svc_visitor SET checked_in_at=? WHERE id=? AND checked_in_at IS NULL AND"
                + " checked_out_at IS NULL"
            : "UPDATE svc_visitor SET checked_out_at=? WHERE id=? AND checked_in_at IS NOT NULL AND"
                + " checked_out_at IS NULL";
    if (jdbc.update(sql, java.sql.Timestamp.from(at), id) != 1)
      throw new vn.id.caohoangphuc.cnpm.service.ServiceException(
          409, "CONFLICT", "Visitor has already been checked in or out");
  }
}
