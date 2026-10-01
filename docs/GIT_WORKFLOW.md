# QUY CHUẨN GIT WORKFLOW: ĐẶT TÊN NHÁNH, COMMIT & PUSH CODE
**Dự Án:** Hệ Thống Quản Lý Ký Túc Xá (KTX Smart Hub) - Đồ Án CNPM  
**Áp Dụng Cho:** Toàn bộ thành viên nhóm phát triển

---

## 1. MÔ HÌNH QUẢN LÝ NHÁNH (GIT BRANCHING STRATEGY)

Hệ thống tuân thủ mô hình Git Flow chuẩn hóa gồm 3 cấp độ nhánh:

```text
  main (Production - Tự động deploy cnpm.caohoangphuc.id.vn)
   ▲
   │ (Pull Request cuối kỳ / sau khi test ổn định)
  develop (Nhánh tích hợp chung của cả nhóm)
   ▲
   │ (Pull Request từng tính năng)
  feat/fix/refactor (Nhánh làm việc của từng cá nhân)
```

* **`main`**: Nhánh Production chính thức. **TUYỆT ĐỐI KHÔNG commit hoặc push trực tiếp**. Chỉ nhận merge từ `develop` qua PR đã được Lead duyệt.
* **`develop`**: Nhánh tích hợp đang phát triển. Toàn bộ tính năng mới sau khi hoàn thành sẽ được tạo PR vào đây.
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

### 3.1. Cú pháp chuẩn:
$$\text{<type>}(\text{<scope>}): \text{<mô tả ngắn ngọn>}$$

* **`<type>`**: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`, `test`.
* **`<scope>`**: Phân hệ chịu ảnh hưởng (`auth`, `room`, `invoice`, `maintenance`, `security`, `db`, `ui`, `config`).
* **`<mô tả>`**: Viết ngắn gọn (dưới 72 ký tự), viết bằng tiếng Việt (hoặc tiếng Anh nhất quán).

### 3.2. Ví dụ chuẩn:
* ✅ `feat(room): them chuc nang loc phong con trong theo tang`
* ✅ `fix(invoice): sua loi tinh tien dien bi am khi chi so moi nho hon chi so cu`
* ✅ `feat(payment): tich hop tao url thanh toan sandbox vnpay`
* ✅ `style(ui): can chinh responsive modal dat giuong tren man hinh dien thoai`
* ✅ `docs(workflow): bo sung huong dan git workflow va quy tac commit`
* ✅ `refactor(db): toi uu co che tu dong phat hien host docker vs local trong DBContext`

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
