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

    public CommunityController(CommunityService community, ApiSupport api) {
        this.community = community;
        this.api = api;
    }

    /** Returns a page of activities from the community service. */
    public ServerResponse activities(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> community.activities(actor,
                request.param("page").map(Integer::parseInt).orElse(1),
                request.param("size").map(Integer::parseInt).orElse(20)));
    }
}
