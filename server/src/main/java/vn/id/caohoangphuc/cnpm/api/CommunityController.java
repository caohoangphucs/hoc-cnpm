package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.ActivityDto;
import vn.id.caohoangphuc.cnpm.api.dto.AttendanceDto;
import vn.id.caohoangphuc.cnpm.api.dto.CertificateRequestDto;
import vn.id.caohoangphuc.cnpm.api.dto.ConfirmDto;
import vn.id.caohoangphuc.cnpm.api.dto.ConvertDto;
import vn.id.caohoangphuc.cnpm.api.dto.VersionDto;
import vn.id.caohoangphuc.cnpm.service.CommunityService;

/** HTTP query for activities available to students and administrators. */
@Component
public class CommunityController {
    private final CommunityService community;
    private final ApiSupport api;

    /** Connects activity routes to the service and shared HTTP request handling. */
    public CommunityController(CommunityService community, ApiSupport api) {
        this.community = community;
        this.api = api;
    }

    /** Returns a page of activities from the community service. */
    public ServerResponse activities(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.activities(actor,
                api.integerParameter(request, "page", 1),
                api.integerParameter(request, "size", 20)));
    }

    /** Publishes a new activity for student registration. */
    public ServerResponse createActivity(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.createActivity(actor, context,
                request.body(ActivityDto.class)));
    }

    /** Replaces an activity before any registration history is created. */
    public ServerResponse updateActivity(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.updateActivity(actor, context,
                Long.parseLong(request.pathVariable("id")), request.body(ActivityDto.class)));
    }

    /** Deletes an activity before any registration history is created. */
    public ServerResponse deleteActivity(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.deleteActivity(actor, context,
                Long.parseLong(request.pathVariable("id"))));
    }

    /** Registers the authenticated student for a selected activity. */
    public ServerResponse register(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.register(actor, context,
                Long.parseLong(request.pathVariable("id"))));
    }

    /** Cancels a registration using the version returned when it was created. */
    public ServerResponse cancel(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.cancel(actor, context,
                Integer.parseInt(request.pathVariable("id")), request.body(VersionDto.class).version()));
    }

    /** Records attendance using the one-time QR token. */
    public ServerResponse attend(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.attend(actor, context,
                request.body(AttendanceDto.class)));
    }

    /** Confirms completed registrations as one atomic administrative batch. */
    public ServerResponse confirm(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.confirm(actor, context,
                request.body(ConfirmDto.class)));
    }

    /** Returns the authenticated student's activity points for one semester. */
    public ServerResponse points(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.points(actor,
                Long.parseLong(request.param("semesterId").orElseThrow())));
    }

    /** Converts eligible community points into conduct points. */
    public ServerResponse convert(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.convert(actor, context,
                request.body(ConvertDto.class)));
    }

    /** Requests a certificate based on completed activities in one semester. */
    public ServerResponse requestCertificate(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.requestCertificate(actor, context,
                request.body(CertificateRequestDto.class)));
    }
}
