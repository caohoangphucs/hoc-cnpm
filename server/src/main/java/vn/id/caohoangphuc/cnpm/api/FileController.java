package vn.id.caohoangphuc.cnpm.api;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.service.FileService;
import vn.id.caohoangphuc.cnpm.service.ServiceException;

/** Chuyển multipart và tải ảnh thành lời gọi service có kiểm tra phiên/quyền. */
@Component
public class FileController {
    private final FileService files;
    private final ApiSupport api;

    /** Dùng chung middleware actor và lỗi chuẩn của các API nghiệp vụ. */
    public FileController(FileService files, ApiSupport api) {
        this.files = files;
        this.api = api;
    }

    /** Đọc một ảnh multipart tối đa 5 MiB; không tin tên tệp hay MIME do client khai báo. */
    public ServerResponse upload(ServerRequest request) throws Exception {
        return api.handle(request,(actor,context) -> {
            if (request.headers().contentType().isEmpty()
                    || !MediaType.MULTIPART_FORM_DATA.isCompatibleWith(request.headers().contentType().get()))
                throw ServiceException.invalid("Use multipart/form-data with file and kind fields");
            try {
                var part = request.servletRequest().getPart("file");
                String kind = request.servletRequest().getParameter("kind");
                if (part == null || kind == null) throw ServiceException.invalid("File and kind are required");
                if (part.getSize()>FileService.MAX_BYTES)
                    throw new ServiceException(413,"FILE_TOO_LARGE","Maximum image size is 5 MiB");
                try (var stream = part.getInputStream()) {
                    return files.upload(actor,kind,part.getContentType(),stream.readNBytes(FileService.MAX_BYTES+1));
                }
            } catch (jakarta.servlet.ServletException exception) {
                if (request.servletRequest().getContentLengthLong()>FileService.MAX_BYTES)
                    throw new ServiceException(413,"FILE_TOO_LARGE","Maximum image size is 5 MiB");
                throw ServiceException.invalid("Multipart request is invalid");
            } catch (IllegalStateException exception) {
                throw new ServiceException(413,"FILE_TOO_LARGE","Maximum image size is 5 MiB");
            }
        });
    }

    /** Trả mô tả tệp cho owner hoặc admin được phép xem hồ sơ. */
    public ServerResponse metadata(ServerRequest request) throws Exception {
        return api.handle(request,(actor,context) -> files.metadata(actor,UUID.fromString(request.pathVariable("id"))));
    }

    /** Trả byte ảnh riêng tư, không cache và không cho browser suy đoán loại nội dung. */
    public ServerResponse content(ServerRequest request) throws Exception {
        return api.handle(request,(actor,context) -> {
            var content = files.content(actor,UUID.fromString(request.pathVariable("id")));
            String disposition = request.param("download").filter("true"::equals).isPresent() ? "attachment" : "inline";
            return ServerResponse.ok().contentType(MediaType.parseMediaType(content.mime()))
                    .header("Cache-Control","no-store, private").header("X-Content-Type-Options","nosniff")
                    .header("Content-Security-Policy","default-src 'none'; img-src 'self'; sandbox")
                    .header("Content-Disposition",disposition+"; filename=\""+content.filename()+"\"")
                    .body(content.bytes());
        });
    }

    /** Danh sách đợt đăng ký còn mở cho form của sinh viên. */
    public ServerResponse windows(ServerRequest request) throws Exception {
        return api.handle(request,(actor,context) -> files.windows(actor));
    }
}
