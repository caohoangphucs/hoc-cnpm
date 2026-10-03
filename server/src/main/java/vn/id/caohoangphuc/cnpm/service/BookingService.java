package vn.id.caohoangphuc.cnpm.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import vn.id.caohoangphuc.cnpm.api.dto.BookingDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.persistence.service.BookingRepository;

public class BookingService {
  private final BookingRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ đăng ký chỗ ở với repository và tiện ích nghiệp vụ dùng chung. */
  public BookingService(BookingRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Tạo yêu cầu đăng ký chỗ ở sau khi kiểm tra quyền và dữ liệu đầu vào. */
  public CommandResultDto book(Actor actor, RequestContext request, BookingDto input) {
    context.validate(input);
    context.text(input.bedId(), 20, "bedId");
    int windowId = Math.toIntExact(input.windowId());

    // Giữ khóa sinh viên trong transaction để không tạo hai chỗ giữ phòng cùng lúc.
    return context.write(
        actor,
        request,
        "bookings",
        input,
        CommandResultDto.class,
        () -> createBooking(actor, request, input, windowId),
        "SINHVIEN");
  }

  /** Kiểm tra điều kiện sinh viên và giường rồi tạo yêu cầu giữ chỗ 48 giờ. */
  private CommandResultDto createBooking(
      Actor actor, RequestContext request, BookingDto input, int windowId) {
    context.require(actor, "SINHVIEN");
    var student = repository.lockStudent(actor.accountId());
    if (student == null) throw ServiceException.notFound("Student");
    var now = context.now();

    requireStudentCanBook(actor, request, student, windowId, now);
    var bed = requireAvailableBed(input.bedId(), student);
    requireBookingDocuments(input.fileIds(), actor.accountId());
    return saveBooking(actor, request, input, student, bed, windowId, now);
  }

  /** Xóa giữ chỗ cũ đã hết hạn và chặn sinh viên đang có yêu cầu hoặc lưu trú khác. */
  private void requireStudentCanBook(
      Actor actor,
      RequestContext request,
      BookingRepository.Student student,
      int windowId,
      java.time.Instant now) {
    requireOpenWindow(repository.window(windowId), now);
    for (var booking : repository.pending(student.id())) {
      if (booking.expiresAt().isAfter(now))
        throw ServiceException.conflict("Student already has a pending booking");
      expire(actor, request, booking);
    }

    if (repository.hasResidency(student.id()))
      throw ServiceException.conflict("Student already has active residency");
  }

  /** Ghi yêu cầu, hồ sơ và trạng thái giường cùng transaction. */
  private CommandResultDto saveBooking(
      Actor actor,
      RequestContext request,
      BookingDto input,
      BookingRepository.Student student,
      BookingRepository.Bed bed,
      int windowId,
      java.time.Instant now) {
    int id = repository.create(student.id(), bed.id(), now, now.plus(Duration.ofHours(48)));
    repository.bedStatus(bed.id(), "GIU_CHO");
    repository.profile(id, windowId, context.json(input.fileIds()), now);
    context.attach(input.fileIds());
    context.event(
        actor,
        request,
        "BOOKING_CREATED",
        Integer.toString(id),
        Map.of("studentId", student.id(), "bedId", bed.id()));
    return new CommandResultDto(Integer.toString(id), "CHO_DUYET", 1L, request.requestId());
  }

  /** Hủy lượt giữ chỗ đã quá hạn và trả giường về trạng thái trống. */
  private void expire(Actor actor, RequestContext request, BookingRepository.Booking booking) {
    var bed = repository.lockBed(booking.bedId());
    if (bed != null && bed.status().equals("GIU_CHO")) repository.bedStatus(bed.id(), "TRONG");
    repository.status(booking.id(), "HUY", "Hold expired");
    context.event(
        actor,
        request,
        "BOOKING_EXPIRED",
        Integer.toString(booking.id()),
        Map.of("studentId", booking.studentId()));
  }

  /** Từ chối yêu cầu nếu đợt đăng ký hoặc học kỳ chưa mở hay đã đóng. */
  private void requireOpenWindow(BookingRepository.Window window, java.time.Instant now) {
    if (window == null
        || !window.status().equals("ACTIVE")
        || !window.semesterStatus().equals("ACTIVE")
        || now.isBefore(window.opensAt())
        || !now.isBefore(window.closesAt()))
      throw ServiceException.invalid("Registration window is closed");
  }

  /** Khóa giường rồi kiểm tra giới tính áp dụng và tình trạng giường/phòng. */
  private BookingRepository.Bed requireAvailableBed(
      String bedId, BookingRepository.Student student) {
    var bed = repository.lockBed(bedId);
    if (bed == null) throw ServiceException.notFound("Bed");

    if (!bed.gender().equals("TAT_CA") && !bed.gender().equals(student.gender()))
      throw ServiceException.invalid("Building gender does not match");

    if (!bed.status().equals("TRONG")
        || java.util.Set.of("KHOA", "DANG_SUA", "DAY").contains(bed.roomStatus()))
      throw ServiceException.conflict("Bed is not available");

    return bed;
  }

  /** Kiểm tra quyền sở hữu và loại giấy tờ bắt buộc của hồ sơ đăng ký. */
  private void requireBookingDocuments(List<java.util.UUID> fileIds, int accountId) {
    context.files(fileIds, accountId, "CCCD_FRONT", "CCCD_BACK", "STUDENT_CARD", "POLICY");
    var kinds =
        fileIds.stream().map(context::fileKind).collect(java.util.stream.Collectors.toSet());
    if (!kinds.containsAll(java.util.Set.of("CCCD_FRONT", "CCCD_BACK", "STUDENT_CARD")))
      throw ServiceException.invalid("Both identity-card sides and the student card are required");
  }

  /** Giải phóng các lượt giữ chỗ đã quá hạn theo giới hạn batch. */
  public int expireHolds(Actor actor, RequestContext request, int limit) {
    context.batchSize(limit);
    var ids =
        context.transaction(
            () -> {
              context.require(actor, "ADMIN");
              return repository.expired(context.now(), limit);
            });
    int expired = 0;
    for (int id : ids) {
      boolean changed =
          context.transaction(
              () -> {
                context.require(actor, "ADMIN");
                var found = repository.booking(id, false);
                if (found == null) return false;
                repository.lockStudent(found.studentId());
                var booking = repository.booking(id, true);
                if (!booking.status().equals("CHO_DUYET")
                    || booking.expiresAt().isAfter(context.now())) return false;
                expire(actor, request, booking);
                return true;
              });
      if (changed) expired++;
    }
    return expired;
  }
}
