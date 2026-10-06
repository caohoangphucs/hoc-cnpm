package vn.id.caohoangphuc.cnpm.persistence.service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.api.dto.BedDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomWriteDto;
import vn.id.caohoangphuc.cnpm.api.dto.BedWriteDto;

/** Read-only SQL queries for rooms and bed availability. */
public class RoomCatalogRepository {
    public record RoomPage(List<RoomDto> items, long total) {}

    public record BedPage(List<BedDto> items, long total) {}

    public record BedState(String id, String roomId, String status) {}

    private final JdbcTemplate jdbc;

    /** Stores the JDBC client used for the small room-catalogue queries. */
    public RoomCatalogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Reads room details and counts only beds that can currently be booked. */
    public RoomPage rooms(int pageSize, long offset) {
        var items = jdbc.query("""
                        SELECT p.maphong,p.matoanha,COALESCE(l.tenloaiphong,p.maloaiphong,'Chưa phân loại'),
                               p.maloaiphong,COALESCE(t.gioitinhapdung,'TAT_CA'),
                               p.sophong,p.tang,p.giaphongcoban,p.trangthai,p.succhua,
                               LEAST(
                                   GREATEST(p.succhua - COUNT(g.magiuong) FILTER (WHERE g.trangthai IN ('GIU_CHO','DANG_O')),0),
                                   COUNT(g.magiuong) FILTER (
                                       WHERE g.trangthai='TRONG'
                                         AND COALESCE(p.trangthai,'TRONG') NOT IN ('KHOA','DANG_SUA','DAY')))
                        FROM phong p
                        JOIN toa_nha t USING(matoanha)
                        LEFT JOIN loai_phong l USING(maloaiphong)
                        LEFT JOIN giuong g USING(maphong)
                        GROUP BY p.maphong,p.matoanha,l.tenloaiphong,p.maloaiphong,
                                 t.gioitinhapdung,p.succhua,p.tang,p.sophong,p.giaphongcoban,p.trangthai
                        ORDER BY p.matoanha,p.tang,p.sophong
                        LIMIT ? OFFSET ?
                        """,
                (rs, row) -> new RoomDto(
                        rs.getString(1),
                        rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6), rs.getInt(7), rs.getLong(8), rs.getString(9),
                        rs.getLong(10), rs.getLong(11)),
                pageSize,
                offset);
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM phong", Long.class);
        return new RoomPage(items, total);
    }

    /** Reads one room using the same public shape as the room list. */
    public RoomDto room(String roomId) {
        var rows = jdbc.query("""
                        SELECT p.maphong,p.matoanha,COALESCE(l.tenloaiphong,p.maloaiphong,'Chưa phân loại'),
                               p.maloaiphong,COALESCE(t.gioitinhapdung,'TAT_CA'),
                               p.sophong,p.tang,p.giaphongcoban,p.trangthai,p.succhua,
                               LEAST(
                                   GREATEST(p.succhua - COUNT(g.magiuong) FILTER (WHERE g.trangthai IN ('GIU_CHO','DANG_O')),0),
                                   COUNT(g.magiuong) FILTER (
                                       WHERE g.trangthai='TRONG'
                                         AND COALESCE(p.trangthai,'TRONG') NOT IN ('KHOA','DANG_SUA','DAY')))
                        FROM phong p
                        JOIN toa_nha t USING(matoanha)
                        LEFT JOIN loai_phong l USING(maloaiphong)
                        LEFT JOIN giuong g USING(maphong)
                        WHERE p.maphong=?
                        GROUP BY p.maphong,p.matoanha,l.tenloaiphong,p.maloaiphong,
                                 t.gioitinhapdung,p.succhua,p.tang,p.sophong,p.giaphongcoban,p.trangthai
                        """,
                (rs, row) -> new RoomDto(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7),
                        rs.getLong(8), rs.getString(9), rs.getLong(10), rs.getLong(11)),
                roomId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Reads a page of beds in one room, including each bed's optimistic-lock version. */
    public BedPage beds(String roomId, int pageSize, long offset) {
        var items = jdbc.query("""
                        SELECT magiuong,sogiuong,vitri,trangthai,version
                        FROM giuong
                        WHERE maphong=?
                        ORDER BY sogiuong,magiuong
                        LIMIT ? OFFSET ?
                        """,
                (rs, row) -> new BedDto(
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getLong(5)),
                roomId,
                pageSize,
                offset);
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM giuong WHERE maphong=?", Long.class, roomId);
        return new BedPage(items, total);
    }

    /** Locks a bed and its room before changing a reservation-sensitive field. */
    public BedState lockBed(String id) {
        var rows = jdbc.query("SELECT g.magiuong,g.maphong,g.trangthai FROM giuong g "
                + "JOIN phong p USING(maphong) WHERE g.magiuong=? FOR UPDATE OF g,p",
            (rs, row) -> new BedState(rs.getString(1), rs.getString(2), rs.getString(3)), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Checks that a room exists before returning an empty bed list for it. */
    public boolean roomExists(String roomId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM phong WHERE maphong=?", Long.class, roomId);
        return count != null && count > 0;
    }

    /** Locks a room so capacity changes and bed allocation cannot race. */
    public boolean lockRoom(String roomId) {
        return !jdbc.queryForList("SELECT maphong FROM phong WHERE maphong=? FOR UPDATE", roomId).isEmpty();
    }

    /** Verifies foreign keys up front so callers receive a client error instead of an SQL failure. */
    public boolean buildingExists(String id) {
        return !jdbc.queryForList("SELECT matoanha FROM toa_nha WHERE matoanha=?", id).isEmpty();
    }

    /** Verifies an optional room type before inserting or updating a room. */
    public boolean roomTypeExists(String id) {
        return !jdbc.queryForList("SELECT maloaiphong FROM loai_phong WHERE maloaiphong=?", id).isEmpty();
    }

    /** Counts active residents and unexpired reservations when enforcing room capacity. */
    public long activeRoomUse(String roomId) {
        Long count = jdbc.queryForObject("""
            SELECT (SELECT count(*) FROM giuong WHERE maphong=? AND trangthai='DANG_O')
                 + (SELECT count(DISTINCT g.magiuong) FROM dang_ky_phong d JOIN giuong g USING(magiuong)
                    WHERE g.maphong=? AND d.trangthai='CHO_DUYET' AND d.ngayhethan>CURRENT_TIMESTAMP)
            """, Long.class, roomId, roomId);
        return count == null ? 0 : count;
    }

    /** Counts configured beds while the room row is locked. */
    public long bedCount(String roomId) {
        Long count = jdbc.queryForObject("SELECT count(*) FROM giuong WHERE maphong=?", Long.class, roomId);
        return count == null ? 0 : count;
    }

    /** Checks whether a live booking currently owns this bed. */
    public boolean bedHasPendingHold(String bedId) {
        return !jdbc.queryForList("""
            SELECT madangky FROM dang_ky_phong
            WHERE magiuong=? AND trangthai='CHO_DUYET' AND ngayhethan>CURRENT_TIMESTAMP
            """, Integer.class, bedId).isEmpty();
    }

    /** Checks whether an active residency currently owns this bed. */
    public boolean bedHasActiveResidency(String bedId) {
        return !jdbc.queryForList("SELECT maluutru FROM luu_tru WHERE magiuong=? AND trangthai='DANG_O'",
            Integer.class, bedId).isEmpty();
    }

    /** Inserts a room using fields that have already passed service validation. */
    public void createRoom(RoomWriteDto room) {
        jdbc.update("""
                INSERT INTO phong(maphong,matoanha,maloaiphong,sophong,tang,succhua,giaphongcoban,trangthai)
                VALUES(?,?,?,?,?,?,?,?)
                """, room.id(), room.buildingId(), room.roomTypeId(), room.number(), room.floor(),
                room.capacity(), room.basePrice(), room.status());
    }

    /** Updates a room's editable catalogue fields. */
    public boolean updateRoom(String id, RoomWriteDto room) {
        BigDecimal oldPrice = jdbc.queryForObject(
                "SELECT giaphongcoban FROM phong WHERE maphong=?", BigDecimal.class, id);
        if (oldPrice == null) return false;

        // Anchor legacy prices before making a changed price effective next month.
        LocalDate nextMonth = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .withDayOfMonth(1).plusMonths(1);
        jdbc.update("INSERT INTO svc_room_price(room_id,effective_from,amount) "
                + "SELECT ?,DATE '0001-01-01',? WHERE NOT EXISTS "
                + "(SELECT 1 FROM svc_room_price WHERE room_id=?) ON CONFLICT(room_id,effective_from) DO NOTHING",
                id, oldPrice, id);
        jdbc.update("INSERT INTO svc_room_price(room_id,effective_from,amount) VALUES(?,?,?) "
                + "ON CONFLICT(room_id,effective_from) DO UPDATE SET amount=EXCLUDED.amount",
                id, nextMonth, room.basePrice());

        return jdbc.update("""
                UPDATE phong SET matoanha=?,maloaiphong=?,sophong=?,tang=?,succhua=?,giaphongcoban=?,trangthai=?
                WHERE maphong=?
                """, room.buildingId(), room.roomTypeId(), room.number(), room.floor(), room.capacity(),
                room.basePrice(), room.status(), id) == 1;
    }

    /** Deletes a room and its dependent records according to the schema's FK rules. */
    public boolean deleteRoom(String id) {
        return jdbc.update("DELETE FROM phong WHERE maphong=?", id) == 1;
    }

    /** Inserts one bed into an existing room. */
    public void createBed(BedWriteDto bed) {
        jdbc.update("INSERT INTO giuong(magiuong,maphong,sogiuong,vitri,trangthai) VALUES(?,?,?,?,?)",
                bed.id(), bed.roomId(), bed.number(), bed.position(), bed.status());
    }

    /** Updates the room, number, position, and status of a bed. */
    public boolean updateBed(String id, BedWriteDto bed) {
        return jdbc.update("UPDATE giuong SET maphong=?,sogiuong=?,vitri=?,trangthai=? WHERE magiuong=?",
                bed.roomId(), bed.number(), bed.position(), bed.status(), id) == 1;
    }

    /** Deletes one bed according to the schema's FK rules. */
    public boolean deleteBed(String id) {
        return jdbc.update("DELETE FROM giuong WHERE magiuong=?", id) == 1;
    }

    /** Checks whether a bed identifier exists before an update. */
    public boolean bedExists(String bedId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM giuong WHERE magiuong=?", Long.class, bedId);
        return count != null && count > 0;
    }

    /** Prevents deleting a room while linked student, billing, or repair history exists. */
    public boolean roomHasHistory(String roomId) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM sinh_vien WHERE maphonghientai=?)
                    OR EXISTS(SELECT 1 FROM dang_ky_phong d JOIN giuong g USING(magiuong) WHERE g.maphong=?)
                    OR EXISTS(SELECT 1 FROM luu_tru l JOIN giuong g USING(magiuong) WHERE g.maphong=?)
                    OR EXISTS(SELECT 1 FROM hoa_don WHERE maphong=?)
                    OR EXISTS(SELECT 1 FROM yeu_cau_sua_chua WHERE maphong=?)
                    OR EXISTS(SELECT 1 FROM chi_so_dien_nuoc WHERE maphong=?)
                    OR EXISTS(SELECT 1 FROM svc_room_price WHERE room_id=?)
                    OR EXISTS(SELECT 1 FROM svc_room_extra WHERE room_id=?)
                """, Boolean.class, roomId, roomId, roomId, roomId, roomId, roomId, roomId, roomId);
        return Boolean.TRUE.equals(exists);
    }

    /** Prevents deleting a bed while a registration or residency refers to it. */
    public boolean bedHasHistory(String bedId) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM dang_ky_phong WHERE magiuong=?)
                    OR EXISTS(SELECT 1 FROM luu_tru WHERE magiuong=?)
                """, Boolean.class, bedId, bedId);
        return Boolean.TRUE.equals(exists);
    }
}
