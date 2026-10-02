# Biểu đồ hiện hành và tài liệu nguồn

| Bản xem | Nguồn chuẩn | Phạm vi |
|---|---|---|
| [ERD27bảng](ERD_Full_27Tables.png) / [SVG](ERD_Full_27Tables.svg) | [PlantUML](ERD_Full_27Tables.puml) và [schemaSQL](../../database/01_schema.sql) | Schema hiện hành; tên trường/kiểu/PK/FK/UQ đối chiếu SQL |
| [Class Diagram kiến trúc](../demo/class-diagrams/architecture.png) | [kiến trúc Spring Boot](../demo/class-diagrams/architecture.puml) | Thiết kế đích8UC |
| [ERD19bảng lịch sử](ERD_Full_19Tables.png) / [SVG](ERD_Full_19Tables.svg) | [PlantUML](ERD_Full_19Tables.puml), schema commit15da6e7 | Lịch sử, không dùng làm inventory hiện hành |

PNG/SVG được render trực tiếp từ PlantUML, không chụp viewport. CHECK/DEFAULT/ON DELETE chi tiết đọc schemaSQL; star đánh dấu NOT NULL/PK, field không star là nullable.

SRS DOCX, Class/ERD drawio cũ, UseCase Diagram và ScreenFlow được giữ làm nguồn/lịch sử; không dùng ảnh mới để tuyên bố code Spring Boot đã tồn tại. Thiết kế demo hiện hành ở [docs/demo](../demo/README.md).
