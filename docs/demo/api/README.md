# Danh mục API đích

OpenAPI mô tả input, loại dữ liệu, headers và response lỗi. Output cụ thể dưới đây là contract bổ sung; đây là thiết kế chưa có endpoint runtime.

| Method | Path (sau /api/v1) | UC | Quyền | Schema body |
|---|---|---|---|---|
| POST | /auth/login | AUTH |  | Login |
| POST | /auth/2fa | AUTH | ALL | Otp |
| GET | /auth/session | AUTH | ALL | — |
| POST | /auth/logout | AUTH | ALL | — |
| GET | /rooms | UC-04 | SINHVIEN,ADMIN | — |
| GET | /rooms/{id}/beds | UC-04 | SINHVIEN,ADMIN | — |
| POST | /bookings | UC-04 | SINHVIEN | Booking |
| GET | /bookings | UC-04 | SINHVIEN,ADMIN | — |
| POST | /bookings/{id}/decision | UC-12 | ADMIN | Approval |
| POST | /meters | UC-17 | KETOAN | Meter |
| POST | /invoice-drafts | UC-17 | KETOAN | InvoiceDraft |
| GET | /invoice-drafts/{id} | UC-17 | KETOAN | — |
| POST | /invoices/issue | UC-17 | KETOAN | Issue |
| GET | /invoices | UC-17 | SINHVIEN,KETOAN,ADMIN | — |
| POST | /payment-sessions | UC-08 | SINHVIEN | Payment |
| GET | /payment-sessions/{id} | UC-08 | SINHVIEN,KETOAN,ADMIN | — |
| POST | /repairs | UC-22 | SINHVIEN | Repair |
| GET | /repairs | UC-22 | SINHVIEN,ADMIN,KYTHUAT | — |
| POST | /repairs/{id}/assignment | UC-22 | ADMIN,KYTHUAT | Assignment |
| POST | /repairs/{id}/completion | UC-22 | KYTHUAT | RepairComplete |
| POST | /entries | UC-25 | BAOVE | Entry |
| GET | /entries | UC-25 | SINHVIEN,ADMIN,BAOVE | — |
| POST | /visitors | UC-25 | SINHVIEN,BAOVE | Visitor |
| POST | /visitors/{id}/approval | UC-25 | ADMIN | Approval |
| GET | /violations | UC-14 | SINHVIEN,ADMIN,BAOVE | — |
| POST | /discipline-decisions | UC-14 | ADMIN | Discipline |
| POST | /violations/{id}/appeals | UC-14 | SINHVIEN | Appeal |
| POST | /appeals/{id}/response | UC-14 | ADMIN | AppealResponse |
| GET | /activities | UC-30 | SINHVIEN,ADMIN | — |
| POST | /activities | UC-30 | ADMIN | Activity |
| POST | /activities/{id}/registrations | UC-30 | SINHVIEN | — |
| POST | /activity-registrations/{id}/cancellation | UC-30 | SINHVIEN | Version |
| POST | /activities/{id}/attendance | UC-30 | ADMIN | Attendance |
| POST | /activities/{id}/completion | UC-30 | ADMIN | Confirm |
| GET | /community-points | UC-30 | SINHVIEN,ADMIN | — |
| POST | /point-conversions | UC-30 | SINHVIEN | Convert |
| POST | /certificates | UC-30 | SINHVIEN | CertificateRequest |
| GET | /certificates/{id} | UC-30 | SINHVIEN | — |
| GET | /notifications | SUPPORT | ALL | — |
| GET | /certificates/verify/{token} | UC-30 |  | — |

## Trường response

List: `{items:[Resource],page,pageSize,total}`. Mutation: `{id,status,version,requestId}`; trường id là chuỗi biểu diễn bigint/UUID của đối tượng tạo. Các timestamp RFC3339, tiền integer VND.

| Resource | Trường ra |
|---|---|
| Session | authenticated, role, twoFactorVerified, csrfToken, idleExpiresAt |
| Room/bed | id, buildingId, roomType, gender, capacity, availableBeds; bed: id,number,status,version |
| Booking | id,bedId,studentId,status,expiresAt,profileState,version; Admin thêm fileIds,reason |
| Draft | id,version,month,rooms[{roomId,meter,price,discount,extras,total,warnings}],inputHash |
| Invoice | id,roomId,month,lines[{name,quantity,unitPrice,total}],discount,total,paid,balance,dueDate,status |
| Payment session | id,gateway,providerRef,amount,invoiceIds,status,payUrl,expiresAt; payUrl chỉ trong phiên của SV |
| Repair | id,roomId,title,priority,status,assignedTo,schedule,history,version; ảnh chỉ bằng fileIds |
| Entry/visitor | eventId,studentId,type,time,barrierStatus,violationId; visitor: id,hostStudentId,state,validFrom/To (không trả identity rõ) |
| Violation | id,studentId,semesterId,content,level,status,semesterCount,decisions,version |
| Activity | id,semesterId,title,startAt,endAt,capacity,remaining,points,status,version |
| Points | semesterId,earned,available,convertedDrl,ledger[{id,delta,kind,time}] |
| Certificate | id,status,downloadUrl khi SIGNED,sha256,signedAt; verify: valid,revoked,certificateId,signer,issuedAt |
| Notification | id,type,title,readAt,createdAt |

POST login trả session; payment-sessions trả CommandResult cùng payUrl/amount/expiresAt; invoice-drafts trả draft; certificates trả 202 khi job PENDING, GET đọc trạng thái. OpenAPI đã có schema response cụ thể theo tài nguyên và list; `/files/{id}` GET trả binary sau kiểm ownership. Không có public file URL.
