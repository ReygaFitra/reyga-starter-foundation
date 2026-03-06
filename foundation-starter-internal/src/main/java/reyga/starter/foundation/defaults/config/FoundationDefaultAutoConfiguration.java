package reyga.starter.foundation.defaults.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import reyga.starter.foundation.common.config.CommonConfig;
import reyga.starter.foundation.common_database.processor.DefaultQueryProcessor;
import reyga.starter.foundation.common_database.processor.QueryProcessor;
import reyga.starter.foundation.core.config.CoreConfig;
import reyga.starter.foundation.logging.config.AsyncLoggingConfig;
import reyga.starter.foundation.logging.config.LogInterceptorConfig;
import reyga.starter.foundation.logging.config.LoggingConfig;
import reyga.starter.foundation.logging.interceptor.util.CustomRequestBodyAdviceAdapter;

@Configuration
@Import({
        CommonConfig.class,
        CoreConfig.class,
        LogInterceptorConfig.class,
        LoggingConfig.class,
        AsyncLoggingConfig.class
})
public class FoundationDefaultAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(QueryProcessor.class)
    public QueryProcessor queryProcessor(JdbcTemplate jdbcTemplate) {
        return new DefaultQueryProcessor(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CustomRequestBodyAdviceAdapter.class)
    public CustomRequestBodyAdviceAdapter customRequestBodyAdviceAdapter() {
        return new CustomRequestBodyAdviceAdapter();
    }
}
