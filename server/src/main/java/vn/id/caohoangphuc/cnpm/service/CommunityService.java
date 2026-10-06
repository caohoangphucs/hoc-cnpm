package vn.id.caohoangphuc.cnpm.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import vn.id.caohoangphuc.cnpm.api.dto.ActivityDto;
import vn.id.caohoangphuc.cnpm.api.dto.ActivityResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.AttendanceDto;
import vn.id.caohoangphuc.cnpm.api.dto.CertificateRequestDto;
import vn.id.caohoangphuc.cnpm.api.dto.CertificateResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.ConfirmDto;
import vn.id.caohoangphuc.cnpm.api.dto.ConvertDto;
import vn.id.caohoangphuc.cnpm.api.dto.LedgerLineDto;
import vn.id.caohoangphuc.cnpm.api.dto.PointsResourceDto;
import vn.id.caohoangphuc.cnpm.persistence.service.CommunityRepository;

public class CommunityService {
  private static final SecureRandom RANDOM = new SecureRandom();

  /** Identifies a student's registration and the QR token used for attendance. */
  public record RegistrationReceipt(
      long id, String activityId, String status, String qrToken, long version) {}

  /** Reports a requested points conversion and its current review state. */
  public record ConversionReceipt(
      String id, String status, int pointsSpent, int drl, String externalState) {}

  private final CommunityRepository repository;
  private final ServiceContext context;

  /** Connects community workflows to persistence and shared transaction handling. */
  public CommunityService(CommunityRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Lists activities after checking the caller's role and requested page. */
  public List<ActivityResourceDto> activities(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(
        () -> {
          context.require(actor, "SINHVIEN", "ADMIN");
          return repository.activities(page, size).stream().map(this::resource).toList();
        });
  }

  /** Phiếu tra cứu có thông tin hoạt động, không tiết lộ hash mã điểm danh. */
  public record RegistrationResource(ActivityResourceDto activity, RegistrationReceipt receipt,
      boolean cancelled) {}

  /** Lấy lịch sử từ database theo tài khoản hiện tại, không nhận mã sinh viên từ client. */
  public List<RegistrationResource> registrations(Actor actor) {
    return context.transaction(() -> {
      String student = context.require(actor, "SINHVIEN").studentId();
      return repository.registrations(student).stream().map(r -> new RegistrationResource(
          resource(repository.activityByCode(r.activityId(), false)),
          new RegistrationReceipt(r.id(), r.activityId(), r.status(), "", r.version()),
          r.status().equals("CANCELLED"))).toList();
    });
  }

  /** Cấp lại mã cho chủ phiếu đang đăng ký; mã cũ sẽ hết hiệu lực. */
  public RegistrationReceipt attendanceToken(Actor actor, RequestContext request, int id) {
    context.positive(id, "registrationId");
    return context.write(actor, request, "activity-registrations/" + id + "/token", id,
        RegistrationReceipt.class, () -> {
          String student = context.require(actor, "SINHVIEN").studentId();
          repository.lockStudent(student);
          var r = repository.registration(id, true);
          if (r == null || !r.studentId().equals(student)) throw ServiceException.notFound("Registration");
          if (!r.status().equals("REGISTERED")) throw ServiceException.conflict("Registration is not active");
          String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(random(32));
          repository.attendanceToken(id, token);
          return new RegistrationReceipt(id, r.activityId(), r.status(), token, r.version());
        }, "SINHVIEN");
  }

  /** Creates an activity and writes its audit event in the same transaction. */
  public ActivityResourceDto createActivity(
      Actor actor, RequestContext request, ActivityDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "activities",
        input,
        ActivityResourceDto.class,
        () -> {
          context.require(actor, "ADMIN");
          requireFutureActivity(input);
          var activity =
              repository.create(
                  input.semesterId(),
                  input.title(),
                  input.startAt().toInstant(),
                  input.endAt().toInstant(),
                  Math.toIntExact(input.capacity()),
                  Math.toIntExact(input.points()));
          context.event(
              actor, request, "ACTIVITY_CREATED", activity.id(), Map.of("title", input.title()));
          return resource(activity);
        },
        "ADMIN");
  }

  /** Replaces activity details before students have created any registration history. */
  public ActivityResourceDto updateActivity(
      Actor actor, RequestContext request, long id, ActivityDto input) {
    context.validate(input);
    context.positive(id, "activityId");
    return context.write(actor, request, "PUT:/api/v1/activities/" + id, input,
        ActivityResourceDto.class, () -> {
          context.require(actor, "ADMIN");
          var current = repository.activity(id, true);
          if (current == null) throw ServiceException.notFound("Activity");
          if (repository.hasRegistrationHistory(current.id()))
            throw ServiceException.conflict("Activity with registration history cannot be edited");
          requireFutureActivity(input);
          repository.updateActivity(id, input.semesterId(), input.title(), input.startAt().toInstant(),
              input.endAt().toInstant(), Math.toIntExact(input.capacity()), Math.toIntExact(input.points()));
          context.event(actor, request, "ACTIVITY_UPDATED", current.id(), Map.of("title", input.title()));
          return resource(repository.activity(id, false));
        }, "ADMIN");
  }

  /** Deletes an activity only before its registration history begins. */
  public CommandResultDto deleteActivity(Actor actor, RequestContext request, long id) {
    context.positive(id, "activityId");
    return context.write(actor, request, "DELETE:/api/v1/activities/" + id, id,
        CommandResultDto.class, () -> {
          context.require(actor, "ADMIN");
          var current = repository.activity(id, true);
          if (current == null) throw ServiceException.notFound("Activity");
          if (repository.hasRegistrationHistory(current.id()))
            throw ServiceException.conflict("Activity with registration history cannot be deleted");
          repository.deleteActivity(id);
          context.event(actor, request, "ACTIVITY_DELETED", current.id(), Map.of("id", id));
          return new CommandResultDto(Long.toString(id), "COMPLETED", current.version() + 1,
              request.requestId());
        }, "ADMIN");
  }

  /** Registers a student after checking capacity, duplicate registration and schedule conflicts. */
  public RegistrationReceipt register(Actor actor, RequestContext request, long activityPublicId) {
    context.positive(activityPublicId, "activityId");
    return context.write(
        actor,
        request,
        "activities/registrations",
        activityPublicId,
        RegistrationReceipt.class,
        () -> {
          var account = context.require(actor, "SINHVIEN");

          // Khóa sinh viên và hoạt động trước khi kiểm tra chỗ trống, trùng giờ, đăng ký lặp.
          repository.lockStudent(account.studentId());
          var activity = repository.activity(activityPublicId, true);
          if (activity == null) throw ServiceException.notFound("Activity");
          requireCanRegister(activity, account.studentId());
          String token =
              java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(random(32));
          int id = repository.register(activity.id(), account.studentId(), token);
          context.event(
              actor,
              request,
              "COMMUNITY_REGISTERED",
              Integer.toString(id),
              Map.of("activityId", activity.id()));
          return new RegistrationReceipt(id, activity.id(), "REGISTERED", token, 1);
        },
        "SINHVIEN");
  }

  /** Cancels an eligible registration and releases one activity place. */
  public CommandResultDto cancel(Actor actor, RequestContext request, int id, long version) {
    var input = new vn.id.caohoangphuc.cnpm.api.dto.VersionDto(version);
    context.validate(input);
    return context.write(
        actor,
        request,
        "activity-registrations/" + id + "/cancellation",
        input,
        CommandResultDto.class,
        () -> {
          var student = context.require(actor, "SINHVIEN").studentId();
          repository.lockStudent(student);
          var registration = repository.registration(id, true);
          if (registration == null || !registration.studentId().equals(student))
            throw ServiceException.notFound("Registration");

          context.version(registration.version(), version);
          var activity = repository.activityByCode(registration.activityId(), true);
          if (activity == null) throw ServiceException.notFound("Activity");

          requireCanCancel(registration, activity);
          repository.state(id, "CANCELLED");
          repository.capacity(activity.id(), -1);
          context.event(
              actor,
              request,
              "COMMUNITY_CANCELLED",
              Integer.toString(id),
              Map.of("activityId", activity.id()));
          return new CommandResultDto(
              Integer.toString(id), "CANCELLED", version + 1, request.requestId());
        },
        "SINHVIEN");
  }

  /** Marks a registration attended after validating its one-time QR token. */
  public CommandResultDto attend(Actor actor, RequestContext request, AttendanceDto input) {
    context.validate(input);
    String hash = ServiceContext.hash(input.qrToken());
    return context.write(
        actor,
        request,
        "activities/attendance",
        input,
        CommandResultDto.class,
        () -> {
          context.require(actor, "ADMIN");
          var registration = repository.registrationByToken(hash, true);
          if (registration == null) throw ServiceException.notFound("Registration QR");
          if (!registration.status().equals("REGISTERED"))
            throw ServiceException.conflict("Registration is not active");
          var activity = repository.activityByCode(registration.activityId(), false);
          if (activity == null) throw ServiceException.notFound("Activity");
          var now = context.now();
          if (now.isBefore(activity.start()) || now.isAfter(activity.end()))
            throw ServiceException.conflict("Attendance can only be recorded during the activity");
          repository.state(registration.id(), "ATTENDED");
          context.event(
              actor,
              request,
              "COMMUNITY_ATTENDED",
              Integer.toString(registration.id()),
              Map.of("activityId", registration.activityId()));
          return new CommandResultDto(
              Integer.toString(registration.id()),
              "ATTENDED",
              registration.version() + 1,
              request.requestId());
        },
        "ADMIN");
  }

  /** Completes a batch of attended registrations in a stable lock order. */
  public List<CommandResultDto> confirm(Actor actor, RequestContext request, ConfirmDto input) {
    context.validate(input);
    var result =
        context.write(
            actor,
            request,
            "activities/completion",
            input,
            CommandResultDto[].class,
            () -> completeRegistrations(actor, request, input),
            "ADMIN");
    return java.util.Arrays.asList(result);
  }

  /** Khóa mọi sinh viên rồi xác nhận toàn bộ danh sách trong cùng transaction. */
  private CommandResultDto[] completeRegistrations(
      Actor actor, RequestContext request, ConfirmDto input) {
    context.require(actor, "ADMIN");
    var registrations = loadRegistrations(input.registrationIds());
    lockRegistrationStudents(registrations);

    var results = new java.util.ArrayList<CommandResultDto>();
    for (var before : registrations) {
      var current = repository.registration(before.id(), true);
      results.add(completeRegistration(actor, request, current));
    }
    return results.toArray(CommandResultDto[]::new);
  }

  /** Đọc trước các đăng ký theo ID tăng dần để khóa sinh viên theo cùng thứ tự. */
  private List<CommunityRepository.Registration> loadRegistrations(List<Long> ids) {
    var registrations =
        ids.stream()
            .map(Math::toIntExact)
            .sorted()
            .map(id -> repository.registration(id, false))
            .toList();
    if (registrations.stream().anyMatch(java.util.Objects::isNull))
      throw ServiceException.notFound("Registration");
    return registrations;
  }

  /** Khóa từng sinh viên theo thứ tự cố định để tránh deadlock giữa các batch. */
  private void lockRegistrationStudents(List<CommunityRepository.Registration> registrations) {
    registrations.stream()
        .map(CommunityRepository.Registration::studentId)
        .distinct()
        .sorted()
        .forEach(repository::lockStudent);
  }

  /** Chỉ hoàn tất lượt đã điểm danh; repository cộng điểm theo mã đăng ký đúng một lần. */
  private CommandResultDto completeRegistration(
      Actor actor, RequestContext request, CommunityRepository.Registration registration) {
    if (!registration.status().equals("ATTENDED"))
      throw ServiceException.conflict("Only attended registrations can be completed");
    var activity = repository.activityByCode(registration.activityId(), false);
    if (activity == null) throw ServiceException.notFound("Activity");
    if (context.now().isBefore(activity.end()))
      throw ServiceException.conflict("Activity cannot be completed before it ends");

    repository.confirm(registration.id(), actor.accountId(), context.now());
    context.event(
        actor,
        request,
        "COMMUNITY_COMPLETED",
        Integer.toString(registration.id()),
        Map.of("studentId", registration.studentId()));
    return new CommandResultDto(
        Integer.toString(registration.id()),
        "COMPLETED",
        registration.version() + 1,
        request.requestId());
  }

  /** Calculates a student's earned, available and converted points for one semester. */
  public PointsResourceDto points(Actor actor, long semester) {
    return context.transaction(
        () -> {
          var student = context.require(actor, "SINHVIEN").studentId();
          String code = repository.studentSemester(student, semester);
          var ledger = repository.ledger(student, code);
          long earned =
              ledger.stream()
                  .filter(x -> x.delta() > 0)
                  .mapToLong(CommunityRepository.Ledger::delta)
                  .sum();
          long available = ledger.stream().mapToLong(CommunityRepository.Ledger::delta).sum();
          int converted = repository.conversionDrl(student, Long.toString(semester));
          return new PointsResourceDto(
              semester,
              earned,
              Math.max(0, available),
              (long) converted,
              ledger.stream()
                  .map(
                      x ->
                          new LedgerLineDto(
                              x.id(),
                              x.delta(),
                              x.type(),
                              java.time.OffsetDateTime.ofInstant(x.at(), ServiceContext.ZONE)))
                  .toList());
        });
  }

  /** Converts available community points into conduct points within the semester limit. */
  public ConversionReceipt convert(Actor actor, RequestContext request, ConvertDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "point-conversions",
        input,
        ConversionReceipt.class,
        () -> {
          var student = context.require(actor, "SINHVIEN").studentId();
          repository.lockStudent(student);
          String code = repository.studentSemester(student, input.semesterId());
          var ledger = repository.ledger(student, code);
          long balance = ledger.stream().mapToLong(CommunityRepository.Ledger::delta).sum();
          int converted = repository.conversionDrl(student, Long.toString(input.semesterId()));

          // Mỗi học kỳ đổi tối đa 5 điểm rèn luyện, theo tỷ lệ 10 điểm công ích / 1 điểm.
          int spend = pointsToConvert(balance, converted, input.drl());
          UUID transfer = UUID.randomUUID();
          int conversion =
              repository.createConversion(
                  transfer, student, Long.toString(input.semesterId()), spend, input.drl());
          repository.debit(student, Long.toString(input.semesterId()), spend, conversion);
          context.event(
              actor,
              request,
              "POINTS_CONVERSION_REQUESTED",
              transfer.toString(),
              Map.of("studentId", student, "points", spend, "drl", input.drl()));
          return new ConversionReceipt(
              transfer.toString(), "PENDING", spend, input.drl(), "PENDING");
        },
        "SINHVIEN");
  }

  /** Creates a certificate request from a snapshot of completed activities. */
  public CertificateResourceDto requestCertificate(
      Actor actor, RequestContext request, CertificateRequestDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "certificates",
        input,
        CertificateResourceDto.class,
        () -> {
          var account = context.require(actor, "SINHVIEN");
          String student = account.studentId();
          String snapshot =
              repository.completedSnapshot(student, Long.toString(input.semesterId()));
          if ("[]".equals(snapshot))
            throw ServiceException.invalid("There are no completed activities to certify");
          UUID id = UUID.randomUUID();
          String token = verificationToken(id);

          // Snapshot là dữ liệu tại thời điểm yêu cầu; chứng nhận chỉ có hiệu lực sau khi ký.
          repository.certificate(
              id,
              account.id(),
              student,
              Long.toString(input.semesterId()),
              snapshot,
              ServiceContext.hash(token));
          context.event(
              actor, request, "CERTIFICATE_REQUESTED", id.toString(), Map.of("studentId", student));
          return new CertificateResourceDto(id, "PENDING", null, null, null);
        },
        "SINHVIEN");
  }

  /** Signs a certificate verification token using the configured server key. */
  private String verificationToken(UUID id) {
    try {
      String key = System.getenv("KTX_CERTIFICATE_TOKEN_KEY");
      if (key == null)
        throw new ServiceException(
            503, "CERTIFICATE_SIGNER_UNAVAILABLE", "Certificate signing is not configured");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return java.util.Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(mac.doFinal(id.toString().getBytes(StandardCharsets.UTF_8)));
    } catch (ServiceException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IllegalStateException("Certificate token generation failed", exception);
    }
  }

  /** Rejects registration if the activity is closed, full, duplicated or overlapping. */
  private void requireCanRegister(CommunityRepository.Activity activity, String studentId) {
    if (!activity.status().equals("MO_DANG_KY") || !activity.start().isAfter(context.now()))
      throw new ServiceException(409, "ACTIVITY_CLOSED", "Activity registration is closed");

    if (activity.registered() >= activity.capacity())
      throw new ServiceException(409, "ACTIVITY_FULL", "Activity is full");

    if (repository.registered(studentId, activity.id()))
      throw new ServiceException(409, "ALREADY_REGISTERED", "Already registered");

    if (repository.overlaps(studentId, activity.start(), activity.end()) > 0)
      throw new ServiceException(409, "ACTIVITY_TIME_CONFLICT", "Activity time conflicts with another registration");
  }

  /** Requires an activity to start in the future and have a positive time range. */
  private void requireFutureActivity(ActivityDto input) {
    boolean validRange = input.endAt().isAfter(input.startAt());
    boolean startsInFuture = input.startAt().toInstant().isAfter(context.now());
    if (!validRange || !startsInFuture)
      throw ServiceException.invalid("Activity must have a future start and a valid time range");
  }

  /** Applies the rule that cancellation closes twelve hours before the activity begins. */
  private void requireCanCancel(
      CommunityRepository.Registration registration, CommunityRepository.Activity activity) {
    if (!registration.status().equals("REGISTERED"))
      throw ServiceException.conflict("Registration is not active");

    Duration untilStart = Duration.between(context.now(), activity.start());
    if (untilStart.compareTo(Duration.ofHours(12)) < 0)
      throw ServiceException.conflict("Cancellation is allowed until 12 hours before the activity");
  }

  /** Converts requested conduct points to community-point cost within the remaining limit. */
  private int pointsToConvert(long balance, int alreadyConverted, int requestedDrl) {
    int remainingLimit = 5 - alreadyConverted;
    int maximum = (int) Math.min(balance / 10, remainingLimit);
    if (maximum <= 0 || requestedDrl > maximum)
      throw ServiceException.conflict("Not enough points or semester conversion limit reached");

    return requestedDrl * 10;
  }

  /** Maps the repository activity row to the public response, including remaining capacity. */
  private ActivityResourceDto resource(CommunityRepository.Activity a) {
    return new ActivityResourceDto(
        a.publicId(),
        a.semesterId(),
        a.title(),
        java.time.OffsetDateTime.ofInstant(a.start(), ServiceContext.ZONE),
        java.time.OffsetDateTime.ofInstant(a.end(), ServiceContext.ZONE),
        (long) a.capacity(),
        (long) (a.capacity() - a.registered()),
        (long) a.points(),
        a.status(),
        a.version());
  }

  /** Generates the unpredictable token embedded in a registration QR code. */
  private byte[] random(int size) {
    byte[] bytes = new byte[size];
    RANDOM.nextBytes(bytes);
    return bytes;
  }
}
