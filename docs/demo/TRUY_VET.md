# Truy vết triển khai

| UC / REQ | Quy tắc | API | Dữ liệu/SP | Test |
|---|---|---|---|---|
| UC-04 / 001 | BR-03/04 | bookings,files,rooms | DANG_KY_PHONG/ho_so;usp_DatGiuong | TC-04-01, TC-04-02, TC-04-03, TC-04-04, TC-04-05, TC-04-06, TC-04-07, TC-04-08 |
| UC-12 / 011 | BR-05/17 | bookings/{id}/decision | hop_dong/LUU_TRU;usp_DuyetDangKy | TC-12-01, TC-12-02, TC-12-03, TC-12-04, TC-12-05, TC-12-06 |
| UC-17 / 033 | BR-06..08 | meters,invoice-drafts,invoices | hoa_don_snapshot;usp_PhatHanhHoaDon | TC-17-01, TC-17-02, TC-17-03, TC-17-04, TC-17-05, TC-17-06 |
| UC-08 / 004 | BR-09/10 | payment-sessions,IPN | phien/phan_bo/callback;usp_XacNhanThanhToan | TC-08-01, TC-08-02, TC-08-03, TC-08-04, TC-08-05, TC-08-06, TC-08-07, TC-08-08 |
| UC-22 / 030 | BR-11 | repairs | lich_su/phan_cong/de_nghi;usp_HoanThanhSuaChua | TC-22-01, TC-22-02, TC-22-03, TC-22-04, TC-22-05 |
| UC-25 / 036 | BR-12 | entries,visitors | the_ktx/su_kien_cong;usp_GhiNhanRaVao | TC-25-01, TC-25-02, TC-25-03, TC-25-04, TC-25-05, TC-25-06 |
| UC-14 / 012 | BR-13 | discipline-decisions,appeals | quyet_dinh/khieu_nai;usp_XuLyKyLuat | TC-14-01, TC-14-02, TC-14-03, TC-14-04 |
| UC-30 / 017 | BR-14..16 | activities,points,certificates | hoat_dong/ledger/transfer;SPcongich | TC-30-01, TC-30-02, TC-30-03, TC-30-04, TC-30-05, TC-30-06, TC-30-07, TC-30-08 |

REQ-005 thông báo đi qua outbox; REQ-008 giảm giá theo BR-07; REQ-034 giá theo kỳ; REQ-035 khách thăm/037 báo cáo theo BR-12; REQ-029 quy đổi/038 giấy theo BR-16. RBAC và bảo mật áp dụng xuyên suốt, xem API_VA_PHAN_QUYEN. Main/alternative/exception/extension của 8 UC được thể hiện ở 8 sequence và thiết kế5bước; test catalog kiểm luồng chính,biên,cạnh tranh,rollback và tích hợp. Các ID ở README cũ không thay ID SRS.
