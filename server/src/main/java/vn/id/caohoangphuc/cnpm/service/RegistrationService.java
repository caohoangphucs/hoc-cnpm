package vn.id.caohoangphuc.cnpm.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.persistence.service.BookingRepository;

public class RegistrationService {
  private final BookingRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ duyệt đăng ký chỗ ở. */
  public RegistrationService(BookingRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Duyệt hoặc từ chối yêu cầu sau khi khóa sinh viên, yêu cầu và giường liên quan. */
  public CommandResultDto decide(Actor actor, RequestContext request, int id, ApprovalDto input) {
    context.positive(id, "bookingId");
    context.validate(input);
    var result =
        context.write(
            actor,
            request,
            "bookings/" + id + "/decision",
            input,
            CommandResultDto.class,
            () -> processDecision(actor, request, id, input),
            "ADMIN");

    // Ghi nhận hết hạn đã commit trước khi trả lỗi để quản trị viên tải lại danh sách.
    if (result.status().equals("EXPIRED"))
      throw ServiceException.conflict("Booking expired; the hold was released");
    return result;
  }

  /** Khóa yêu cầu, xử lý hết hạn rồi áp dụng quyết định duyệt hoặc từ chối. */
  private CommandResultDto processDecision(
      Actor actor, RequestContext request, int id, ApprovalDto input) {
    var booking = lockBookingAndStudent(id);
    if (isExpiredHold(booking, context.now())) return expireBooking(actor, request, booking);

    context.version(booking.version(), input.version());
    if (!booking.status().equals("CHO_DUYET"))
      throw ServiceException.conflict("Booking is already processed");

    var bed = repository.lockBed(booking.bedId());
    requireApprovableBed(bed);
    String status =
        input.decision() == ApprovalDto.Decision.DUYET
            ? approveBooking(id, booking, bed)
            : rejectBooking(bed);
    return saveDecision(actor, request, id, booking, status, input.reason());
  }

  /** Đọc yêu cầu để tìm sinh viên, khóa sinh viên trước rồi khóa lại yêu cầu để cập nhật. */
  private BookingRepository.Booking lockBookingAndStudent(int id) {
    var found = repository.booking(id, false);
    if (found == null) throw ServiceException.notFound("Booking");
    repository.lockStudent(found.studentId());

    var booking = repository.booking(id, true);
    if (booking == null) throw ServiceException.notFound("Booking");
    return booking;
  }

  /** Giải phóng giường và ghi trạng thái hết hạn trước khi trả kết quả cho caller. */
  private CommandResultDto expireBooking(
      Actor actor, RequestContext request, BookingRepository.Booking booking) {
    var bed = repository.lockBed(booking.bedId());
    if (bed != null && bed.status().equals("GIU_CHO")) repository.bedStatus(bed.id(), "TRONG");
    repository.status(booking.id(), "HUY", "Hold expired");
    context.event(
        actor,
        request,
        "BOOKING_EXPIRED",
        Integer.toString(booking.id()),
        Map.of("studentId", booking.studentId()));
    return new CommandResultDto(
        Integer.toString(booking.id()), "EXPIRED", booking.version() + 1, request.requestId());
  }

  /** Tạo lưu trú và hợp đồng sau khi hồ sơ, giường và học kỳ đã qua kiểm tra. */
  private String approveBooking(
      int id, BookingRepository.Booking booking, BookingRepository.Bed bed) {
    requireValidProfileAndNoActiveResidency(id, booking);
    var window = repository.bookingWindow(id);
    var start = residencyStart(window, context.now());
    int residency = repository.createResidency(booking, start, window.endsOn());
    repository.createContract(residency, window.semesterId(), start, window.endsOn());
    repository.bedStatus(bed.id(), "DANG_O");
    repository.studentRoom(booking.studentId(), bed.roomId());
    updateRoomOccupancy(bed.roomId());
    return "DA_DUYET";
  }

  /** Giải phóng giường của yêu cầu bị từ chối. */
  private String rejectBooking(BookingRepository.Bed bed) {
    repository.bedStatus(bed.id(), "TRONG");
    return "TU_CHOI";
  }

  /** Tính lại số giường đang ở và cập nhật tình trạng phòng. */
  private void updateRoomOccupancy(String roomId) {
    int occupied = Math.toIntExact(repository.occupiedBeds(roomId));
    String status = occupied >= repository.capacity(roomId) ? "DAY" : "CON_CHO";
    repository.roomOccupancy(roomId, occupied, status);
  }

  /** Lưu trạng thái quyết định, người duyệt, sự kiện và version mới. */
  private CommandResultDto saveDecision(
      Actor actor,
      RequestContext request,
      int id,
      BookingRepository.Booking booking,
      String status,
      String reason) {
    repository.status(id, status, reason);
    repository.approver(id, actor.accountId());
    context.event(
        actor,
        request,
        "BOOKING_DECIDED",
        Integer.toString(id),
        Map.of("studentId", booking.studentId(), "status", status));
    return new CommandResultDto(
        Integer.toString(id), status, booking.version() + 1, request.requestId());
  }

  /** Xác định giữ chỗ đang chờ nhưng đã hết hạn. */
  private boolean isExpiredHold(BookingRepository.Booking booking, java.time.Instant now) {
    return booking.status().equals("CHO_DUYET") && !booking.expiresAt().isAfter(now);
  }

  /** Từ chối duyệt khi giường không còn được giữ hoặc phòng không dùng được. */
  private void requireApprovableBed(BookingRepository.Bed bed) {
    if (bed == null
        || !bed.status().equals("GIU_CHO")
        || Set.of("KHOA", "DANG_SUA").contains(bed.roomStatus()))
      throw ServiceException.conflict("Bed or room is unavailable");
  }

  /** Chặn duyệt nếu hồ sơ chưa đạt hoặc sinh viên đã có chỗ ở đang hoạt động. */
  private void requireValidProfileAndNoActiveResidency(
      int bookingId, BookingRepository.Booking booking) {
    if (!"VALID".equals(repository.profileState(bookingId)))
      throw ServiceException.invalid("A valid profile is required");

    if (repository.hasResidency(booking.studentId()))
      throw ServiceException.conflict("Student already has active residency");
  }

  /** Chọn hôm nay hoặc ngày bắt đầu học kỳ, miễn ngày kết thúc chưa qua. */
  private LocalDate residencyStart(BookingRepository.Window window, java.time.Instant now) {
    LocalDate today = LocalDate.ofInstant(now, ServiceContext.ZONE);
    if (window == null || today.isAfter(window.endsOn()))
      throw ServiceException.conflict("Semester has ended");

    return today.isBefore(window.startsOn()) ? window.startsOn() : today;
  }
}
