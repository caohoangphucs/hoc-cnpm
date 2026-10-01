# QUY CHUẨN GIT WORKFLOW: ĐẶT TÊN NHÁNH, COMMIT & PUSH CODE
**Dự Án:** Hệ Thống Quản Lý Ký Túc Xá (KTX Smart Hub) - Đồ Án CNPM  
**Áp Dụng Cho:** Toàn bộ thành viên nhóm phát triển

---

## 1. MÔ HÌNH QUẢN LÝ NHÁNH (GIT BRANCHING STRATEGY)

Hệ thống tuân thủ mô hình Git Flow chuẩn hóa gồm 3 cấp độ nhánh:

```text
  main (Nhánh release)
   ▲
   │ (Pull Request cuối kỳ / sau khi test ổn định)
  develop (Nhánh tích hợp chung của cả nhóm)
   ▲
   │ (Pull Request từng tính năng)
  feat/fix/refactor (Nhánh làm việc của từng cá nhân)
```

> [!CAUTION]
> **LỆNH CẤM BẮT BUỘC ĐỐI VỚI TẤT CẢ THÀNH VIÊN:**
> 1. **TUYỆT ĐỐI KHÔNG commit hoặc push trực tiếp vào nhánh `main`**.
> 2. Mọi thành viên chỉ làm việc trên nhánh cá nhân `feat/...`, `fix/...` và tạo Pull Request vào **`develop`**.
> 3. **Nhánh `main` CHỈ được merge từ `develop` khi:**
>    - Toàn bộ 6–8 Use Cases đã hoàn thành và kiểm thử ổn định trên `develop`.
>    - Chốt đợt Release / Báo cáo / Chấm điểm đồ án cuối kỳ.
>    - Khi Lead cập nhật tài liệu hoặc cấu hình chung của toàn repo.

* **`develop`**: Nhánh tích hợp chính; push/PR kích hoạt CI kiểm tra PostgreSQL, Maven và container. Workflow hiện chưa triển khai lên server. Toàn bộ tính năng mới sau khi hoàn thành sẽ được tạo PR vào đây.
* **`main`**: Nhánh Release / Production chính thức (chốt nộp bài cuối kỳ / demo đồ án). Chỉ nhận merge từ `develop` qua PR do Lead duyệt.
* **Nhánh cá nhân (`feat/*`, `fix/*`, `docs/*`)**: Nơi mỗi thành viên tự do phát triển mà không làm ảnh hưởng đến người khác.

---

## 2. QUY TẮC ĐẶT TÊN NHÁNH (BRANCH NAMING CONVENTION)

### 2.1. Cú pháp bắt buộc:
$$\text{<tiền-tố>}/\text{<tên-bạn>}-\text{<tên-chức-năng>}$$

* Toàn bộ dùng **chữ thường (lowercase)**.
* Các từ nối bằng dấu gạch ngang (`-`), **không dùng khoảng trắng hay gạch dưới `_`**.
* Không dấu tiếng Việt.

### 2.2. Các tiền tố chuẩn:

| Tiền tố | Mục đích | Ví dụ hợp lệ |
|---|---|---|
| **`feat/`** | Phát triển tính năng mới | `feat/dat-vnpay`<br>`feat/gam-room-booking`<br>`feat/linh-auth-login` |
| **`fix/`** | Sửa lỗi bug phát sinh | `fix/dat-invoice-calculation`<br>`fix/khoa-meter-negative`<br>`fix/phuc-session-timeout` |
| **`refactor/`** | Tối ưu mã nguồn, không đổi logic chức năng | `refactor/dat-db-pool`<br>`refactor/gam-dao-queries` |
| **`style/`** | Căn chỉnh UI, CSS, responsive, icon | `style/dat-mobile-navbar`<br>`style/linh-login-card` |
| **`docs/`** | Viết hoặc cập nhật tài liệu, diagram, SRS | `docs/dat-git-workflow`<br>`docs/phuc-update-erd` |
| **`test/`** | Viết Unit Test, kiểm thử tự động | `test/dat-account-dao-unit` |

### 2.3. Các ví dụ SAI cần tránh:
* ❌ `dat` *(Quá ngắn, không biết làm chức năng gì)*
* ❌ `feat/chuc-nang-moi` *(Không biết ai đang phụ trách nhánh này)*
* ❌ `Feat/Dat_VnPay` *(Viết hoa và dùng dấu gạch dưới)*
* ❌ `fix-bug` *(Không đúng cấu trúc phân nhóm)*

---

## 3. QUY TẮC VIẾT COMMIT MESSAGE (CONVENTIONAL COMMITS)

Mỗi commit phải giải thích rõ ràng **đã làm gì và ở đâu**. Giúp cả nhóm theo dõi lịch sử và debug khi có sự cố.

### 3.1. Cú pháp chuẩn (Bắt buộc theo C6-D1):
$$\text{<type>}(\text{<scope>}): \text{<mô tả ngắn ngọn>} \text{ #UC-XX}$$

* **`<type>`**: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`, `test`.
* **`<scope>`**: Phân hệ chịu ảnh hưởng (`auth`, `room`, `invoice`, `maintenance`, `security`, `db`, `ui`, `config`).
* **`<mô tả>`**: Viết ngắn gọn (dưới 72 ký tự), viết bằng tiếng Việt (hoặc tiếng Anh nhất quán).
* **`#UC-XX`**: Mã Use Case tương ứng (`#UC-01` .. `#UC-08`) để đối chiếu Traceability khi chấm bài.

### 3.2. Ví dụ chuẩn:
* ✅ `feat(room): them chuc nang loc phong con trong theo tang #UC-02`
* ✅ `fix(invoice): sua loi tinh tien dien bi am khi chi so moi nho hon chi so cu #UC-06`
* ✅ `feat(payment): tich hop tao url thanh toan sandbox vnpay #UC-07`
* ✅ `feat(booking): sinh vien chon va dat giuong chong tranh chap #UC-03`
* ✅ `style(ui): can chinh responsive modal dat giuong tren dien thoai #UC-03`
* ✅ `docs(diagram): bo sung sequence diagram luong duyet dang ky #UC-04`
* ✅ `refactor(db): toi uu co che connection pool trong DBContext`

### 3.3. Các commit BỊ CẤM:
* ❌ `update`
* ❌ `fix bug`
* ❌ `done`
* ❌ `asdfghj`
* ❌ `sua lai code`

---

## 4. QUY TRÌNH PUSH CODE & TẠO PULL REQUEST (CHUẨN 7 BƯỚC)

### Bước 1: Luôn cập nhật mã nguồn mới nhất trước khi làm việc
```bash
git checkout develop
git pull origin develop
```

### Bước 2: Tạo nhánh mới cho chức năng của bạn
```bash
# Ví dụ: bạn tên Đạt, làm chức năng thanh toán VNPAY
git checkout -b feat/dat-vnpay
```

### Bước 3: Code và kiểm tra trạng thái
```bash
git status
```
> [!IMPORTANT]
> **Quy tắc an toàn dữ liệu:**
> * Không bao giờ commit các file rác: `.DS_Store`, `Thumbs.db`, file log `*.log`.
> * Không bao giờ commit file chứa thông tin nhạy cảm / mật khẩu cá nhân `.env`.
> * Chỉ `git add` đúng các file bạn đã chỉnh sửa.

### Bước 4: Commit theo chuẩn Conventional Commits
```bash
git add src/main/java/...
git commit -m "feat(payment): xu ly callback ipn tu cong thanh toan vnpay"
```

### Bước 5: Đồng bộ trước khi push (Tránh conflict)
Trước khi push, kéo code mới nhất từ `develop` về để kiểm tra có xung đột không:
```bash
git fetch origin develop
git rebase origin/develop
```
*(Nếu có conflict, giải quyết conflict, chạy `git add .` rồi `git rebase --continue`)*.

### Bước 6: Push nhánh lên GitHub
```bash
git push -u origin feat/dat-vnpay
```

### Bước 7: Mở Pull Request (PR) trên GitHub
1. Vào repository trên GitHub, bấm nút **Compare & pull request**.
2. **Base repository:** chọn `develop` *(Không chọn `main`)* $\leftarrow$ **Compare:** chọn nhánh của bạn (`feat/dat-vnpay`).
3. Điền tiêu đề PR rõ ràng: `[FEAT] Tích hợp thanh toán trực tuyến VNPAY`.
4. Mô tả ngắn gọn:
   - Các file đã thay đổi.
   - Kết quả tự kiểm thử (đã test những ca nào).
   - Ảnh chụp màn hình kết quả (nếu làm giao diện).
5. Gửi link PR vào nhóm để Lead hoặc thành viên khác Review.
6. Khi CI check GitHub Actions báo xanh ✅ và được duyệt $\rightarrow$ Merge vào `develop`.
7. **Xóa nhánh feature** trên GitHub sau khi merge thành công để kho lưu trữ luôn sạch sẽ.

---

## 5. BẢNG CHEATSHEET CỨU HỘ GIT THƯỜNG DÙNG

| Tình huống | Lệnh thực hiện |
|---|---|
| Xem trạng thái nhánh và file thay đổi | `git status` |
| Xem lịch sử commit dạng cây rút gọn | `git log --oneline -n 10 --graph` |
| Hủy bỏ thay đổi chưa commit ở 1 file | `git restore <tên-file>` |
| Sửa lại commit message gần nhất (khi chưa push) | `git commit --amend -m "feat(scope): noi dung moi"` |
| Cất tạm code đang làm dở để chuyển sang nhánh khác | `git stash` |
| Lấy lại code vừa cất tạm | `git stash pop` |
| Xóa nhánh local sau khi đã merge | `git branch -d <tên-nhánh>` |
