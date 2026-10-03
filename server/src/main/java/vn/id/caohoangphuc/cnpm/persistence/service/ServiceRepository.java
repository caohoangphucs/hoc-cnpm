package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Shared SQL for account checks, request deduplication and durable events. No business decisions.
 */
public class ServiceRepository {
  public record Account(int id, String role, String status, String studentId) {}

  public record SavedRequest(String hash, String response, Instant expiresAt) {}

  public record File(UUID id, int ownerId, String kind, String state, Instant expiresAt) {}

  private final JdbcTemplate jdbc;

  /** Khởi tạo repository dùng chung bằng JDBC. */
  public ServiceRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Đọc tài khoản và khóa chia sẻ để ổn định quyền trong transaction hiện tại. */
  public Account account(int id) {
    var rows =
        jdbc.query(
            """
            SELECT t.mataikhoan,t.vaitro,t.trangthai,s.masinhvien FROM tai_khoan t
            LEFT JOIN sinh_vien s USING(mataikhoan) WHERE t.mataikhoan=? FOR SHARE OF t
            """,
            (rs, n) -> new Account(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Khóa idempotency theo actor, route và key để tuần tự hóa các lần retry. */
  public void lockRequest(int actorId, String route, UUID key) {
    jdbc.execute(
        (org.springframework.jdbc.core.PreparedStatementCreator)
            connection -> {
              var statement =
                  connection.prepareStatement(
                      "SELECT pg_advisory_xact_lock(hashtextextended(?,0))");
              statement.setString(1, actorId + ":" + route + ":" + key);
              return statement;
            },
        statement -> {
          statement.execute();
          return null;
        });
  }

  /** Đọc kết quả đã lưu của một request idempotent, nếu có. */
  public SavedRequest request(int actorId, String route, UUID key) {
    var rows =
        jdbc.query(
            "SELECT request_hash,response::text,expires_at FROM svc_idempotency WHERE actor_id=?"
                + " AND route=? AND key=?",
            (rs, n) ->
                new SavedRequest(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toInstant()),
            actorId,
            route,
            key);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lưu hoặc cập nhật request và response JSON để các lần retry dùng lại. */
  public void saveRequest(
      int actorId, String route, UUID key, String hash, String response, Instant expiresAt) {
    jdbc.update(
        """
INSERT INTO svc_idempotency(actor_id,route,key,request_hash,response,expires_at) VALUES(?,?,?,?,?::jsonb,?)
ON CONFLICT(actor_id,route,key) DO UPDATE SET request_hash=excluded.request_hash,response=excluded.response,expires_at=excluded.expires_at
""",
        actorId,
        route,
        key,
        hash,
        response,
        java.sql.Timestamp.from(expiresAt));
  }

  /** Đọc và khóa file nháp trước khi kiểm tra quyền sở hữu hoặc gắn file. */
  public File file(UUID id) {
    var rows =
        jdbc.query(
            "SELECT id,owner_id,kind,state,expires_at FROM svc_file WHERE id=? FOR UPDATE",
            (rs, n) ->
                new File(
                    rs.getObject(1, UUID.class),
                    rs.getInt(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getTimestamp(5).toInstant()),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Đánh dấu file đã được gắn vào hồ sơ nghiệp vụ. */
  public void attachFile(UUID id) {
    jdbc.update("UPDATE svc_file SET state='ATTACHED' WHERE id=?", id);
  }

  /** Ghi thông tin audit kèm request ID để truy vết thao tác. */
  public void audit(int actorId, UUID requestId, String table, String action, String details) {
    jdbc.update(
        "INSERT INTO audit_log(tenbang,hanhdong,dulieumoi,nguoithuchien) VALUES(?,?,?::jsonb ||"
            + " jsonb_build_object('requestId',?::text),?)",
        table,
        action,
        details,
        requestId.toString(),
        Integer.toString(actorId));
  }

  /** Thêm sự kiện vào outbox; dedupe key ngăn ghi trùng khi retry. */
  public void enqueue(String type, String aggregateId, String payload, String dedupeKey) {
    jdbc.update(
        "INSERT INTO svc_outbox(event_type,aggregate_id,payload,dedupe_key) VALUES(?,?,?::jsonb,?)"
            + " ON CONFLICT(dedupe_key) DO NOTHING",
        type,
        aggregateId,
        payload,
        dedupeKey);
  }
}
