# Class Diagram demo

Thiết kế đích cho 8 UC, phân biệt với Class Diagram lịch sử trong docs/main. Service và adapter được đề xuất; chưa tạo class Java mới. DAO/DBContext hiện có là nền legacy; đích thay bằng @Repository/JdbcTemplate/DataSource auto-configured theo [Spring Boot](../SPRING_BOOT.md).

- [Kiến trúc lớp](architecture.svg) — [nguồn PlantUML](architecture.puml): vai trò lớp, SP/JDBC, transaction và adapter.
- [Mô hình miền](domain.svg) — [nguồn PlantUML](domain.puml): kiểu dữ liệu, liên kết và multiplicity.
- [Dữ liệu/SP](../DU_LIEU_VA_SP.md), [quy tắc](../QUY_TAC_NGHIEP_VU.md), [API](../API_VA_PHAN_QUYEN.md).

DTO request/response theo OpenAPI; Java money BigDecimal, id Long/UUID, timestamp Instant, date LocalDate, trạng thái enum. Enum và constraints phải dùng cùng tập giá trị trong DU_LIEU_VA_SP. Model không tự gọi DB; DAO không gọi provider. @Transactional Service và JdbcTransactionManager dùng cùng DataSource; JdbcTemplate chia sẻ Connection Spring quản lý. Controller/DTO/Security/Thymeleaf và migration theo SPRING_BOOT.md.

Render bằng PlantUML với `!pragma layout smetana`, không cần Graphviz. Khi sửa nguồn kết xuất lại SVG và kiểm bố cục.
