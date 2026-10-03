package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.service.DisciplineService;

/** HTTP query for the administrator's unresolved violation queue. */
@Component
public class DisciplineController {
    private final DisciplineService discipline;
    private final ApiSupport api;

    public DisciplineController(DisciplineService discipline, ApiSupport api) {
        this.discipline = discipline;
        this.api = api;
    }

    /** Lists pending violations visible to an administrator. */
    public ServerResponse pending(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> discipline.pending(actor,
                request.param("page").map(Integer::parseInt).orElse(1),
                request.param("size").map(Integer::parseInt).orElse(20)));
    }
}
