package vn.id.caohoangphuc.cnpm.persistence.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class InvoiceRepository {
  public record Reading(long oldElectricity, long newElectricity, long oldWater, long newWater) {}

  public record Price(BigDecimal electricity, BigDecimal water, BigDecimal room) {}

  public record Occupant(String studentId, BigDecimal roomShare, BigDecimal discountPercent) {}

  public record Extra(String name, BigDecimal amount) {}

  public record Draft(UUID id, int actorId, String month, long version, String rooms) {}

  public record Invoice(
      int id, String roomId, String month, BigDecimal total, BigDecimal paid, String status) {}

  private final JdbcTemplate jdbc;

  public InvoiceRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public boolean roomExists(String roomId) {
    return !jdbc.queryForList("SELECT maphong FROM phong WHERE maphong=?", String.class, roomId)
        .isEmpty();
  }

  public Reading reading(String roomId, String month) {
    var rows =
        jdbc.query(
            "SELECT diencu,dienmoi,nuoccu,nuocmoi FROM chi_so_dien_nuoc WHERE maphong=? AND"
                + " kythang=?",
            (rs, n) -> new Reading(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)),
            roomId,
            month);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public Price prices(String roomId, String month) {
    LocalDate first = LocalDate.parse(month + "-01");
    var electric =
        jdbc.queryForList(
            "SELECT dongia FROM don_gia_dich_vu WHERE loaidichvu='DIEN' AND trangthai='ACTIVE' AND"
                + " ngayapdung<=? ORDER BY ngayapdung DESC,madongia LIMIT 1",
            BigDecimal.class,
            first);
    var water =
        jdbc.queryForList(
            "SELECT dongia FROM don_gia_dich_vu WHERE loaidichvu='NUOC' AND trangthai='ACTIVE' AND"
                + " ngayapdung<=? ORDER BY ngayapdung DESC,madongia LIMIT 1",
            BigDecimal.class,
            first);
    var room =
        jdbc.queryForList(
            "SELECT amount FROM svc_room_price WHERE room_id=? AND effective_from<=? ORDER BY"
                + " effective_from DESC LIMIT 1",
            BigDecimal.class,
            roomId,
            first);
    if (electric.isEmpty() || water.isEmpty()) return null;
    BigDecimal roomPrice =
        room.isEmpty()
            ? jdbc.queryForObject(
                "SELECT giaphongcoban FROM phong WHERE maphong=?", BigDecimal.class, roomId)
            : room.get(0);
    return new Price(electric.get(0), water.get(0), roomPrice);
  }

  public List<Occupant> occupants(String roomId, String month, BigDecimal fullRoomPrice) {
    LocalDate first = LocalDate.parse(month + "-01");
    var students =
        jdbc.queryForList(
            """
SELECT DISTINCT l.masinhvien FROM luu_tru l JOIN giuong g USING(magiuong)
WHERE g.maphong=? AND l.ngaybatdau<=? AND (l.ngayketthuc IS NULL OR l.ngayketthuc>=?) ORDER BY l.masinhvien
""",
            String.class,
            roomId,
            first,
            first);
    var result = new java.util.ArrayList<Occupant>();
    BigDecimal[] division =
        fullRoomPrice.divideAndRemainder(BigDecimal.valueOf(Math.max(1, students.size())));
    for (int i = 0; i < students.size(); i++) {
      String id = students.get(i);
      BigDecimal percent =
          jdbc.queryForObject(
              """
SELECT coalesce(max(cs.tylegiam),0) FROM sinh_vien_chinh_sach sc JOIN chinh_sach_giam_gia cs USING(machinhsach)
WHERE sc.masinhvien=? AND sc.ngayxacnhan<=? AND cs.ngaybatdau<=? AND (cs.ngayketthuc IS NULL OR cs.ngayketthuc>=?)
AND cs.trangthai='DANG_AP_DUNG'
""",
              BigDecimal.class,
              id,
              first,
              first,
              first);
      BigDecimal share =
          division[0].add(i < division[1].intValueExact() ? BigDecimal.ONE : BigDecimal.ZERO);
      result.add(new Occupant(id, share, percent));
    }
    return result;
  }

  public List<Extra> extras(String roomId, String month) {
    return jdbc.query(
        "SELECT name,amount FROM svc_room_extra WHERE room_id=? AND month=? ORDER BY id",
        (rs, n) -> new Extra(rs.getString(1), rs.getBigDecimal(2)),
        roomId,
        month);
  }

  public boolean issued(String roomId, String month) {
    return !jdbc.queryForList(
            "SELECT mahoadon FROM hoa_don WHERE maphong=? AND kythang=?",
            Integer.class,
            roomId,
            month)
        .isEmpty();
  }

  public void saveDraft(UUID id, int actor, String month, String rooms, String hash) {
    jdbc.update(
        "INSERT INTO svc_invoice_draft(id,actor_id,month,inputs,input_hash)"
            + " VALUES(?,?,?,?::jsonb,?)",
        id,
        actor,
        month,
        rooms,
        hash);
  }

  public Draft draft(UUID id, int actor, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT id,actor_id,month,version,inputs::text FROM svc_invoice_draft WHERE id=? AND"
                + " actor_id=?"
                + (lock ? " FOR UPDATE" : ""),
            (rs, n) ->
                new Draft(
                    rs.getObject(1, UUID.class),
                    rs.getInt(2),
                    rs.getString(3),
                    rs.getLong(4),
                    rs.getString(5)),
            id,
            actor);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public Invoice issue(
      String roomId,
      String month,
      Reading reading,
      Price price,
      BigDecimal discount,
      List<Extra> extras,
      LocalDate dueDate) {
    BigDecimal electric =
        BigDecimal.valueOf(reading.newElectricity() - reading.oldElectricity())
            .multiply(price.electricity());
    BigDecimal water =
        BigDecimal.valueOf(reading.newWater() - reading.oldWater()).multiply(price.water());
    BigDecimal extraTotal =
        extras.stream().map(Extra::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal total = price.room().add(electric).add(water).add(extraTotal).subtract(discount);
    int id =
        jdbc.queryForObject(
            """
INSERT INTO hoa_don(maphong,kythang,tienphong,tiendien,tiennuoc,tiendichvukhac,giamgia,tongtien,hanthanhtoan,trangthai)
VALUES(?,?,?,?,?,?,?,?,?,'CHUA_TT') RETURNING mahoadon
""",
            Integer.class,
            roomId,
            month,
            price.room(),
            electric,
            water,
            extraTotal,
            discount,
            total,
            dueDate);
    jdbc.update(
        "INSERT INTO chi_tiet_hoa_don(mahoadon,tenmuc,soluong,dongia,thanhtien) VALUES(?, 'Tien"
            + " phong',1,?,?)",
        id,
        price.room(),
        price.room());
    jdbc.update(
        "INSERT INTO chi_tiet_hoa_don(mahoadon,tenmuc,soluong,dongia,thanhtien) VALUES(?, 'Tien"
            + " dien',?,?,?)",
        id,
        reading.newElectricity() - reading.oldElectricity(),
        price.electricity(),
        electric);
    jdbc.update(
        "INSERT INTO chi_tiet_hoa_don(mahoadon,tenmuc,soluong,dongia,thanhtien) VALUES(?, 'Tien"
            + " nuoc',?,?,?)",
        id,
        reading.newWater() - reading.oldWater(),
        price.water(),
        water);
    for (Extra extra : extras)
      jdbc.update(
          "INSERT INTO chi_tiet_hoa_don(mahoadon,tenmuc,soluong,dongia,thanhtien)"
              + " VALUES(?,?,1,?,?)",
          id,
          extra.name(),
          extra.amount(),
          extra.amount());
    return new Invoice(id, roomId, month, total, BigDecimal.ZERO, "CHUA_TT");
  }

  public void snapshot(int invoiceId, String hash, String input) {
    jdbc.update(
        "INSERT INTO svc_invoice_snapshot(invoice_id,input_hash,inputs) VALUES(?,?,?::jsonb)",
        invoiceId,
        hash,
        input);
  }

  public List<Integer> invoiceIds(String roomId, String month) {
    return jdbc.queryForList(
        "SELECT mahoadon FROM hoa_don WHERE maphong=? AND kythang=?", Integer.class, roomId, month);
  }

  public void lockRoom(String roomId) {
    jdbc.queryForList("SELECT maphong FROM phong WHERE maphong=? FOR UPDATE", roomId);
  }
}
