# ⚙️ DỰ ÁN CÔNG NGHỆ PHẦN MỀM (CNPM)

- **Production URL:** [https://cnpm.caohoangphuc.id.vn](https://cnpm.caohoangphuc.id.vn)
- **Tech Stack:** Java 17 / Jakarta EE 10 / ASP.NET, MySQL 8.0 / SQL Server, Docker, Bootstrap 5.

---

## 🚨 QUY TẮC PHÁT TRIỂN & SỬ DỤNG NHÁNH (BẮT BUỘC CHO TẤT CẢ THÀNH VIÊN)

1. **Tạo nhánh riêng cho từng module / Use Case:**
   - **TUYỆT ĐỐI KHÔNG commit/push trực tiếp vào nhánh `main` hay `develop`**.
   - Mỗi thành viên khi làm chức năng phải tạo nhánh theo định dạng:
     - `feat/<tên-thành-viên>-<tên-module>` (Ví dụ: `feat/linh-auth-role`, `feat/gam-facility`, `feat/khoa-bill`, `feat/dat-payment`).
     - `fix/<tên-thành-viên>-<tên-lỗi>` khi sửa lỗi.
   - Sau khi hoàn thành và test local thành công, tạo **Pull Request (PR)** vào `develop` để review trước khi merge vào `main`.

2. **Cơ sở dữ liệu (Database) đã có sẵn:**
   - Database đã được cấu hình và chạy sẵn trên Docker container của server.
   - Khi có thay đổi bảng, thêm cột hoặc cập nhật dữ liệu mẫu, chỉ thêm file script SQL mới vào thư mục `/database` (Ví dụ: `/database/02_update_dorm.sql`). Không xóa hoặc làm gián đoạn database đang chạy.

3. **Không chỉnh sửa các file cấu hình hệ thống:**
   - Các file: `Dockerfile`, `docker-compose.yml`, `.github/workflows/`, cấu hình server đã được chuẩn hóa.
   - **Thành viên chỉ tập trung chỉnh sửa sourcecode trong thư mục `src/` và tài liệu**.

4. **Tự động Build & Deploy (CI/CD):**
   - Khi PR được merge vào nhánh chính, hệ thống CI/CD sẽ tự động kích hoạt build (sử dụng Docker Maven cache không tải lại thư viện) và cập nhật phiên bản mới nhất lên server tại: `https://cnpm.caohoangphuc.id.vn`.

---

## 🛠 HƯỚNG DẪN CHẠY LOCAL VỚI DOCKER

```bash
# 1. Khởi động DB và Web app local
docker compose up -d --build

# 2. Truy cập local
http://localhost:8091
```
