package vn.id.caohoangphuc.cnpm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import vn.id.caohoangphuc.cnpm.api.dto.AppealDto;
import vn.id.caohoangphuc.cnpm.api.dto.AppealResponseDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.DecisionsItemDto;
import vn.id.caohoangphuc.cnpm.api.dto.DisciplineDto;
import vn.id.caohoangphuc.cnpm.api.dto.ViolationResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.ViolationWriteDto;
import vn.id.caohoangphuc.cnpm.api.dto.ViolationUpdateDto;
import vn.id.caohoangphuc.cnpm.persistence.service.DisciplineRepository;

public class DisciplineService {
  private final DisciplineRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ xử lý vi phạm và khiếu nại. */
  public DisciplineService(DisciplineRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Liệt kê các vi phạm đang chờ xử lý theo trang. */
  public List<ViolationResourceDto> pending(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(
        () -> {
          context.require(actor, "ADMIN");
          return repository.pending(page, size).stream().map(v -> resource(v)).toList();
        });
  }

  /** Lists all violations for administrators, including processed records. */
  public List<ViolationResourceDto> all(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(() -> {
      context.require(actor, "ADMIN");
      return repository.all(page, size).stream().map(this::resource).toList();
    });
  }

  /** Returns a single violation to an administrator. */
  public ViolationResourceDto get(Actor actor, long id) {
    context.positive(id, "violationId");
    return context.transaction(() -> {
      context.require(actor, "ADMIN");
      var rows = repository.violations(List.of(id), false);
      if (rows.isEmpty()) throw ServiceException.notFound("Violation");
      return resource(rows.get(0));
    });
  }

  /** Creates a violation for a student on an incident date within a configured semester. */
  public ViolationResourceDto create(Actor actor, RequestContext request, ViolationWriteDto input) {
    context.validate(input);
    return context.write(actor, request, "violations", input, ViolationResourceDto.class, () -> {
      context.require(actor, "ADMIN");
      Instant occurredAt = input.occurredAt() == null ? context.now() : input.occurredAt().toInstant();
      if (!repository.canCreate(input.studentId(), occurredAt))
        throw ServiceException.invalid("Student or incident date is not valid for a configured semester");
      int id = repository.create(input.studentId(), input.content(), input.level(), occurredAt);
      var violation = repository.violations(List.of((long) id), false).get(0);
      context.event(actor, request, "VIOLATION_CREATED", Integer.toString(id),
          Map.of("studentId", input.studentId()));
      return resource(violation);
    }, "ADMIN");
  }

  /** Updates the editable details while preserving recorded decision revisions. */
  public ViolationResourceDto update(Actor actor, RequestContext request, long id, ViolationUpdateDto input) {
    context.validate(input);
    context.positive(id, "violationId");
    return context.write(actor, request, "violations/" + id, input, ViolationResourceDto.class, () -> {
      context.require(actor, "ADMIN");
      var rows = repository.violations(List.of(id), true);
      if (rows.isEmpty()) throw ServiceException.notFound("Violation");
      if (!"CHUA_XU_LY".equals(rows.get(0).status()))
        throw ServiceException.conflict("Only an unprocessed violation can be edited");
      context.version(rows.get(0).version(), input.version());
      if (!repository.update((int) id, input.content(), input.level()))
        throw ServiceException.notFound("Violation");
      var updated = repository.violations(List.of(id), false).get(0);
      context.event(actor, request, "VIOLATION_UPDATED", Long.toString(id), Map.of());
      return resource(updated);
    }, "ADMIN");
  }

  /** Deletes a manual violation only before it is processed or linked to an entry. */
  public CommandResultDto delete(Actor actor, RequestContext request, long id) {
    context.positive(id, "violationId");
    return context.write(actor, request, "DELETE:/api/v1/violations/" + id, id,
        CommandResultDto.class, () -> {
          context.require(actor, "ADMIN");
          var rows = repository.violations(List.of(id), true);
          if (rows.isEmpty()) throw ServiceException.notFound("Violation");
          if (!"CHUA_XU_LY".equals(rows.get(0).status()))
            throw ServiceException.conflict("Only an unprocessed violation can be deleted");
          if (!repository.delete((int) id))
            throw ServiceException.conflict("Violation has a gate entry or decision history");
          context.event(actor, request, "VIOLATION_DELETED", Long.toString(id), Map.of());
          return new CommandResultDto(Long.toString(id), "DELETED", 1L, request.requestId());
        }, "ADMIN");
  }

  /** Ghi các quyết định xử lý vi phạm theo batch. */
  public List<ViolationResourceDto> decide(
      Actor actor, RequestContext request, DisciplineDto input) {
    context.validate(input);
    var result =
        context.write(
            actor,
            request,
            "discipline-decisions",
            input,
            ViolationResourceDto[].class,
            () -> applyDecisionBatch(actor, request, input),
            "ADMIN");
    return java.util.Arrays.asList(result);
  }

  /** Khóa và kiểm tra toàn batch trước, sau đó ghi các quyết định cùng transaction. */
  private ViolationResourceDto[] applyDecisionBatch(
      Actor actor, RequestContext request, DisciplineDto input) {
    context.require(actor, "ADMIN");
    var ids = input.violationIds().stream().sorted().toList();
    for (String student : repository.studentIds(ids)) repository.lockStudent(student);
    var violations = repository.violations(ids, true);
    validateDecisionBatch(ids, violations, input.versions());
    context.files(List.of(input.evidenceFileId()), actor.accountId(), "EVIDENCE");

    for (var violation : violations) applyDecision(actor, request, input, violation);

    // Gắn chứng cứ sau khi mọi quyết định trong batch đã được ghi.
    context.attach(List.of(input.evidenceFileId()));
    // Trả thống kê sau khi toàn bộ batch đã cập nhật, cùng trạng thái với GET.
    return repository.violations(ids, false).stream().map(this::resource)
        .toArray(ViolationResourceDto[]::new);
  }

  /** Ghi một quyết định mới, giữ lại các revision trước và phát sự kiện audit. */
  private void applyDecision(
      Actor actor,
      RequestContext request,
      DisciplineDto input,
      DisciplineRepository.Violation violation) {
    int revision = repository.nextRevision(violation.id());
    repository.decide(
        violation.id(),
        violation.semesterId(),
        revision,
        actor.accountId(),
        input.decision(),
        input.reason(),
        input.evidenceFileId(),
        context.now());
    context.event(
        actor,
        request,
        "DISCIPLINE_DECIDED",
        Integer.toString(violation.id()),
        Map.of(
            "studentId",
            violation.studentId(),
            "revision",
            revision,
            "repeated",
            repository.violations(List.of((long) violation.id()), false).get(0).count() >= 3));
  }

  /** Đọc lịch sử revision và tạo response cho một vi phạm. */
  private ViolationResourceDto resource(DisciplineRepository.Violation v) {
    var decisions =
        repository.decisions(v.id()).stream()
            .map(
                d ->
                    new DecisionsItemDto(
                        d.id(),
                        (long) d.revision(),
                        d.decision(),
                        d.reason(),
                        (long) d.actorId(),
                        java.time.OffsetDateTime.ofInstant(d.createdAt(), ServiceContext.ZONE), d.evidenceFileId()))
            .toList();
    return new ViolationResourceDto(
        (long) v.id(),
        v.studentId(),
        v.semesterId(),
        v.content(),
        v.level(),
        v.status(),
        v.count(),
        decisions,
        v.version());
  }

  /** Kiểm tra toàn bộ vi phạm còn chờ và version trước khi ghi quyết định batch. */
  private void validateDecisionBatch(
      List<Long> ids,
      List<DisciplineRepository.Violation> violations,
      Map<String, Long> expectedVersions) {
    if (violations.size() != ids.size()) throw ServiceException.notFound("Violation");

    for (var violation : violations) {
      if (!"CHUA_XU_LY".equals(violation.status()))
        throw ServiceException.conflict("A violation has already been processed");

      Long expectedVersion = expectedVersions.get(Integer.toString(violation.id()));
      if (expectedVersion == null) throw ServiceException.invalid("Missing violation version");
      context.version(violation.version(), expectedVersion);
    }
  }

  /** Tạo khiếu nại của sinh viên đối với một quyết định. */
  public CommandResultDto appeal(
      Actor actor, RequestContext request, long decisionId, AppealDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "decisions/" + decisionId + "/appeals",
        input,
        CommandResultDto.class,
        () -> {
          var student = context.require(actor, "SINHVIEN").studentId();
          if (repository.decisionStudent(decisionId, student) != 1)
            throw ServiceException.notFound("Decision");
          long id = repository.createAppeal(decisionId, student, input.content());
          context.event(
              actor,
              request,
              "DISCIPLINE_APPEAL",
              Long.toString(id),
              Map.of("decisionId", decisionId));
          return new CommandResultDto(Long.toString(id), "OPEN", 1L, request.requestId());
        },
        "SINHVIEN");
  }

  /** Trả lời khiếu nại và tạo revision mới nếu khiếu nại được chấp nhận. */
  public CommandResultDto respond(
      Actor actor, RequestContext request, long appealId, AppealResponseDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "appeals/" + appealId + "/response",
        input,
        CommandResultDto.class,
        () -> {
          context.require(actor, "ADMIN");
          var appeal = repository.appeal(appealId, true);
          if (appeal == null) throw ServiceException.notFound("Appeal");
          context.version(appeal.version(), input.version());
          if (!appeal.state().equals("OPEN"))
            throw ServiceException.conflict("Appeal was already answered");
          String state = input.accepted() ? "ACCEPTED" : "REJECTED";
          repository.respond(appealId, state, input.reason(), actor.accountId());

          // Khi chấp nhận khiếu nại, tạo revision mới thay vì ghi đè quyết định cũ.
          if (input.accepted()) {
            var violation =
                repository
                    .violations(
                        List.of((long) repository.violationIdForDecision(appeal.decisionId())),
                        true)
                    .get(0);
            int revision = repository.nextRevision(violation.id());
            repository.decide(
                violation.id(),
                violation.semesterId(),
                revision,
                actor.accountId(),
                "REVIEW: " + input.reason(),
                input.reason(),
                repository.evidence(appeal.decisionId()),
                context.now());
          }
          context.event(
              actor,
              request,
              "DISCIPLINE_APPEAL_ANSWERED",
              Long.toString(appealId),
              Map.of("status", state));
          return new CommandResultDto(
              Long.toString(appealId), state, appeal.version() + 1, request.requestId());
        },
        "ADMIN");
  }
}
