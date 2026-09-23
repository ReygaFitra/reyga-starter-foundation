package reyga.starter.foundation.common_io.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.common_io.operations.DefaultFileInspector;
import reyga.starter.foundation.common_io.operations.DefaultFileSanitizer;
import reyga.starter.foundation.common_io.operations.FileInspector;
import reyga.starter.foundation.common_io.operations.FileSanitizer;

@Configuration
public class CommonIOConfig {

    @Bean
    public FileInspector fileInspector() {
        return new DefaultFileInspector();
    }

    @Bean
    public FileSanitizer fileSanitizer() {
        return new DefaultFileSanitizer();
    }

}
