package reyga.starter.foundation.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.common.logging.CustomLogger;

@Configuration
public class CommonConfig {

    @Bean
    public CustomLogger customLogger() {
        return new CustomLogger();
    }

}
