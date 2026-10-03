package vn.id.caohoangphuc.cnpm.persistence.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.service.ServiceContext;

public class CommunityRepository {
  public record Activity(
      String id,
      long publicId,
      long semesterId,
      String title,
      Instant start,
      Instant end,
      int capacity,
      int registered,
      int points,
      String status,
      long version) {}

  public record Registration(
      int id, String activityId, String studentId, String status, String token, long version) {}

  public record Ledger(
      long id, String studentId, String semesterId, int delta, String type, Instant at) {}

  public record Conversion(
      int id, String studentId, String semesterId, int spent, int drl, String status) {}

  private final JdbcTemplate jdbc;

  /** Khởi tạo repository cho hoạt động, đăng ký và sổ điểm công ích. */
  public CommunityRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Ánh xạ một dòng hoạt động và học kỳ sang mô hình service. */
  private Activity activityRow(java.sql.ResultSet r) throws java.sql.SQLException {
    return new Activity(
        r.getString("mahoatdong"),
        r.getLong("public_id"),
        r.getLong("semester_id"),
        r.getString("tieude"),
        r.getObject("thoigianbatdau", LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
        r.getObject("thoigianketthuc", LocalDateTime.class).atZone(ServiceContext.ZONE).toInstant(),
        r.getInt("chitieu"),
        r.getInt("dadangky"),
        r.getInt("diemcongich"),
        r.getString("trangthai"),
        r.getLong("version"));
  }

  private final String activitySelect =
      """
      SELECT a.*,h.public_id semester_id FROM hoat_dong a JOIN hoc_ky h USING(mahocky)
      """;

  /** Liệt kê các hoạt động đang mở theo trang và thời gian bắt đầu. */
  public List<Activity> activities(int page, int size) {
    return jdbc.query(
        activitySelect
            + " WHERE a.trangthai='MO_DANG_KY' ORDER BY a.thoigianbatdau LIMIT ? OFFSET ?",
        (r, n) -> activityRow(r),
        size,
        (page - 1) * size);
  }

  /** Tìm mã học kỳ nội bộ từ mã công khai hoặc báo không tìm thấy. */
  public String semesterCode(long id) {
    var rows = jdbc.queryForList("SELECT mahocky FROM hoc_ky WHERE public_id=?", String.class, id);
    if (rows.isEmpty()) throw vn.id.caohoangphuc.cnpm.service.ServiceException.notFound("Semester");
    return rows.get(0);
  }

  /** Tạo hoạt động mới trong học kỳ và trả lại dòng vừa được tạo. */
  public Activity create(
      long semesterId, String title, Instant start, Instant end, int capacity, int points) {
    String code = semesterCode(semesterId);
    String id =
        "A"
            + java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(random(12))
                .substring(0, 16);
    long publicId =
        jdbc.queryForObject(
            "INSERT INTO"
                + " hoat_dong(mahoatdong,mahocky,tieude,thoigianbatdau,thoigianketthuc,chitieu,diemcongich,trangthai)"
                + " VALUES(?,?,?,?,?,?,?,'MO_DANG_KY') RETURNING public_id",
            Long.class,
            id,
            code,
            title,
            local(start),
            local(end),
            capacity,
            points);
    return activity(publicId, false);
  }

  /** Sinh các byte khó đoán dùng cho mã hoạt động nội bộ. */
  private byte[] random(int size) {
    byte[] result = new byte[size];
    new java.security.SecureRandom().nextBytes(result);
    return result;
  }

  /** Đọc hoạt động theo mã nội bộ; tùy chọn khóa dòng để cập nhật. */
  public Activity activityByCode(String code, boolean lock) {
    var rows =
        jdbc.query(
            activitySelect + " WHERE a.mahoatdong=?" + (lock ? " FOR UPDATE OF a" : ""),
            (r, n) -> activityRow(r),
            code);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Đọc hoạt động theo mã công khai; tùy chọn khóa dòng để cập nhật. */
  public Activity activity(long id, boolean lock) {
    var rows =
        jdbc.query(
            activitySelect + " WHERE a.public_id=?" + (lock ? " FOR UPDATE OF a" : ""),
            (r, n) -> activityRow(r),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Updates the editable fields of an activity that has not collected registrations. */
  public void updateActivity(long id, long semesterId, String title, Instant start, Instant end,
      int capacity, int points) {
    jdbc.update("""
        UPDATE hoat_dong SET mahocky=?,tieude=?,thoigianbatdau=?,thoigianketthuc=?,chitieu=?,diemcongich=?,version=version+1
        WHERE public_id=?
        """, semesterCode(semesterId), title, local(start), local(end), capacity, points, id);
  }

  /** Deletes an activity only after its registration history has been removed. */
  public boolean deleteActivity(long id) {
    return jdbc.update("DELETE FROM hoat_dong WHERE public_id=?", id) == 1;
  }

  /** Checks for any registration history before an activity is edited or deleted. */
  public boolean hasRegistrationHistory(String activityCode) {
    Integer count = jdbc.queryForObject(
        "SELECT count(*) FROM dang_ky_cong_ich WHERE mahoatdong=?", Integer.class, activityCode);
    return count != null && count > 0;
  }

  /** Đếm các đăng ký đang hiệu lực có thời gian trùng với hoạt động mới. */
  public int overlaps(String student, Instant start, Instant end) {
    return jdbc.queryForObject(
        """
        SELECT count(*) FROM dang_ky_cong_ich d JOIN hoat_dong a USING(mahoatdong)
        WHERE d.masinhvien=? AND d.trangthai IN ('REGISTERED','ATTENDED')
        AND a.thoigianbatdau<? AND ?<a.thoigianketthuc
        """,
        Integer.class,
        student,
        local(end),
        local(start));
  }

  /** Kiểm tra sinh viên đã đăng ký hoạt động này và chưa hủy hay chưa. */
  public boolean registered(String student, String activity) {
    return !jdbc.queryForList(
            "SELECT madangkycongich FROM dang_ky_cong_ich WHERE masinhvien=? AND mahoatdong=? AND"
                + " trangthai<>'CANCELLED'",
            Integer.class,
            student,
            activity)
        .isEmpty();
  }

  /** Đọc một đăng ký theo ID; có thể khóa dòng trước khi đổi trạng thái. */
  public Registration registration(int id, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM dang_ky_cong_ich WHERE madangkycongich=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Registration(
                    r.getInt("madangkycongich"),
                    r.getString("mahoatdong"),
                    r.getString("masinhvien"),
                    r.getString("trangthai"),
                    r.getString("maqrtoken"),
                    r.getLong("version")),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Tìm đăng ký qua hash QR và tùy chọn khóa trước khi điểm danh. */
  public Registration registrationByToken(String hash, boolean lock) {
    var rows =
        jdbc.query(
            "SELECT * FROM dang_ky_cong_ich WHERE maqrtoken=?" + (lock ? " FOR UPDATE" : ""),
            (r, n) ->
                new Registration(
                    r.getInt("madangkycongich"),
                    r.getString("mahoatdong"),
                    r.getString("masinhvien"),
                    r.getString("trangthai"),
                    r.getString("maqrtoken"),
                    r.getLong("version")),
            "sha256:" + hash);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** Khóa sinh viên để tuần tự hóa đăng ký, hủy và cộng điểm. */
  public void lockStudent(String id) {
    jdbc.queryForList("SELECT masinhvien FROM sinh_vien WHERE masinhvien=? FOR UPDATE", id);
  }

  /** Tạo đăng ký, lưu hash QR và tăng số chỗ đã đăng ký của hoạt động. */
  public int register(String activity, String student, String token) {
    int id =
        jdbc.queryForObject(
            "INSERT INTO dang_ky_cong_ich(mahoatdong,masinhvien,maqrtoken) VALUES(?,?,?) RETURNING"
                + " madangkycongich",
            Integer.class,
            activity,
            student,
            "sha256:" + ServiceContext.hash(token));
    jdbc.update(
        "UPDATE hoat_dong SET dadangky=dadangky+1,version=version+1 WHERE mahoatdong=?", activity);
    return id;
  }

  /** Cập nhật trạng thái đăng ký và tăng version của dòng. */
  public void state(int id, String state) {
    jdbc.update(
        "UPDATE dang_ky_cong_ich SET trangthai=?,version=version+1 WHERE madangkycongich=?",
        state,
        id);
  }

  /** Điều chỉnh số chỗ đã đăng ký và tăng version hoạt động. */
  public void capacity(String activity, int delta) {
    jdbc.update(
        "UPDATE hoat_dong SET dadangky=dadangky+?,version=version+1 WHERE mahoatdong=?",
        delta,
        activity);
  }

  /** Hoàn tất đăng ký và cộng điểm đúng một lần nhờ khóa duy nhất theo đăng ký. */
  public int confirm(int id, int admin, Instant at) {
    jdbc.update(
        "UPDATE dang_ky_cong_ich SET"
            + " trangthai='COMPLETED',nguoixacnhan=?,ngayxacnhan=?,version=version+1 WHERE"
            + " madangkycongich=?",
        Integer.toString(admin),
        java.sql.Timestamp.from(at),
        id);
    return jdbc.update(
        "INSERT INTO"
            + " so_diem_cong_ich(masinhvien,mahocky,madangkycongich,sodiemthaydoi,loaigiaodich,ghichu)"
            + " SELECT d.masinhvien,a.mahocky,d.madangkycongich,a.diemcongich,'CONG_DIEM','Activity"
            + " completed' FROM dang_ky_cong_ich d JOIN hoat_dong a USING(mahoatdong) WHERE"
            + " d.madangkycongich=? ON CONFLICT(madangkycongich) WHERE loaigiaodich='CONG_DIEM' DO"
            + " NOTHING",
        id);
  }

  /** Đọc sổ biến động điểm theo sinh viên và học kỳ. */
  public List<Ledger> ledger(String student, String semester) {
    return jdbc.query(
        "SELECT magiaodich,masinhvien,mahocky,sodiemthaydoi,loaigiaodich,thoigian FROM"
            + " so_diem_cong_ich WHERE masinhvien=? AND mahocky=? ORDER BY thoigian",
        (r, n) ->
            new Ledger(
                r.getLong(1),
                r.getString(2),
                r.getString(3),
                r.getInt(4),
                r.getString(5),
                r.getTimestamp(6).toInstant()),
        student,
        semester);
  }

  /** Tính tổng điểm rèn luyện đã nhận hoặc đang chờ trong học kỳ. */
  public int conversionDrl(String student, String semester) {
    return jdbc.queryForObject(
        "SELECT coalesce(sum(diemrenluyennhan),0) FROM quy_doi_diem q JOIN hoc_ky h USING(mahocky)"
            + " WHERE q.masinhvien=? AND h.public_id=? AND q.trangthai IN ('PENDING','THANH_CONG')",
        Integer.class,
        student,
        Long.parseLong(semester));
  }

  /** Tạo yêu cầu quy đổi trạng thái chờ và liên kết mã giao dịch service. */
  public int createConversion(UUID transfer, String student, String semester, int spent, int drl) {
    int id =
        jdbc.queryForObject(
            "INSERT INTO"
                + " quy_doi_diem(masinhvien,mahocky,diemcongichtieu,diemrenluyennhan,trangthai)"
                + " SELECT ?,mahocky,?,?, 'PENDING' FROM hoc_ky WHERE public_id=? RETURNING"
                + " maquydoi",
            Integer.class,
            student,
            spent,
            drl,
            Long.parseLong(semester));
    jdbc.update("INSERT INTO svc_conversion(id,legacy_id) VALUES(?,?)", transfer, id);
    return id;
  }

  /** Ghi bút toán trừ điểm công ích cho một yêu cầu quy đổi. */
  public void debit(String student, String semester, int points, int conversion) {
    jdbc.update(
        "INSERT INTO so_diem_cong_ich(masinhvien,mahocky,sodiemthaydoi,loaigiaodich,ghichu) SELECT"
            + " ?,mahocky,?,'QUY_DOI',? FROM hoc_ky WHERE public_id=?",
        student,
        -points,
        "conversion:" + conversion,
        Long.parseLong(semester));
  }

  /** Lưu bản nháp chứng nhận và hash token xác minh. */
  public void certificate(
      UUID id, int account, String student, String semester, String snapshot, String tokenHash) {
    jdbc.update(
        "INSERT INTO svc_certificate(id,actor_id,student_id,semester_id,snapshot,token_hash)"
            + " VALUES(?,?,?,?,?::jsonb,?)",
        id,
        account,
        student,
        Long.parseLong(semester),
        snapshot,
        tokenHash);
  }

  /** Tạo JSON snapshot các hoạt động hoàn thành tại thời điểm yêu cầu chứng nhận. */
  public String completedSnapshot(String student, String semester) {
    return jdbc.queryForObject(
        """
SELECT coalesce(jsonb_agg(jsonb_build_object('activityId',d.madangkycongich,'title',a.tieude,
  'completedAt',d.ngayxacnhan,'points',a.diemcongich) ORDER BY d.ngayxacnhan),'[]')::text
FROM dang_ky_cong_ich d JOIN hoat_dong a USING(mahoatdong) JOIN hoc_ky h USING(mahocky)
WHERE d.masinhvien=? AND h.public_id=? AND d.trangthai='COMPLETED'
""",
        String.class,
        student,
        Long.parseLong(semester));
  }

  /** Đọc mã học kỳ nội bộ từ mã công khai của học kỳ. */
  public String studentSemester(String student, long semester) {
    return semesterCode(semester);
  }

  /** Chuyển thời điểm UTC sang giờ địa phương dùng trong các cột timestamp cũ. */
  private LocalDateTime local(Instant value) {
    return LocalDateTime.ofInstant(value, ServiceContext.ZONE);
  }
}
