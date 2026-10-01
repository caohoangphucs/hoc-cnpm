package vn.id.caohoangphuc.cnpm.persistence.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class PaymentRepository {
  public record Invoice(
      int id, String roomId, BigDecimal total, BigDecimal paid, String status, String studentId) {}

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

  public PaymentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Invoice invoice(int id, boolean lock) {
    var rows =
        jdbc.query(
            """
SELECT h.mahoadon,h.maphong,h.tongtien,h.dathanhtoan,h.trangthai,s.masinhvien
FROM hoa_don h JOIN luu_tru l ON l.trangthai='DANG_O'
JOIN giuong g ON g.magiuong=l.magiuong AND g.maphong=h.maphong JOIN sinh_vien s ON s.masinhvien=l.masinhvien
WHERE h.mahoadon=? ORDER BY s.masinhvien LIMIT 1
"""
                + (lock ? " FOR UPDATE OF h" : ""),
            (r, n) ->
                new Invoice(
                    r.getInt(1),
                    r.getString(2),
                    r.getBigDecimal(3),
                    r.getBigDecimal(4),
                    r.getString(5),
                    r.getString(6)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

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

  public List<Allocation> allocations(UUID id) {
    return jdbc.query(
        "SELECT invoice_id,amount,child_ref FROM svc_payment_allocation WHERE session_id=? ORDER BY"
            + " invoice_id",
        (r, n) -> new Allocation(r.getInt(1), r.getBigDecimal(2), r.getString(3)),
        id);
  }

  public void payUrl(UUID id, String url) {
    jdbc.update("UPDATE svc_payment_session SET pay_url=? WHERE id=? AND pay_url IS NULL", url, id);
  }

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

  public void paymentState(UUID id, String state, String transaction) {
    jdbc.update(
        "UPDATE svc_payment_session SET state=?,provider_transaction_id=? WHERE id=?",
        state,
        transaction,
        id);
  }

  public void childState(UUID id, String state) {
    jdbc.update(
        "UPDATE thanh_toan SET trangthai=? WHERE magiaodich IN (SELECT child_ref FROM"
            + " svc_payment_allocation WHERE session_id=?) AND trangthai='PENDING'",
        state,
        id);
  }

  public List<UUID> expired(Instant now, int limit) {
    return jdbc.queryForList(
        "SELECT id FROM svc_payment_session WHERE state='PENDING' AND expires_at<=? ORDER BY"
            + " expires_at LIMIT ?",
        UUID.class,
        java.sql.Timestamp.from(now),
        limit);
  }

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
