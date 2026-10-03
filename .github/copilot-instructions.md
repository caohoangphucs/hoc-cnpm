# Development context

Backend nằm trong `server/`: Spring Boot4.1.1, Java17, PostgreSQL16. Frontend nằm trong `client/`: React/TypeScript/Vite, Nginx proxy cùng origin. Xem [thiết kế](../docs/demo/SPRING_BOOT.md) và [Git workflow](../docs/main/GIT_WORKFLOW.md).

- Dùng một DataSource/HikariCP. JPA entity/read repository chỉ đọc, schema validate; transaction ghi dùng Spring JDBC và stored procedure.
- `DormitoryProcedureRepository` bắt buộc caller có JDBC transaction; status lỗi chuyển thành runtime exception để rollback. Không tạo pool, commit hoặc Connection riêng trong repository.
- Entity ánh xạ schema hiện hành; DTO là contract API đích, không trả entity/hash/CCCD trực tiếp ra HTTP. Các bảng chưa có trong SQL không được giả lập thành entity hiện hành.
- Service/Controller nghiệp vụ và auth/session/2FA chưa triển khai. Giữ quyền từ session và kiểm ownership khi viết Service; không tin actor/student identity từ body.
- Không dùng migration tự động hoặc ddl-auto=create/update. Nguồn schema/seed duy nhất trong database/. Reset chỉ DB demo riêng.
- CI/CD build/test/deploy cả server/client tại server, không chuyển image qua cloud. Không đánh dấu TODO hoàn thành chỉ vì đã có DTO/entity/scaffold.
- Trao đổi tiếng Việt; làm việc trên nhánh riêng.
