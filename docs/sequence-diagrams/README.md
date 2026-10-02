# Sequence Diagram cho demo KTX

Bộ sơ đồ gồm **8 UC trọng tâm** được chọn từ [SRS cũ](../demo/NGUON.md).
Mỗi UC có nguồn PlantUML riêng và bản SVG tương ứng để mở trong trình duyệt.
Sơ đồ mô tả thiết kế đích theo đặc tả; mức triển khai hiện tại nằm trong
[kế hoạch demo](../demo/README.md#khoảng-cách-triển-khai).

| UC | Nghiệp vụ | REQ | Tệp |
|---|---|---|---|
| UC-04 | Đăng ký phòng trực tuyến | REQ-001 | [PlantUML](demo/UC-04_dang-ky-phong.puml) · [SVG](demo/UC-04_dang-ky-phong.svg) |
| UC-12 | Duyệt đơn đăng ký phòng | REQ-011, REQ-005 | [PlantUML](demo/UC-12_duyet-dang-ky.puml) · [SVG](demo/UC-12_duyet-dang-ky.svg) |
| UC-17 | Tạo hóa đơn tháng tự động | REQ-033, REQ-008, REQ-034, REQ-005 | [PlantUML](demo/UC-17_tao-hoa-don.puml) · [SVG](demo/UC-17_tao-hoa-don.svg) |
| UC-08 | Thanh toán hóa đơn trực tuyến | REQ-004, REQ-005 | [PlantUML](demo/UC-08_thanh-toan-truc-tuyen.puml) · [SVG](demo/UC-08_thanh-toan-truc-tuyen.svg) |
| UC-22 | Tiếp nhận và xử lý yêu cầu sửa chữa | REQ-030, REQ-003, REQ-015, REQ-005 | [PlantUML](demo/UC-22_xu-ly-sua-chua.puml) · [SVG](demo/UC-22_xu-ly-sua-chua.svg) |
| UC-25 | Ghi nhận giờ sinh viên về KTX | REQ-036, REQ-035, REQ-037 | [PlantUML](demo/UC-25_ghi-nhan-ra-vao.puml) · [SVG](demo/UC-25_ghi-nhan-ra-vao.svg) |
| UC-14 | Xử lý vi phạm kỷ luật | REQ-012, REQ-014 | [PlantUML](demo/UC-14_xu-ly-ky-luat.puml) · [SVG](demo/UC-14_xu-ly-ky-luat.svg) |
| UC-30 | Đăng ký hoạt động công ích | REQ-017, REQ-029, REQ-038 | [PlantUML](demo/UC-30_dang-ky-cong-ich.puml) · [SVG](demo/UC-30_dang-ky-cong-ich.svg) |

## Cách đọc

- Actor → Thymeleaf/Bootstrap → Spring @RestController → @Service/@Transactional → @Repository/JdbcTemplate → PostgreSQL/SP.
- `<<đề xuất>>` là thành phần cần triển khai; Controller/Service/Repository Spring Boot chưa có trong mã hiện tại.
- Các ghi chú Servlet/DAO là nền legacy để đối chiếu; class đích Spring Boot được mô tả ở [kiến trúc](../demo/SPRING_BOOT.md). SP giữ contract, không chứng minh
  toàn bộ thông điệp của sơ đồ đã được cài đặt.
- `alt` là các nhánh lựa chọn/lỗi; `opt` là nhánh tùy điều kiện; `loop`
  là xử lý lặp; `ref` chỉ tiền điều kiện hoặc nghiệp vụ liên quan.
- Include gửi thông báo dùng REQ-005 để tránh nhầm UC-03 giữa hai bản SRS.
- Thông báo và đồng bộ ra hệ thống ngoài diễn ra sau commit. Khi gửi lỗi,
  retry từ dữ liệu bền vững; không lặp nghiệp vụ đã hoàn thành.

## Sửa và kết xuất

Sửa tệp `.puml`, sau đó kết xuất lại SVG từ thư mục gốc repository:

```sh
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -checkonly docs/sequence-diagrams/demo/*.puml
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tsvg docs/sequence-diagrams/demo/*.puml
```

Dùng PlantUML **1.2026.2** để tái tạo bản SVG đã kiểm tra. Sequence Diagram
không cần Graphviz. JAR là công cụ ngoài repository, không cần đưa vào Git.
Sơ đồ đầy đủ 28 yêu cầu chức năng vẫn là phần mở rộng trong TODO.
