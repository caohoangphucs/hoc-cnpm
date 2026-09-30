# HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ (KTX SMART HUB) - ĐỒ ÁN CNPM

- **Production URL:** [https://cnpm.caohoangphuc.id.vn](https://cnpm.caohoangphuc.id.vn)
- **Tech Stack:** Java 17, Jakarta EE 10 (Servlet 6.0, JSP/JSTL 3.0), PostgreSQL 16, HikariCP Connection Pool, BCrypt, Docker, Bootstrap 5.
- **Kiến trúc:** 3 Tầng chuẩn hóa (Presentation Layer $\rightarrow$ Business/Service Layer $\rightarrow$ Data Access / Stored Procedures & Triggers).

---

## PHÂN CÔNG TRÁCH NHIỆM 5 THÀNH VIÊN & 8 USE CASES

| STT | Thành Viên | Vai Trò & Phân Hệ | Thực Thể Phụ Trách | Use Cases & Nghiệp Vụ Chính |
|---|---|---|---|---|
| **1** | **TV1 (Lead)** | Hạ Tầng, DevOps, CSDL & Kỹ Thuật | `VAT_TU`, `XUAT_VAT_TU`, `YEU_CAU_SUA_CHUA`, `AUDIT_LOG` | Toàn bộ Schema/SP/Trigger, Connection Pool (`DBContext`), Module Kỹ thuật (`/maintenance`), xuất kho vật tư (`usp_HoanThanhSuaChua`). |
| **2** | **TV2** | Auth, Sinh Viên, Bảo Vệ, Thống Kê | `TAI_KHOAN`, `SINH_VIEN`, `RA_VAO`, `VI_PHAM` | **UC-01** (Đăng nhập 5 vai trò), **UC-08** (Báo cáo doanh thu & tỷ lệ lấp đầy `vw_DoanhThuThang`), Module Bảo vệ quét thẻ ra vào & phạt trễ sau 23h (`usp_GhiNhanRaVao`). |
| **3** | **TV3** | Cơ Sở Vật Chất & Đăng Ký Ở | `TOA_NHA`, `LOAI_PHONG`, `PHONG`, `GIUONG`, `DANG_KY_PHONG`, `LUU_TRU` | **UC-02** (Quản lý phòng/giường), **UC-03** (Sinh viên đặt giữ giường chống tranh chấp `usp_DatGiuong`), **UC-04** (Admin duyệt đăng ký `usp_DuyetDangKy` + Trigger gán giường). |
| **4** | **TV4** | Điện Nước & Hóa Đơn | `DON_GIA_DICH_VU`, `CHI_SO_DIEN_NUOC`, `HOA_DON`, `CHI_TIET_HOA_DON` | **UC-05** (Ghi chỉ số điện nước `usp_GhiChiSoDienNuoc`), **UC-06** (Phát hành hóa đơn tự động `usp_PhatHanhHoaDon`). |
| **5** | **TV5** | Thanh Toán & Giao Diện Lead | `THANH_TOAN` | **UC-07** (Thanh toán trực tuyến VNPAY/Tiền mặt `usp_XacNhanThanhToan`), Khung layout chung, UI Responsive. |

---

## QUY TẮC PHÁT TRIỂN, SỬ DỤNG NHÁNH & COMMIT (BẮT BUỘC CHO TOÀN BỘ DỰ ÁN)

> 📖 **Xem chi tiết toàn bộ:** [Quy Chuẩn Git Workflow, Commit & Push Code](docs/main/GIT_WORKFLOW.md)

### ⛔ QUY TẮC BẢO VỆ NHÁNH `main` (CẤM TUYỆT ĐỐI COMMIT TRỰC TIẾP)
- **NGHIÊM CẤM TẤT CẢ THÀNH VIÊN commit hoặc push trực tiếp vào nhánh `main`**.
- Nhánh `main` chỉ nhận merge từ nhánh `develop` thông qua Pull Request (PR) sau khi đã kiểm thử ổn định.
- **Thời điểm duy nhất được merge vào `main`:**
  1. Khi đã hoàn tất và kiểm thử trơn tru toàn bộ các Use Cases trên `develop`.
  2. Khi chốt đợt Release / Nộp bài / Demo chấm điểm cuối kỳ.
  3. Khi Lead cập nhật tài liệu hoặc cấu hình nền móng chung của toàn dự án.

### 🌿 CẤU TRÚC NHÁNH & QUY TRÌNH LÀM VIỆC
1. **Mô hình nhánh chuẩn (Branching Strategy):**
   - `main`: Nhánh Release / Production (tự động kích hoạt CI/CD deploy lên https://cnpm.caohoangphuc.id.vn).
   - `develop`: Nhánh tích hợp chính (Staging) của cả nhóm. Toàn bộ tính năng mới bắt buộc tạo PR vào `develop`.
   - `feat/<tên-bạn>-<chức-năng>` / `fix/<tên-bạn>-<lỗi>`: Nhánh làm việc cá nhân (Ví dụ: `feat/dat-vnpay`, `feat/linh-auth`, `fix/dat-invoice-calc`).
   - Sử dụng script tạo nhánh nhanh tự động:
     ```powershell
     .\scripts\git-branch.ps1 -Type feat -Author dat -Feature vnpay
     ```

2. **Quy tắc viết Commit Message (Conventional Commits kèm #UC-XX):**
   - **Bắt buộc gắn mã Use Case (`#UC-XX`)** vào cuối message để phục vụ chấm điểm Traceability theo rubric C6-D1.
   - Cú pháp: `<type>(<scope>): <mô tả ngắn> #UC-XX`
   - Ví dụ chuẩn:
     - `feat(booking): sinh vien dat giuong chong tranh chap #UC-03`
     - `feat(payment): them nut thanh toan vnpay sandbox #UC-07`
     - `feat(auth): dang nhap 5 phan he phan quyen #UC-01`
     - `fix(invoice): sua loi am tien dien khi nhap sai #UC-06`
     - `docs(api): cap nhat sequence diagram luong dat giuong #UC-03`

3. **Cơ sở dữ liệu (Database) đã có sẵn:**
   - Dữ liệu và database được quản lý tự động bởi Docker container.
   - Reset và nạp lại CSDL mẫu 1-click bằng lệnh: `.\scripts\reset-db.ps1`.
   - Thêm bảng hoặc update logic chỉ tạo file SQL mới trong thư mục `/database/` (ví dụ: `/database/02_update_vnpay.sql`). Không xóa hoặc làm gián đoạn DB.

4. **Tiện ích phát triển nhanh (Hot-reload UI):**
   - Đã cấu hình `docker-compose.override.yml`: Khi sửa file JSP, CSS, JS trong `src/main/webapp`, bạn chỉ cần **bấm F5 trên trình duyệt** là thấy kết quả ngay mà không cần rebuild container.

5. **CI/CD Tự Động:**
   - Khi PR merge vào `main`, GitHub Actions tự động kích hoạt build (sử dụng Docker Maven Cache layer) và tự deploy lên `https://cnpm.caohoangphuc.id.vn`.

---

## TÀI KHOẢN TRẢI NGHIỆM DEMO (5 VAI TRÒ)

| Vai Trò | Username | Mật Khẩu | Quyền Hạn & Màn Hình |
|---|---|---|---|
| **Admin** | `admin` | `123456` | Quản trị toàn hệ thống, duyệt đăng ký giường (`/admin/dashboard`) |
| **Sinh Viên** | `sv001` (hoặc `sv002`) | `123456` | Xem sơ đồ phòng, đặt giữ giường, xem hóa đơn, gửi báo hỏng (`/student/dashboard`) |
| **Kế Toán** | `ketoan1` | `123456` | Nhập chỉ số điện nước, phát hành hóa đơn, thu tiền mặt (`/accounting/dashboard`) |
| **Kỹ Thuật** | `kythuat1` | `123456` | Quản lý kho vật tư, xử lý báo hỏng và xuất kho (`/maintenance/dashboard`) |
| **Bảo Vệ** | `baove1` | `123456` | Quét thẻ sinh viên ra vào, tự động ghi nhận vi phạm sau 23h (`/security/dashboard`) |

---

## HƯỚNG DẪN CHẠY LOCAL VỚI DOCKER

```bash
# 1. Khởi động DB và ứng dụng
docker compose up -d --build

# 2. Truy cập ứng dụng local
http://localhost:8091
```
