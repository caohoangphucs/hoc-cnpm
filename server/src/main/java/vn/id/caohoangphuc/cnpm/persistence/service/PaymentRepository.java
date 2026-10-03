package vn.id.caohoangphuc.cnpm.persistence.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class PaymentRepository {
  public record Invoice(int id, String roomId, BigDecimal total, BigDecimal paid, String status) {}

  public record Session(
      UUID id,
      int actorId,
      String gateway,
      String providerRef,
      BigDecimal amount,
      String status,
      Instant expiresAt,
      String payUrl,
      String providerTransactionId) {}

  public record Allocation(int invoiceId, BigDecimal amount, String childRef) {}

  public record Callback(
      String gateway, String eventKey, UUID sessionId, String hash, String response) {}

  private final JdbcTemplate jdbc;

  /** Khởi tạo repository cho phiên thanh toán và phân bổ hóa đơn. */
  public PaymentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Đọc hóa đơn và tùy chọn khóa trước khi ghi nhận khoản thanh toán. */
  public Invoice invoice(int id, boolean lock) {
    var rows =
        jdbc.query(
            """
SELECT h.mahoadon,h.maphong,h.tongtien,h.dathanhtoan,h.trangthai
FROM hoa_don h WHERE h.mahoadon=?
"""
                + (lock ? " FOR UPDATE OF h" : ""),
            (r, n) ->
                new Invoice(
                    r.getInt(1),
                    r.getString(2),
                    r.getBigDecimal(3),
                    r.getBigDecimal(4),
                    r.getString(5)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Checks the resident list captured when the bill was issued; legacy bills use residency dates. */
  public boolean belongsToStudent(int invoiceId, String studentId) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            """
            SELECT EXISTS (
              SELECT 1
              FROM svc_invoice_snapshot snap,
                   jsonb_array_elements(snap.inputs->'occupants') occupant
              WHERE snap.invoice_id=? AND occupant->>'studentId'=?
              UNION ALL
              SELECT 1
              FROM hoa_don h
              JOIN giuong g ON g.maphong=h.maphong
              JOIN luu_tru l ON l.magiuong=g.magiuong
              WHERE h.mahoadon=? AND l.masinhvien=?
                AND l.ngaybatdau <=
                    (to_date(h.kythang || '-01','YYYY-MM-DD') + INTERVAL '1 month - 1 day')::date
                AND (l.ngayketthuc IS NULL OR
                     l.ngayketthuc >= to_date(h.kythang || '-01','YYYY-MM-DD'))
            )
            """,
            Boolean.class,
            invoiceId,
            studentId,
            invoiceId,
            studentId));
  }

  /** Tạo phiên thanh toán mới và trả lại phiên đã khóa. */
  public Session create(
      UUID id, int actor, String gateway, String ref, BigDecimal amount, Instant expires) {
    jdbc.update(
        "INSERT INTO svc_payment_session(id,actor_id,gateway,provider_ref,amount,expires_at)"
            + " VALUES(?,?,?,?,?,?)",
        id,
        actor,
        gateway,
        ref,
        amount,
        java.sql.Timestamp.from(expires));
    return session(id, actor, true);
  }

  /** Lưu phân bổ hóa đơn và khoản thanh toán con ở trạng thái chờ. */
  public void allocate(UUID id, int invoice, String gateway, BigDecimal amount, String child) {
    jdbc.update(
        "INSERT INTO svc_payment_allocation(session_id,invoice_id,amount,child_ref)"
            + " VALUES(?,?,?,?)",
        id,
        invoice,
        amount,
        child);
    jdbc.update(
        "INSERT INTO thanh_toan(mahoadon,magiaodich,phuongthuc,sotien,trangthai,ghichu)"
            + " VALUES(?,?,?,?,'PENDING',?)",
        invoice,
        child,
        gateway,
        amount,
        id.toString());
  }

  /** Đọc phiên thuộc đúng actor; có thể khóa trước khi cập nhật. */
  public Session session(UUID id, int actor, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT"
                + " id,actor_id,gateway,provider_ref,amount,state,expires_at,pay_url,provider_transaction_id"
                + " FROM svc_payment_session WHERE id=? AND actor_id=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Session(
                    r.getObject(1, UUID.class),
                    r.getInt(2),
                    r.getString(3),
                    r.getString(4),
                    r.getBigDecimal(5),
                    r.getString(6),
                    r.getTimestamp(7).toInstant(),
                    r.getString(8),
                    r.getString(9)),
            id,
            actor);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Đọc phiên theo ID, không giới hạn actor; có thể khóa dòng. */
  public Session session(UUID id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT"
                + " id,actor_id,gateway,provider_ref,amount,state,expires_at,pay_url,provider_transaction_id"
                + " FROM svc_payment_session WHERE id=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Session(
                    r.getObject(1, UUID.class),
                    r.getInt(2),
                    r.getString(3),
                    r.getString(4),
                    r.getBigDecimal(5),
                    r.getString(6),
                    r.getTimestamp(7).toInstant(),
                    r.getString(8),
                    r.getString(9)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lists payment sessions for one account, or all accounts for an administrator/accounting. */
  public List<Session> sessions(Integer actorId, int page, int size) {
    String ownerFilter = actorId == null ? "" : "WHERE actor_id=?";
    Object[] args = actorId == null ? new Object[] {size, (page - 1) * size}
        : new Object[] {actorId, size, (page - 1) * size};
    return jdbc.query("SELECT id,actor_id,gateway,provider_ref,amount,state,expires_at,pay_url,provider_transaction_id "
            + "FROM svc_payment_session " + ownerFilter + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
        (r, n) -> new Session(r.getObject(1, UUID.class), r.getInt(2), r.getString(3), r.getString(4),
            r.getBigDecimal(5), r.getString(6), r.getTimestamp(7).toInstant(), r.getString(8), r.getString(9)),
        args);
  }

  /** Checks whether a provider callback links to a session. */
  public boolean hasCallback(UUID id) {
    Integer count = jdbc.queryForObject("SELECT count(*) FROM svc_payment_callback WHERE session_id=?",
        Integer.class, id);
    return count != null && count > 0;
  }

  /** Deletes pending child transactions, allocations, and a session without callback history. */
  public boolean deletePending(UUID id) {
    jdbc.update("DELETE FROM thanh_toan WHERE magiaodich IN "
        + "(SELECT child_ref FROM svc_payment_allocation WHERE session_id=?) AND trangthai='PENDING'", id);
    jdbc.update("DELETE FROM svc_payment_allocation WHERE session_id=?", id);
    return jdbc.update("DELETE FROM svc_payment_session WHERE id=? AND state='PENDING'", id) == 1;
  }

  /** Tìm phiên theo cổng và mã tham chiếu do provider trả về. */
  public Session byReference(String gateway, String ref, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT"
                + " id,actor_id,gateway,provider_ref,amount,state,expires_at,pay_url,provider_transaction_id"
                + " FROM svc_payment_session WHERE gateway=? AND provider_ref=?"
                + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Session(
                    r.getObject(1, UUID.class),
                    r.getInt(2),
                    r.getString(3),
                    r.getString(4),
                    r.getBigDecimal(5),
                    r.getString(6),
                    r.getTimestamp(7).toInstant(),
                    r.getString(8),
                    r.getString(9)),
            gateway,
            ref);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Liệt kê các hóa đơn và số tiền đã phân bổ cho phiên. */
  public List<Allocation> allocations(UUID id) {
    return jdbc.query(
        "SELECT invoice_id,amount,child_ref FROM svc_payment_allocation WHERE session_id=? ORDER BY"
            + " invoice_id",
        (r, n) -> new Allocation(r.getInt(1), r.getBigDecimal(2), r.getString(3)),
        id);
  }

  /** Lưu URL provider lần đầu mà không ghi đè URL khi retry. */
  public void payUrl(UUID id, String url) {
    jdbc.update("UPDATE svc_payment_session SET pay_url=? WHERE id=? AND pay_url IS NULL", url, id);
  }

  /** Đọc kết quả callback đã lưu theo cổng và event key. */
  public Callback callback(String gateway, String event) {
    var rows =
        jdbc.query(
            "SELECT gateway,event_key,session_id,payload_hash,result::text FROM"
                + " svc_payment_callback WHERE gateway=? AND event_key=?",
            (r, n) ->
                new Callback(
                    r.getString(1),
                    r.getString(2),
                    r.getObject(3, UUID.class),
                    r.getString(4),
                    r.getString(5)),
            gateway,
            event);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Lưu callback đã xác minh cùng payload hash và response có thể replay. */
  public void saveCallback(
      String gateway, String event, UUID session, String hash, String response, Instant at) {
    jdbc.update(
        "INSERT INTO"
            + " svc_payment_callback(gateway,event_key,session_id,payload_hash,result,received_at)"
            + " VALUES(?,?,?, ?,?::jsonb,?)",
        gateway,
        event,
        session,
        hash,
        response,
        java.sql.Timestamp.from(at));
  }

  /** Cộng khoản phân bổ vào hóa đơn và đánh dấu giao dịch con thành công. */
  public void confirmInvoice(Allocation allocation, int actor) {
    var invoice =
        jdbc.queryForObject(
            "SELECT tongtien,dathanhtoan FROM hoa_don WHERE mahoadon=? FOR UPDATE",
            (r, n) -> new BigDecimal[] {r.getBigDecimal(1), r.getBigDecimal(2)},
            allocation.invoiceId());
    BigDecimal after = invoice[1].add(allocation.amount());
    if (after.compareTo(invoice[0]) > 0)
      throw vn.id.caohoangphuc.cnpm.service.ServiceException.conflict(
          "Payment would exceed invoice balance");
    jdbc.update(
        "UPDATE thanh_toan SET trangthai='SUCCESS',nguoixacnhan=? WHERE magiaodich=? AND"
            + " trangthai='PENDING'",
        Integer.toString(actor),
        allocation.childRef());
    if (jdbc.update(
            "UPDATE hoa_don SET dathanhtoan=?,trangthai=? WHERE mahoadon=? AND trangthai<>'HUY'",
            after,
            after.compareTo(invoice[0]) == 0 ? "DA_TT" : "TT_MOT_PHAN",
            allocation.invoiceId())
        != 1)
      throw vn.id.caohoangphuc.cnpm.service.ServiceException.conflict(
          "Invoice can no longer be paid");
  }

  /** Cập nhật trạng thái và mã giao dịch phía nhà cung cấp của phiên. */
  public void paymentState(UUID id, String state, String transaction) {
    jdbc.update(
        "UPDATE svc_payment_session SET state=?,provider_transaction_id=? WHERE id=?",
        state,
        transaction,
        id);
  }

  /** Cập nhật các giao dịch con còn chờ theo trạng thái của phiên. */
  public void childState(UUID id, String state) {
    jdbc.update(
        "UPDATE thanh_toan SET trangthai=? WHERE magiaodich IN (SELECT child_ref FROM"
            + " svc_payment_allocation WHERE session_id=?) AND trangthai='PENDING'",
        state,
        id);
  }

  /** Liệt kê các phiên chờ đã hết hạn theo thứ tự và giới hạn batch. */
  public List<UUID> expired(Instant now, int limit) {
    return jdbc.queryForList(
        "SELECT id FROM svc_payment_session WHERE state='PENDING' AND expires_at<=? ORDER BY"
            + " expires_at LIMIT ?",
        UUID.class,
        java.sql.Timestamp.from(now),
        limit);
  }

  /** Chuyển phiên còn chờ sang hết hạn và đóng các giao dịch con của nó. */
  public boolean expire(UUID id, Instant now) {
    if (jdbc.update(
            "UPDATE svc_payment_session SET state='EXPIRED' WHERE id=? AND state='PENDING' AND"
                + " expires_at<=?",
            id,
            java.sql.Timestamp.from(now))
        != 1) return false;
    childState(id, "FAILED");
    return true;
  }
}
