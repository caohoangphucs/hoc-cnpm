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

  /** Khởi tạo repository đọc và cập nhật hồ sơ đăng ký chỗ ở. */
  public BookingRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Khóa sinh viên theo tài khoản và trả thông tin dùng để tạo đăng ký. */
  public Student lockStudent(int accountId) {
    var rows =
        jdbc.query(
            "SELECT masinhvien,gioitinh,mataikhoan FROM sinh_vien WHERE mataikhoan=? FOR UPDATE",
            (rs, n) -> new Student(rs.getString(1), rs.getString(2), rs.getInt(3)),
            accountId);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Khóa bản ghi sinh viên để tuần tự hóa thay đổi chỗ ở của sinh viên đó. */
  public void lockStudent(String id) {
    jdbc.queryForList("SELECT masinhvien FROM sinh_vien WHERE masinhvien=? FOR UPDATE", id);
  }

  /** Đọc thời gian đăng ký cùng trạng thái học kỳ tương ứng. */
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

  /** Đọc và khóa các yêu cầu đang chờ của một sinh viên theo thứ tự ID. */
  public List<Booking> pending(String studentId) {
    return jdbc.query(
        "SELECT * FROM dang_ky_phong WHERE masinhvien=? AND trangthai='CHO_DUYET' ORDER BY madangky"
            + " FOR UPDATE",
        (rs, n) -> booking(rs),
        studentId);
  }

  /** Đọc một yêu cầu đăng ký; có thể khóa bản ghi để cập nhật. */
  public Booking booking(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM dang_ky_phong WHERE madangky=?" + (lock ? " FOR UPDATE" : ""),
            (rs, n) -> booking(rs),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Ánh xạ một dòng đăng ký từ SQL sang mô hình dùng trong service. */
  private Booking booking(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new Booking(
        rs.getInt("madangky"),
        rs.getString("masinhvien"),
        rs.getString("magiuong"),
        rs.getObject("ngayhethan", LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
        rs.getString("trangthai"),
        rs.getLong("version"));
  }

  /** Khóa giường và phòng liên quan để kiểm tra hoặc đổi trạng thái an toàn. */
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

  /** Kiểm tra sinh viên đã có lưu trú đang hoạt động hay chưa. */
  public boolean hasResidency(String studentId) {
    return !jdbc.queryForList(
            "SELECT maluutru FROM luu_tru WHERE masinhvien=? AND trangthai='DANG_O'", studentId)
        .isEmpty();
  }

  /** Tạo yêu cầu giữ chỗ chờ duyệt và trả về mã đăng ký mới. */
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

  /** Chuyển thời điểm UTC sang giờ địa phương lưu trong các cột timestamp cũ. */
  private LocalDateTime local(Instant value) {
    return LocalDateTime.ofInstant(value, ServiceContext.ZONE);
  }

  /** Cập nhật trạng thái giường và tăng version để phát hiện dữ liệu cũ. */
  public void bedStatus(String id, String status) {
    jdbc.update("UPDATE giuong SET trangthai=?,version=version+1 WHERE magiuong=?", status, id);
  }

  /** Cập nhật trạng thái, lý do và version của yêu cầu đăng ký. */
  public void status(int id, String status, String reason) {
    jdbc.update(
        "UPDATE dang_ky_phong SET trangthai=?,ghichulydo=?,version=version+1 WHERE madangky=?",
        status,
        reason,
        id);
  }

  /** Lưu hồ sơ file đã nộp cho yêu cầu đăng ký. */
  public void profile(int id, int windowId, String fileIds, Instant acceptedAt) {
    jdbc.update(
        "INSERT INTO svc_profile(booking_id,window_id,file_ids,accepted_at,state)"
            + " VALUES(?,?,?::jsonb,?,'VALID')",
        id,
        windowId,
        fileIds,
        java.sql.Timestamp.from(acceptedAt));
  }

  /** Đọc trạng thái thẩm định hồ sơ của yêu cầu đăng ký. */
  public String profileState(int id) {
    var rows =
        jdbc.queryForList("SELECT state FROM svc_profile WHERE booking_id=?", String.class, id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Đọc kỳ đăng ký đã gắn với hồ sơ để lập lưu trú và hợp đồng. */
  public Window bookingWindow(int id) {
    var rows =
        jdbc.queryForList(
            "SELECT window_id FROM svc_profile WHERE booking_id=?", Integer.class, id);
    return rows.isEmpty() ? null : window(rows.get(0));
  }

  /** Ghi nhận tài khoản quản trị đã duyệt hồ sơ. */
  public void approver(int id, int actorId) {
    jdbc.update("UPDATE svc_profile SET approved_by=? WHERE booking_id=?", actorId, id);
  }

  /** Tạo bản ghi lưu trú đang hoạt động từ yêu cầu đã được duyệt. */
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

  /** Tạo hợp đồng cho lưu trú trong học kỳ đã chọn. */
  public void createContract(int residencyId, long semesterId, LocalDate start, LocalDate end) {
    jdbc.update(
        "INSERT INTO svc_contract(residency_id,semester_id,start_date,end_date) VALUES(?,?,?,?)",
        residencyId,
        semesterId,
        start,
        end);
  }

  /** Cập nhật phòng hiện tại và trạng thái của sinh viên sau khi duyệt. */
  public void studentRoom(String studentId, String roomId) {
    jdbc.update(
        "UPDATE sinh_vien SET maphonghientai=?,trangthai='DANG_O' WHERE masinhvien=?",
        roomId,
        studentId);
  }

  /** Đếm số giường đang có người ở trong phòng. */
  public long occupiedBeds(String roomId) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM giuong WHERE maphong=? AND trangthai='DANG_O'", Long.class, roomId);
  }

  /** Đọc sức chứa tối đa của phòng. */
  public int capacity(String roomId) {
    return jdbc.queryForObject("SELECT succhua FROM phong WHERE maphong=?", Integer.class, roomId);
  }

  /** Cập nhật số giường đang dùng và trạng thái còn chỗ của phòng. */
  public void roomOccupancy(String id, int occupied, String status) {
    jdbc.update("UPDATE phong SET dango=?,trangthai=? WHERE maphong=?", occupied, status, id);
  }

  /** Liệt kê các yêu cầu giữ chỗ hết hạn theo thứ tự xử lý ổn định. */
  public List<Integer> expired(Instant now, int limit) {
    return jdbc.queryForList(
        "SELECT madangky FROM dang_ky_phong WHERE trangthai='CHO_DUYET' AND ngayhethan<=? ORDER BY"
            + " madangky LIMIT ?",
        Integer.class,
        local(now),
        limit);
  }
}
