package vn.id.caohoangphuc.cnpm.persistence.service;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL đơn giản cho metadata, quyền đọc và nội dung ảnh đã mã hóa. */
@Repository
public class FileRepository {
    public record StoredFile(UUID id, int ownerId, String kind, String mime, int size,
                             String sha256, byte[] nonce, byte[] encrypted) {}
    private final JdbcTemplate jdbc;

    /** Nhận kết nối database dùng chung transaction với service. */
    public FileRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lưu metadata nháp và ciphertext trong cùng transaction của service. */
    public void insert(UUID id, int owner, String kind, String mime, byte[] image,
                       String hash, byte[] nonce, byte[] encrypted) {
        jdbc.update("""
            INSERT INTO svc_file(id,owner_id,kind,object_key,mime,size_bytes,sha256,key_id,
                                 nonce,tag,state,scanned_at,expires_at)
            VALUES (?,?,?,?,?,?,?,'database-aes-gcm-v1',?,?,'DRAFT',now(),now()+interval '24 hours')
            """, id, owner, kind, "uploads/" + id, mime, image.length, hash, nonce,
                java.util.Arrays.copyOfRange(encrypted, encrypted.length - 16, encrypted.length));
        jdbc.update("INSERT INTO svc_file_content(file_id,encrypted_bytes) VALUES (?,?)", id, encrypted);
    }

    /** Chỉ đọc nội dung khi metadata và ciphertext đều tồn tại. */
    public StoredFile find(UUID id) {
        var rows = jdbc.query("""
            SELECT f.id,f.owner_id,f.kind,f.mime,f.size_bytes,f.sha256,f.nonce,c.encrypted_bytes
            FROM svc_file f JOIN svc_file_content c ON c.file_id=f.id WHERE f.id=?
            """, (r,n) -> new StoredFile(r.getObject(1,UUID.class),r.getInt(2),r.getString(3),
                r.getString(4),r.getInt(5),r.getString(6),r.getBytes(7),r.getBytes(8)), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Admin được đọc minh chứng đã gắn vào hồ sơ đăng ký của đúng chủ tệp. */
    public boolean attachedToBooking(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM svc_profile p JOIN dang_ky_phong b ON b.madangky=p.booking_id
                JOIN sinh_vien s ON s.masinhvien=b.masinhvien
                JOIN svc_file f ON f.id=? AND f.owner_id=s.mataikhoan
                WHERE p.file_ids @> jsonb_build_array(CAST(? AS text)) AND f.state='ATTACHED'
            )
            """, Boolean.class, id, id.toString()));
    }

    /** Quyền đọc ảnh sửa chữa theo chủ phiếu, admin hoặc kỹ thuật viên được giao. */
    public boolean readableRepair(UUID id, int accountId, String role) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM svc_repair_files p JOIN yeu_cau_sua_chua r ON r.mayeucau=p.repair_id
                JOIN sinh_vien s ON s.masinhvien=r.masinhvien
                JOIN svc_file f ON f.id=? AND f.state='ATTACHED'
                LEFT JOIN svc_repair_assignment a ON a.repair_id=r.mayeucau
                WHERE p.file_ids @> jsonb_build_array(CAST(? AS text))
                  AND (?='ADMIN' OR s.mataikhoan=? OR (?='KYTHUAT' AND a.technician_id=?))
            )
            """, Boolean.class, id,id.toString(),role,accountId,role,accountId));
    }

    /** Chỉ admin hoặc sinh viên trong quyết định được đọc minh chứng đã gắn. */
    public boolean readableDiscipline(UUID id, int accountId, String role) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM svc_discipline_decision d
                JOIN vi_pham v ON v.mavipham=d.violation_id
                JOIN sinh_vien s ON s.masinhvien=v.masinhvien
                JOIN svc_file f ON f.id=d.evidence_id AND f.state='ATTACHED'
                WHERE f.id=? AND (?='ADMIN' OR (?='SINHVIEN' AND s.mataikhoan=?))
            )
            """, Boolean.class, id,role,role,accountId));
    }

    public record RegistrationWindow(int id, String name, String semesterName,
                                     java.time.OffsetDateTime opensAt, java.time.OffsetDateTime closesAt) {}

    /** Liệt kê đợt hiện còn mở và học kỳ chưa kết thúc cho form đăng ký. */
    public List<RegistrationWindow> windows() {
        return jdbc.query("""
            SELECT d.madot AS "id",d.tendot AS "name",h.tenhocky AS "semesterName",
                   d.thoigianmo AS "opensAt",d.thoigiandong AS "closesAt"
            FROM dot_dang_ky d JOIN hoc_ky h ON h.mahocky=d.mahocky
            WHERE d.trangthai='ACTIVE' AND h.trangthai='ACTIVE'
              AND d.thoigianmo<=localtimestamp AND d.thoigiandong>localtimestamp
              AND h.ngayketthuc>=current_date ORDER BY d.thoigianmo DESC
            """, (r,n) -> new RegistrationWindow(r.getInt(1),r.getString(2),r.getString(3),
                r.getObject(4,java.time.LocalDateTime.class).atZone(vn.id.caohoangphuc.cnpm.service.ServiceContext.ZONE).toOffsetDateTime(),
                r.getObject(5,java.time.LocalDateTime.class).atZone(vn.id.caohoangphuc.cnpm.service.ServiceContext.ZONE).toOffsetDateTime()));
    }

    /** Chọn đúng các tham chiếu seed được biết trước, chưa có nội dung thật. */
    public List<java.util.Map<String,Object>> missingDemoFiles() {
        return jdbc.queryForList("""
            SELECT f.id,f.kind FROM svc_file f JOIN tai_khoan a ON a.mataikhoan=f.owner_id
            WHERE NOT EXISTS (SELECT 1 FROM svc_file_content c WHERE c.file_id=f.id)
              AND ((f.object_key LIKE 'demo/profile/DEMO-%/%' AND a.tendangnhap LIKE 'demo_%')
                   OR (f.object_key='demo/discipline/evidence.png' AND a.tendangnhap='admin'))
              AND f.kind IN ('CCCD_FRONT','CCCD_BACK','STUDENT_CARD','EVIDENCE')
            """);
    }

    /** Bổ sung ciphertext vào seed mà không thay trạng thái hoặc người sở hữu hồ sơ. */
    public void seedContent(UUID id, byte[] image, String hash, byte[] nonce, byte[] encrypted) {
        jdbc.update("""
            WITH inserted AS (
                INSERT INTO svc_file_content(file_id,encrypted_bytes) VALUES (?,?)
                ON CONFLICT DO NOTHING RETURNING file_id
            )
            UPDATE svc_file f SET mime='image/png',size_bytes=?,sha256=?,
                key_id='database-aes-gcm-v1',nonce=?,tag=? FROM inserted i WHERE f.id=i.file_id
            """, id, encrypted, image.length, hash, nonce,
                java.util.Arrays.copyOfRange(encrypted,encrypted.length-16,encrypted.length));
    }
}
