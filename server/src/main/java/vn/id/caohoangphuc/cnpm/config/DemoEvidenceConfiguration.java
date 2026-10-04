package vn.id.caohoangphuc.cnpm.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.id.caohoangphuc.cnpm.service.FileService;

/** Bổ sung nội dung ảnh chỉ cho các metadata demo đã được migration tạo. */
@Configuration
public class DemoEvidenceConfiguration {
    /** Mã hóa ảnh mẫu bằng khóa của deployment; không reset tệp hay hồ sơ đã có. */
    @Bean
    ApplicationRunner seedEvidenceImages(FileService files) {
        return arguments -> files.seedDemoImages();
    }
}
