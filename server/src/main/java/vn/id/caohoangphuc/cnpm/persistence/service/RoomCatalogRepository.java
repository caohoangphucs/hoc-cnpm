package vn.id.caohoangphuc.cnpm.persistence.service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.api.dto.BedDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomDto;

/** Read-only SQL queries for rooms and bed availability. */
public class RoomCatalogRepository {
    public record RoomPage(List<RoomDto> items, long total) {}

    public record BedPage(List<BedDto> items, long total) {}

    private final JdbcTemplate jdbc;

    /** Stores the JDBC client used for the small room-catalogue queries. */
    public RoomCatalogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Reads room details and counts only beds that can currently be booked. */
    public RoomPage rooms(int pageSize, long offset) {
        var items = jdbc.query("""
                        SELECT p.maphong,p.matoanha,COALESCE(l.tenloaiphong,p.maloaiphong,'Chưa phân loại'),
                               COALESCE(t.gioitinhapdung,'TAT_CA'),p.succhua,
                               COUNT(g.magiuong) FILTER (
                                   WHERE g.trangthai='TRONG'
                                     AND COALESCE(p.trangthai,'TRONG') NOT IN ('KHOA','DANG_SUA','DAY'))
                        FROM phong p
                        JOIN toa_nha t USING(matoanha)
                        LEFT JOIN loai_phong l USING(maloaiphong)
                        LEFT JOIN giuong g USING(maphong)
                        GROUP BY p.maphong,p.matoanha,l.tenloaiphong,p.maloaiphong,
                                 t.gioitinhapdung,p.succhua,p.tang,p.sophong
                        ORDER BY p.matoanha,p.tang,p.sophong
                        LIMIT ? OFFSET ?
                        """,
                (rs, row) -> new RoomDto(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getLong(5),
                        rs.getLong(6)),
                pageSize,
                offset);
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM phong", Long.class);
        return new RoomPage(items, total);
    }

    /** Reads a page of beds in one room, including each bed's optimistic-lock version. */
    public BedPage beds(String roomId, int pageSize, long offset) {
        var items = jdbc.query("""
                        SELECT magiuong,sogiuong,trangthai,version
                        FROM giuong
                        WHERE maphong=?
                        ORDER BY sogiuong,magiuong
                        LIMIT ? OFFSET ?
                        """,
                (rs, row) -> new BedDto(
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getLong(4)),
                roomId,
                pageSize,
                offset);
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM giuong WHERE maphong=?", Long.class, roomId);
        return new BedPage(items, total);
    }

    /** Checks that a room exists before returning an empty bed list for it. */
    public boolean roomExists(String roomId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM phong WHERE maphong=?", Long.class, roomId);
        return count != null && count > 0;
    }
}
