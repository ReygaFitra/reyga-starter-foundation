package reyga.starter.foundation.logging.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reyga.starter.foundation.logging.interceptor.BaseLogInterceptor;

@Configuration
@RequiredArgsConstructor
public class LogInterceptorConfig implements WebMvcConfigurer {

    private final BaseLogInterceptor baseLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseLogInterceptor);
    }


}
