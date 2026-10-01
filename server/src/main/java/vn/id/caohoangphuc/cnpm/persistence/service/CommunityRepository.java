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

  public CommunityRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

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

  public List<Activity> activities(int page, int size) {
    return jdbc.query(
        activitySelect
            + " WHERE a.trangthai='MO_DANG_KY' ORDER BY a.thoigianbatdau LIMIT ? OFFSET ?",
        (r, n) -> activityRow(r),
        size,
        (page - 1) * size);
  }

  public String semesterCode(long id) {
    var rows = jdbc.queryForList("SELECT mahocky FROM hoc_ky WHERE public_id=?", String.class, id);
    if (rows.isEmpty()) throw vn.id.caohoangphuc.cnpm.service.ServiceException.notFound("Semester");
    return rows.get(0);
  }

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

  private byte[] random(int size) {
    byte[] result = new byte[size];
    new java.security.SecureRandom().nextBytes(result);
    return result;
  }

  public Activity activityByCode(String code, boolean lock) {
    var rows =
        jdbc.query(
            activitySelect + " WHERE a.mahoatdong=?" + (lock ? " FOR UPDATE OF a" : ""),
            (r, n) -> activityRow(r),
            code);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public Activity activity(long id, boolean lock) {
    var rows =
        jdbc.query(
            activitySelect + " WHERE a.public_id=?" + (lock ? " FOR UPDATE OF a" : ""),
            (r, n) -> activityRow(r),
            id);
    return rows.isEmpty() ? null : rows.get(0);
  }

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

  public boolean registered(String student, String activity) {
    return !jdbc.queryForList(
            "SELECT madangkycongich FROM dang_ky_cong_ich WHERE masinhvien=? AND mahoatdong=? AND"
                + " trangthai<>'CANCELLED'",
            Integer.class,
            student,
            activity)
        .isEmpty();
  }

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

  public void lockStudent(String id) {
    jdbc.queryForList("SELECT masinhvien FROM sinh_vien WHERE masinhvien=? FOR UPDATE", id);
  }

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

  public void state(int id, String state) {
    jdbc.update(
        "UPDATE dang_ky_cong_ich SET trangthai=?,version=version+1 WHERE madangkycongich=?",
        state,
        id);
  }

  public void capacity(String activity, int delta) {
    jdbc.update(
        "UPDATE hoat_dong SET dadangky=dadangky+?,version=version+1 WHERE mahoatdong=?",
        delta,
        activity);
  }

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

  public int conversionDrl(String student, String semester) {
    return jdbc.queryForObject(
        "SELECT coalesce(sum(diemrenluyennhan),0) FROM quy_doi_diem q JOIN hoc_ky h USING(mahocky)"
            + " WHERE q.masinhvien=? AND h.public_id=? AND q.trangthai='THANH_CONG'",
        Integer.class,
        student,
        Long.parseLong(semester));
  }

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

  public void debit(String student, String semester, int points, int conversion) {
    jdbc.update(
        "INSERT INTO so_diem_cong_ich(masinhvien,mahocky,sodiemthaydoi,loaigiaodich,ghichu) SELECT"
            + " ?,mahocky,?,'QUY_DOI',? FROM hoc_ky WHERE public_id=?",
        student,
        -points,
        "conversion:" + conversion,
        Long.parseLong(semester));
  }

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

  public String studentSemester(String student, long semester) {
    var rows =
        jdbc.queryForList("SELECT mahocky FROM hoc_ky WHERE public_id=?", String.class, semester);
    if (rows.isEmpty()) throw vn.id.caohoangphuc.cnpm.service.ServiceException.notFound("Semester");
    return rows.get(0);
  }

  private LocalDateTime local(Instant value) {
    return LocalDateTime.ofInstant(value, ServiceContext.ZONE);
  }
}
