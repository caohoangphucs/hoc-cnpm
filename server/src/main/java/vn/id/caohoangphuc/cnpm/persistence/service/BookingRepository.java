package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.service.ServiceContext;

public class BookingRepository {
  public record Student(String id, String gender, int accountId) {}

  public record Window(
      int id,
      long semesterId,
      Instant opensAt,
      Instant closesAt,
      String status,
      String semesterStatus,
      LocalDate startsOn,
      LocalDate endsOn) {}

  public record Bed(String id, String roomId, String status, String gender, String roomStatus) {}

  public record Booking(
      int id, String studentId, String bedId, Instant expiresAt, String status, long version) {}

  private final JdbcTemplate jdbc;

  public BookingRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Student lockStudent(int accountId) {
    var rows =
        jdbc.query(
            "SELECT masinhvien,gioitinh,mataikhoan FROM sinh_vien WHERE mataikhoan=? FOR UPDATE",
            (rs, n) -> new Student(rs.getString(1), rs.getString(2), rs.getInt(3)),
            accountId);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public void lockStudent(String id) {
    jdbc.queryForList("SELECT masinhvien FROM sinh_vien WHERE masinhvien=? FOR UPDATE", id);
  }

  public Window window(int id) {
    var rows =
        jdbc.query(
            """
SELECT d.madot,h.public_id,d.thoigianmo,d.thoigiandong,d.trangthai,h.trangthai,h.ngaybatdau,h.ngayketthuc
FROM dot_dang_ky d JOIN hoc_ky h USING(mahocky) WHERE d.madot=?
""",
            (rs, n) ->
                new Window(
                    rs.getInt(1),
                    rs.getLong(2),
                    rs.getObject(3, LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
                    rs.getObject(4, LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getObject(7, LocalDate.class),
                    rs.getObject(8, LocalDate.class)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public List<Booking> pending(String studentId) {
    return jdbc.query(
        "SELECT * FROM dang_ky_phong WHERE masinhvien=? AND trangthai='CHO_DUYET' ORDER BY madangky"
            + " FOR UPDATE",
        (rs, n) -> booking(rs),
        studentId);
  }

  public Booking booking(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM dang_ky_phong WHERE madangky=?" + (lock ? " FOR UPDATE" : ""),
            (rs, n) -> booking(rs),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  private Booking booking(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new Booking(
        rs.getInt("madangky"),
        rs.getString("masinhvien"),
        rs.getString("magiuong"),
        rs.getObject("ngayhethan", LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
        rs.getString("trangthai"),
        rs.getLong("version"));
  }

  public Bed lockBed(String id) {
    var rows =
        jdbc.query(
            """
SELECT g.magiuong,g.maphong,g.trangthai,t.gioitinhapdung,p.trangthai
FROM giuong g JOIN phong p USING(maphong) JOIN toa_nha t USING(matoanha) WHERE g.magiuong=? FOR UPDATE OF g,p
""",
            (rs, n) ->
                new Bed(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5)),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public boolean hasResidency(String studentId) {
    return !jdbc.queryForList(
            "SELECT maluutru FROM luu_tru WHERE masinhvien=? AND trangthai='DANG_O'", studentId)
        .isEmpty();
  }

  public int create(String studentId, String bedId, Instant createdAt, Instant expiresAt) {
    return jdbc.queryForObject(
        """
        INSERT INTO dang_ky_phong(masinhvien,magiuong,ngaydangky,ngayhethan,trangthai)
        VALUES(?,?,?,?,'CHO_DUYET') RETURNING madangky
        """,
        Integer.class,
        studentId,
        bedId,
        local(createdAt),
        local(expiresAt));
  }

  private LocalDateTime local(Instant value) {
    return LocalDateTime.ofInstant(value, ServiceContext.ZONE);
  }

  public void bedStatus(String id, String status) {
    jdbc.update("UPDATE giuong SET trangthai=?,version=version+1 WHERE magiuong=?", status, id);
  }

  public void status(int id, String status, String reason) {
    jdbc.update(
        "UPDATE dang_ky_phong SET trangthai=?,ghichulydo=?,version=version+1 WHERE madangky=?",
        status,
        reason,
        id);
  }

  public void profile(int id, int windowId, String fileIds, Instant acceptedAt) {
    jdbc.update(
        "INSERT INTO svc_profile(booking_id,window_id,file_ids,accepted_at,state)"
            + " VALUES(?,?,?::jsonb,?,'VALID')",
        id,
        windowId,
        fileIds,
        java.sql.Timestamp.from(acceptedAt));
  }

  public String profileState(int id) {
    var rows =
        jdbc.queryForList("SELECT state FROM svc_profile WHERE booking_id=?", String.class, id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public Window bookingWindow(int id) {
    var rows =
        jdbc.queryForList(
            "SELECT window_id FROM svc_profile WHERE booking_id=?", Integer.class, id);
    return rows.isEmpty() ? null : window(rows.get(0));
  }

  public void approver(int id, int actorId) {
    jdbc.update("UPDATE svc_profile SET approved_by=? WHERE booking_id=?", actorId, id);
  }

  public int createResidency(Booking booking, LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        """
        INSERT INTO luu_tru(madangky,masinhvien,magiuong,ngaybatdau,ngayketthuc,trangthai)
        VALUES(?,?,?,?,?,'DANG_O') RETURNING maluutru
        """,
        Integer.class,
        booking.id(),
        booking.studentId(),
        booking.bedId(),
        start,
        end);
  }

  public void createContract(int residencyId, long semesterId, LocalDate start, LocalDate end) {
    jdbc.update(
        "INSERT INTO svc_contract(residency_id,semester_id,start_date,end_date) VALUES(?,?,?,?)",
        residencyId,
        semesterId,
        start,
        end);
  }

  public void studentRoom(String studentId, String roomId) {
    jdbc.update(
        "UPDATE sinh_vien SET maphonghientai=?,trangthai='DANG_O' WHERE masinhvien=?",
        roomId,
        studentId);
  }

  public long occupiedBeds(String roomId) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM giuong WHERE maphong=? AND trangthai='DANG_O'", Long.class, roomId);
  }

  public int capacity(String roomId) {
    return jdbc.queryForObject("SELECT succhua FROM phong WHERE maphong=?", Integer.class, roomId);
  }

  public void roomOccupancy(String id, int occupied, String status) {
    jdbc.update("UPDATE phong SET dango=?,trangthai=? WHERE maphong=?", occupied, status, id);
  }

  public List<Integer> expired(Instant now, int limit) {
    return jdbc.queryForList(
        "SELECT madangky FROM dang_ky_phong WHERE trangthai='CHO_DUYET' AND ngayhethan<=? ORDER BY"
            + " madangky LIMIT ?",
        Integer.class,
        local(now),
        limit);
  }
}
