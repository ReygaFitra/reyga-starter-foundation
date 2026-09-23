package reyga.starter.foundation.common_database.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import reyga.starter.foundation.common_database.processor.DefaultQueryProcessor;
import reyga.starter.foundation.common_database.processor.QueryProcessor;

@Configuration
public class CommonDatabaseConfig {

    @Bean
    public QueryProcessor queryProcessor(JdbcTemplate jdbcTemplate) {
        return new DefaultQueryProcessor(jdbcTemplate);
    }

}
