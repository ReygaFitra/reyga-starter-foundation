package reyga.starter.foundation.common_database.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registers {@link CommonDatabaseConfig} through Boot auto-configuration without changing its bean factories.
 * The legacy configuration remains available for explicit imports and direct factory calls.
 */
@AutoConfiguration
@Import(CommonDatabaseConfig.class)
public class CommonDatabaseAutoConfiguration {

    /** Creates this feature entry point. */
    public CommonDatabaseAutoConfiguration() {
    }
}
