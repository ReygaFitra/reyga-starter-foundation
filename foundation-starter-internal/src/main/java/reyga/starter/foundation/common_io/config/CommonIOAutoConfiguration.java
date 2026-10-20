package reyga.starter.foundation.common_io.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registers {@link CommonIOConfig} through Boot auto-configuration without changing its bean factories.
 * The legacy configuration remains available for explicit imports and direct factory calls.
 */
@AutoConfiguration
@Import(CommonIOConfig.class)
public class CommonIOAutoConfiguration {

    /** Creates this feature entry point. */
    public CommonIOAutoConfiguration() {
    }
}
