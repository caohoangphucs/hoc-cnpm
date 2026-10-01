# 🏢 HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ (KTX SMART HUB) - ĐỒ ÁN CNPM

- **Production URL:** [https://cnpm.caohoangphuc.id.vn](https://cnpm.caohoangphuc.id.vn)
- **Tech Stack:** Java 17, Jakarta EE 10 (Servlet 6.0, JSP/JSTL 3.0), MySQL 8.0, HikariCP Connection Pool, BCrypt, Docker, Bootstrap 5.
- **Kiến trúc:** 3 Tầng chuẩn hóa (Presentation Layer $\rightarrow$ Business/Service Layer $\rightarrow$ Data Access / Stored Procedures & Triggers).

---

## 👥 PHÂN CÔNG TRÁCH NHIỆM 5 THÀNH VIÊN & 8 USE CASES

| STT | Thành Viên | Vai Trò & Phân Hệ | Thực Thể Phụ Trách | Use Cases & Nghiệp Vụ Chính |
|---|---|---|---|---|
| **1** | **TV1 (Lead)** | Hạ Tầng, DevOps, CSDL & Kỹ Thuật | `VAT_TU`, `XUAT_VAT_TU`, `YEU_CAU_SUA_CHUA`, `AUDIT_LOG` | Toàn bộ Schema/SP/Trigger, Connection Pool (`DBContext`), Module Kỹ thuật (`/maintenance`), xuất kho vật tư (`usp_HoanThanhSuaChua`). |
| **2** | **TV2** | Auth, Sinh Viên, Bảo Vệ, Thống Kê | `TAI_KHOAN`, `SINH_VIEN`, `RA_VAO`, `VI_PHAM` | **UC-01** (Đăng nhập 5 vai trò), **UC-08** (Báo cáo doanh thu & tỷ lệ lấp đầy `vw_DoanhThuThang`), Module Bảo vệ quét thẻ ra vào & phạt trễ sau 23h (`usp_GhiNhanRaVao`). |
| **3** | **TV3** | Cơ Sở Vật Chất & Đăng Ký Ở | `TOA_NHA`, `LOAI_PHONG`, `PHONG`, `GIUONG`, `DANG_KY_PHONG`, `LUU_TRU` | **UC-02** (Quản lý phòng/giường), **UC-03** (Sinh viên đặt giữ giường chống tranh chấp `usp_DatGiuong`), **UC-04** (Admin duyệt đăng ký `usp_DuyetDangKy` + Trigger gán giường). |
| **4** | **TV4** | Điện Nước & Hóa Đơn | `DON_GIA_DICH_VU`, `CHI_SO_DIEN_NUOC`, `HOA_DON`, `CHI_TIET_HOA_DON` | **UC-05** (Ghi chỉ số điện nước `usp_GhiChiSoDienNuoc`), **UC-06** (Phát hành hóa đơn tự động `usp_PhatHanhHoaDon`). |
| **5** | **TV5** | Thanh Toán & Giao Diện Lead | `THANH_TOAN` | **UC-07** (Thanh toán trực tuyến VNPAY/Tiền mặt `usp_XacNhanThanhToan`), Khung layout chung, UI Responsive. |

---

## 🚨 QUY TẮC PHÁT TRIỂN & SỬ DỤNG NHÁNH (BẮT BUỘC)

1. **Tạo nhánh theo module / chức năng:**
   - **TUYỆT ĐỐI KHÔNG commit/push trực tiếp vào nhánh `main` hay `develop`**.
   - Đặt tên nhánh: `feat/<tên-bạn>-<module>` (Ví dụ: `feat/linh-auth`, `feat/gam-rooms`, `feat/khoa-invoices`, `feat/dat-vnpay`).
   - Sửa lỗi: `fix/<tên-bạn>-<tên-lỗi>`.
   - Tạo PR vào `develop` sau khi test xong.

2. **Cơ sở dữ liệu (Database) đã có sẵn:**
   - Dữ liệu và database được quản lý tự động bởi Docker container.
   - Thêm bảng hoặc update logic chỉ tạo file SQL mới trong thư mục `/database/` (ví dụ: `/database/02_update_vnpay.sql`). Không xóa hoặc làm gián đoạn DB.

3. **Không sửa các file hệ thống:**
   - Các file: `Dockerfile`, `docker-compose.yml`, `.github/workflows/` đã chuẩn hóa.
   - **Thành viên chỉ tập trung chỉnh sửa mã nguồn trong `src/`**.

4. **CI/CD Tự Động:**
   - Khi PR merge vào `main`, GitHub Actions tự động kích hoạt build (sử dụng Docker Maven Cache layer) và tự deploy lên `https://cnpm.caohoangphuc.id.vn`.

---

## 🔐 TÀI KHOẢN TRẢI NGHIỆM DEMO (5 VAI TRÒ)

| Vai Trò | Username | Mật Khẩu | Quyền Hạn & Màn Hình |
|---|---|---|---|
| **Admin** | `admin` | `123456` | Quản trị toàn hệ thống, duyệt đăng ký giường (`/admin/dashboard`) |
| **Sinh Viên** | `sv001` (hoặc `sv002`) | `123456` | Xem sơ đồ phòng, đặt giữ giường, xem hóa đơn, gửi báo hỏng (`/student/dashboard`) |
| **Kế Toán** | `ketoan1` | `123456` | Nhập chỉ số điện nước, phát hành hóa đơn, thu tiền mặt (`/accounting/dashboard`) |
| **Kỹ Thuật** | `kythuat1` | `123456` | Quản lý kho vật tư, xử lý báo hỏng và xuất kho (`/maintenance/dashboard`) |
| **Bảo Vệ** | `baove1` | `123456` | Quét thẻ sinh viên ra vào, tự động ghi nhận vi phạm sau 23h (`/security/dashboard`) |

---

## 🛠 HƯỚNG DẪN CHẠY LOCAL VỚI DOCKER

```bash
# 1. Khởi động DB và ứng dụng
docker compose up -d --build

# 2. Truy cập ứng dụng local
http://localhost:8091
```
