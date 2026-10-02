package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
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
}
