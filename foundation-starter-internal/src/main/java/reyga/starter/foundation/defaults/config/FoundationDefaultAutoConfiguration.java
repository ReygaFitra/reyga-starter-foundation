package reyga.starter.foundation.defaults.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import reyga.starter.foundation.common.config.CommonConfig;
import reyga.starter.foundation.common_database.config.CommonDatabaseConfig;
import reyga.starter.foundation.common_io.config.CommonIOConfig;
import reyga.starter.foundation.core.config.CoreConfig;
import reyga.starter.foundation.logging.config.AsyncLoggingConfig;
import reyga.starter.foundation.logging.config.LoggingConfig;

@AutoConfiguration
@Import(
        {
                LoggingConfig.class, AsyncLoggingConfig.class, CoreConfig.class,
                CommonIOConfig.class, CommonConfig.class, CommonDatabaseConfig.class
        })
public class FoundationDefaultAutoConfiguration {

}
