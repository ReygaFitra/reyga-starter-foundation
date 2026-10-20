package reyga.starter.foundation.logging.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registers {@link AsyncLoggingConfig} through Boot auto-configuration without changing its bean factories.
 * The legacy configuration remains available for explicit imports and direct factory calls.
 */
@AutoConfiguration
@Import(AsyncLoggingConfig.class)
public class AsyncLoggingAutoConfiguration {

    /** Creates this feature entry point. */
    public AsyncLoggingAutoConfiguration() {
    }
}
