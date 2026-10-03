package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

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

  private final JdbcTemplate jdbc;

  public GateRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String hashCard(String token) {
    return vn.id.caohoangphuc.cnpm.service.ServiceContext.hash(token);
  }

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

  public void barrier(UUID id, String status) {
    jdbc.update("UPDATE svc_gate_event SET barrier_status=? WHERE event_id=?", status, id);
  }

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

  public boolean ownsStudent(int accountId, String studentId) {
    return !jdbc.queryForList(
            "SELECT masinhvien FROM sinh_vien WHERE mataikhoan=? AND masinhvien=?",
            String.class,
            accountId,
            studentId)
        .isEmpty();
  }

  public int studentAccount(String studentId) {
    var rows =
        jdbc.queryForList(
            "SELECT mataikhoan FROM sinh_vien WHERE masinhvien=?", Integer.class, studentId);
    return rows.isEmpty() ? 0 : rows.get(0);
  }

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

  public void visitorState(long id, String state, int approver) {
    jdbc.update(
        "UPDATE svc_visitor SET state=?,approved_by=?,version=version+1 WHERE id=?",
        state,
        approver,
        id);
  }

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
