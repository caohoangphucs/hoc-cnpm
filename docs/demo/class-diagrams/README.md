# Class Diagram demo

Thiết kế đích cho 8 UC, phân biệt với Class Diagram lịch sử trong docs/main. Service/repository Java cho 8 UC đã có; controller và adapter tích hợp ngoài còn cần triển khai. DAO/DBContext lịch sử thuộc nền cũ. Service/repository Java cho 8 UC hiện có; repository dùng JdbcTemplate và DataSource auto-configured theo [Spring Boot](../SPRING_BOOT.md).

- [Kiến trúc lớp](architecture.svg) — [nguồn PlantUML](architecture.puml): vai trò lớp, service/repository SQL, transaction và adapter.
- [Mô hình miền](domain.svg) — [nguồn PlantUML](domain.puml): kiểu dữ liệu, liên kết và multiplicity.
- [Dữ liệu/SQL](../DU_LIEU_VA_SQL.md), [quy tắc](../QUY_TAC_NGHIEP_VU.md), [API](../API_VA_PHAN_QUYEN.md).

DTO request/response theo OpenAPI; Java money BigDecimal, id Long/UUID, timestamp Instant, date LocalDate, trạng thái enum. Enum và constraints phải dùng cùng tập giá trị trong DU_LIEU_VA_SQL. Model không tự gọi DB; DAO không gọi provider. ServiceContext/TransactionTemplate và JdbcTransactionManager dùng cùng DataSource; JdbcTemplate chia sẻ Connection Spring quản lý. Controller/API, Security và giao diện nghiệp vụ còn thiếu; schema được quản lý bằng SQL thủ công theo SPRING_BOOT.md.

Render bằng PlantUML với `!pragma layout smetana`, không cần Graphviz. Khi sửa nguồn kết xuất lại SVG và kiểm bố cục.

Bản PNG trực tiếp: [kiến trúc](architecture.png), [mô hình miền](domain.png); SVG/PNG cùng nguồn, không có thành phần quản lý migration.
