package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.VisitorDto;
import vn.id.caohoangphuc.cnpm.service.GateService;

/** HTTP actions for security staff recording student entry and exit. */
@Component
public class GateController {
    private final GateService gate;
    private final ApiSupport api;

    /** Connects gate routes to the service and shared HTTP request handling. */
    public GateController(GateService gate, ApiSupport api) {
        this.gate = gate;
        this.api = api;
    }

    /** Trả gợi ý sinh viên theo phạm vi tài khoản, tìm theo MSSV hoặc họ tên. */
    public ServerResponse students(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.students(actor, request.param("search").orElse("")));
    }

    /** Records a card event; the service owns duplicate and card-validity checks. */
    public ServerResponse record(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.record(
                actor, context, request.body(EntryDto.class)));
    }

    /** Nhận lượt xác minh trực tiếp, quyền và thẻ được kiểm tra lại trong service. */
    public ServerResponse recordManual(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.recordManual(actor, context,
                request.body(vn.id.caohoangphuc.cnpm.api.dto.ManualEntryDto.class)));
    }

    /** Lists gate entries visible to the authenticated user. */
    public ServerResponse entries(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.entries(actor,
                api.integerParameter(request, "page", 1), api.integerParameter(request, "size", 20)));
    }

    /** Reads one gate entry by its event UUID. */
    public ServerResponse entry(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.entry(actor,
                java.util.UUID.fromString(request.pathVariable("id"))));
    }

    /** Submits a visit request for the authenticated student. */
    public ServerResponse declareVisitor(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.declareVisitor(actor, context,
                request.body(VisitorDto.class)));
    }

    /** Lists visitor requests within the caller's permitted scope. */
    public ServerResponse visitors(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.visitors(actor,
                api.integerParameter(request, "page", 1), api.integerParameter(request, "size", 20)));
    }

    /** Reads one visitor request within the caller's permitted scope. */
    public ServerResponse visitor(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.visitor(actor,
                Long.parseLong(request.pathVariable("id"))));
    }

    /** Deletes a pending visitor request before an administrator approves it. */
    public ServerResponse deleteVisitor(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.deleteVisitor(actor, context,
                Long.parseLong(request.pathVariable("id"))));
    }

    /** Approves or rejects a visitor request as an administrator. */
    public ServerResponse approveVisitor(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.approveVisitor(actor, context,
                Long.parseLong(request.pathVariable("id")), request.body(ApprovalDto.class)));
    }

    /** Records a visitor's arrival at the gate. */
    public ServerResponse checkVisitorIn(ServerRequest request) throws Exception {
        return checkVisitor(request, true);
    }

    /** Records a visitor's departure from the gate. */
    public ServerResponse checkVisitorOut(ServerRequest request) throws Exception {
        return checkVisitor(request, false);
    }

    /** Shares path parsing and service dispatch for the two gate directions. */
    private ServerResponse checkVisitor(ServerRequest request, boolean entering) throws Exception {
        return api.handle(request, (actor, context) -> gate.checkVisitor(actor, context,
                Long.parseLong(request.pathVariable("id")), entering));
    }
}
