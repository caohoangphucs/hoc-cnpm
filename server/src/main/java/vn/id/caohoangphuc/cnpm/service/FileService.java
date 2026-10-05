package vn.id.caohoangphuc.cnpm.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.id.caohoangphuc.cnpm.persistence.service.FileRepository;

/** Kiểm tra ảnh, mã hóa nội dung và áp dụng quyền của chủ hồ sơ/admin. */
@Service
public class FileService {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> KINDS = Set.of("CCCD_FRONT","CCCD_BACK","STUDENT_CARD","REPAIR","EVIDENCE","POLICY");
    public record FileInfo(UUID id, String kind, String mime, int sizeBytes, String sha256, String contentUrl) {}
    public record Content(byte[] bytes, String mime, String filename) {}
    private final FileRepository repository;
    private final ServiceContext context;
    private final SecretKeySpec key;

    /** Khóa ổn định theo cấu hình deploy; tách miền khóa file khỏi TOTP. */
    public FileService(FileRepository repository, ServiceContext context,
                       @Value("${security.file-encryption-key:${security.totp-encryption-key}}") String configuredKey) {
        this.repository = repository;
        this.context = context;
        try {
            key = new SecretKeySpec(MessageDigest.getInstance("SHA-256")
                    .digest(("cnpm-evidence-v1:" + configuredKey).getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize file encryption", exception);
        }
    }

    /** Tải ảnh lên thành tệp nháp; chỉ gắn vào nghiệp vụ sau khi service kiểm tra hồ sơ. */
    public FileInfo upload(Actor actor, String kind, String mime, byte[] raw) {
        var account = context.require(actor,"SINHVIEN","ADMIN","KETOAN","KYTHUAT","BAOVE");
        if (kind == null || !KINDS.contains(kind)) throw ServiceException.invalid("Unsupported file kind");
        if (Set.of("CCCD_FRONT","CCCD_BACK","STUDENT_CARD").contains(kind) && !account.role().equals("SINHVIEN"))
            throw new ServiceException(403,"FORBIDDEN","Identity documents belong to student accounts");
        byte[] image = checkedImage(raw,mime);
        return context.transaction(() -> {
            UUID id = UUID.randomUUID();
            byte[] nonce = nonce();
            repository.insert(id,account.id(),kind,mime,image,hash(image),nonce,crypt(Cipher.ENCRYPT_MODE,image,nonce));
            return info(repository.find(id));
        });
    }

    /** Trả metadata an toàn, không trả key/nonce hoặc đường dẫn storage nội bộ. */
    public FileInfo metadata(Actor actor, UUID id) {
        return info(readable(actor,id));
    }

    /** Giải mã sau kiểm tra quyền; xác minh hash để không trả nội dung bị hỏng. */
    public Content content(Actor actor, UUID id) {
        var file = readable(actor,id);
        byte[] bytes = crypt(Cipher.DECRYPT_MODE,file.encrypted(),file.nonce());
        if (!hash(bytes).equals(file.sha256())) throw new ServiceException(500,"FILE_CORRUPT","File integrity check failed");
        return new Content(bytes,file.mime(),file.kind() + (file.mime().equals("image/png") ? ".png" : ".jpg"));
    }

    /** Lấy đợt đăng ký bằng thời gian server, không cho client tự giả định window ID. */
    public List<FileRepository.RegistrationWindow> windows(Actor actor) {
        context.require(actor,"SINHVIEN","ADMIN");
        return repository.windows();
    }

    /** Không mở quyền admin cho tệp nháp hoặc tệp không thuộc hồ sơ đăng ký. */
    private FileRepository.StoredFile readable(Actor actor, UUID id) {
        var account = context.require(actor,"SINHVIEN","ADMIN","KETOAN","KYTHUAT","BAOVE");
        var file = repository.find(id);
        if (file == null || (file.ownerId()!=account.id()
                && !(account.role().equals("ADMIN") && repository.attachedToBooking(id))
                && !repository.readableRepair(id, account.id(), account.role())
                && !repository.readableDiscipline(id, account.id(), account.role())))
            throw ServiceException.notFound("File");
        return file;
    }

    /** Kiểm tra magic/decoder, MIME và kích thước trước khi giải mã ảnh; loại bỏ metadata khi ghi lại. */
    private byte[] checkedImage(byte[] raw, String mime) {
        if (raw.length>MAX_BYTES) throw new ServiceException(413,"FILE_TOO_LARGE","Maximum image size is 5 MiB");
        if (raw.length==0 || mime == null || !Set.of("image/png","image/jpeg").contains(mime))
            throw ServiceException.invalid("Upload a PNG or JPEG image");
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(raw))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw ServiceException.invalid("Image content is invalid");
            var reader = readers.next();
            try {
                reader.setInput(input,true,true);
                String format = reader.getFormatName();
                String expected = mime.equals("image/png") ? "PNG" : "JPEG";
                if (!format.equalsIgnoreCase(expected)) throw ServiceException.invalid("Image MIME does not match content");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width<1 || height<1 || width>6000 || height>6000 || (long)width*height>20_000_000)
                    throw ServiceException.invalid("Image dimensions are too large");
                var image = reader.read(0);
                var output = new ByteArrayOutputStream();
                if (!ImageIO.write(image,expected,output)) throw ServiceException.invalid("Image cannot be stored");
                byte[] clean = output.toByteArray();
                if (clean.length>MAX_BYTES) throw new ServiceException(413,"FILE_TOO_LARGE","Maximum image size is 5 MiB");
                return clean;
            } finally {
                reader.dispose();
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw ServiceException.invalid("Image content is invalid");
        }
    }

    /** Tạo nonce riêng cho từng ảnh; không tái sử dụng nonce giữa các tệp. */
    private byte[] nonce() {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /** Mã hóa/xác thực AES-GCM; lỗi giải mã không làm lộ ciphertext hay khóa. */
    private byte[] crypt(int mode, byte[] bytes, byte[] nonce) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode,key,new GCMParameterSpec(128,nonce));
            return cipher.doFinal(bytes);
        } catch (Exception exception) {
            throw new ServiceException(500,"FILE_STORAGE_ERROR","File encryption or decryption failed");
        }
    }

    /** Tính checksum của ảnh đã chuẩn hóa để xác minh mỗi lần tải xuống. */
    private String hash(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** Chỉ cung cấp URL API có kiểm tra phiên đăng nhập, không có public object URL. */
    private FileInfo info(FileRepository.StoredFile file) {
        return new FileInfo(file.id(),file.kind(),file.mime(),file.size(),file.sha256(),"/api/v1/files/"+file.id()+"/content");
    }

    /** Bổ sung ảnh tổng hợp cho metadata seed, chạy lại không ghi đè ảnh đã lưu. */
    public void seedDemoImages() {
        for (var file : repository.missingDemoFiles()) {
            UUID id = (UUID) file.get("id");
            String kind = (String) file.get("kind");
            try (var stream = getClass().getResourceAsStream("/demo-evidence/" + kind + ".png")) {
                if (stream == null) throw new IllegalStateException("Demo image is missing");
                byte[] image = stream.readAllBytes();
                context.transaction(() -> {
                    byte[] nonce = nonce();
                    repository.seedContent(id,image,hash(image),nonce,crypt(Cipher.ENCRYPT_MODE,image,nonce));
                    return null;
                });
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Cannot load demo image",exception);
            }
        }
    }
}
