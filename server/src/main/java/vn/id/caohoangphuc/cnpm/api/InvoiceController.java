package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.InvoiceDraftDto;
import vn.id.caohoangphuc.cnpm.api.dto.IssueDto;
import vn.id.caohoangphuc.cnpm.service.InvoiceService;

/** HTTP actions for preparing and issuing monthly invoices. */
@Component
public class InvoiceController {
    private final InvoiceService invoices;
    private final ApiSupport api;

    /** Connects invoice routes to the service and shared HTTP request handling. */
    public InvoiceController(InvoiceService invoices, ApiSupport api) {
        this.invoices = invoices;
        this.api = api;
    }

    /** Calculates a draft snapshot without publishing invoices. */
    public ServerResponse preview(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> invoices.preview(
                actor, context, request.body(InvoiceDraftDto.class)));
    }

    /** Issues invoices only after the draft has been reviewed. */
    public ServerResponse issue(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> invoices.issue(
                actor, context, request.body(IssueDto.class)));
    }
}
